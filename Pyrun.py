from pathlib import Path
from os.path import dirname, realpath
import os
import signal
import pandas as pd
import time
import subprocess
import shlex
import sys

class Runner:
    
    def __init__(self):
        #self.ROOT_DIR = Path("/home/afonso/Documents/joularjx")
        self.ROOT_DIR = Path("/home/afonso/Documents/EnergyAwareProgramming")
        self.start = 0
        self.stop = 0
        self.frequency = .1
        signal.signal(signal.SIGUSR1, self.start_measurement_signal)
        signal.signal(signal.SIGUSR2, self.stop_measurement_signal)

    def start_measurement_signal(self, signum, frame):
        """Handle SIGUSR1 to start measurement"""
        print("Signal received: Start measurement")
        self.start = 1

    def stop_measurement_signal(self, signum, frame):
        """Handle SIGUSR2 to stop measurement"""
        print("Signal received: Stop measurement")
        self.stop = 1

    def run_c_prog(self,file):
        self.target = subprocess.Popen(['pkexec', f'./c_progs/{file}', str(os.getpid())],stdout=subprocess.PIPE, stderr=subprocess.PIPE, cwd=self.ROOT_DIR,)
        command = "pkexec cat c_progs/pidfile.txt"
        try:
            time.sleep(.2)
            result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
            pid_content = result.stdout.strip()
            #print(f"PID from file: {pid_content}")  
        except subprocess.CalledProcessError as e:
            #print(f"An error occurred: {e.stderr.strip()}")
            exit()
        self.target.pid = int(pid_content)

    def m(self,file,run_c_prog):
        if run_c_prog:self.run_c_prog(file)
        else:
            self.target = subprocess.Popen(['java', f'java_progs/{file}', str(os.getpid())],stdout=subprocess.PIPE, stderr=subprocess.PIPE, cwd=self.ROOT_DIR,)
            time.sleep(.5)
            with open("java_progs/pid.txt", 'r') as file:
                child_pid = int(file.read().split('\n')[0])
            self.target.pid = child_pid
        
        while self.start == 0: continue
        start_time = time.time()
        #profiler_cmd = f'powerjoular -l -p {os.getpid()} -D .1 -f {self.ROOT_DIR / "powerjoular.csv"}'
        profiler_cmd2 = f'powerjoular -l -p {self.target.pid} -D {self.frequency} -f {self.ROOT_DIR / "powerjoular.csv"}'
        #print("parent",str(os.getpid()))
        #print("child",self.target.pid)

        #self.profiler = subprocess.Popen(shlex.split(profiler_cmd))
        subprocess.Popen(shlex.split(profiler_cmd2))
        print(f"Started measurement for PID {self.target.pid}")

        print("Waiting for process to finish...")
        while self.stop == 0:continue
        print("Java process finished execution.")
        end_time = time.time()
        try:
            os.kill(self.profiler.pid, signal.SIGINT) # graceful shutdown of powerjoular
            self.profiler.wait()
        except:pass

        self.target.kill()
        self.target.wait()
        self.readCSV(os.getpid())
        self.readCSV(self.target.pid)
        print(f"Process took {(end_time-start_time):.3f}s")

    def readCSV(self,pid):
        try:
            df = pd.read_csv(self.ROOT_DIR / f"powerjoular.csv-{pid}.csv")
            run_data = {
                'avg_cpu': round(df['CPU Utilization'].sum(), 3),
                'total_energy': round(sum([x*self.frequency for x in df['CPU Power']]), 3),
            }
            print(f'CPU energy: {run_data['total_energy']} J')
            return run_data
        except:return


def main():
    args = sys.argv[1:]
    file = args[0]
    runs = int(args[2])
    run_c_prog = True if args[1] == "t"  else False
    for _ in range(runs):
        Runner().m(file,run_c_prog)


if __name__ == "__main__":
    main()

