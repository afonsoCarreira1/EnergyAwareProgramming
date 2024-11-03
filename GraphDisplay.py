import sys
import pandas as pd
import matplotlib.pyplot as plt
import glob
import os
from math import sqrt
import matplotlib.pyplot as plt
import numpy as np
from scipy.stats import norm, lognorm, expon, kstest

def read_java_python_files(files):
    file_paths = glob.glob(f'{files}/*.csv')#sorted(glob.glob(f'{files}/*.csv'))
    all_powers = [pd.read_csv(file)['CPU Power'].sum() for file in file_paths]
    all_powers.sort()
    return all_powers

def get_joular_jx_data():
    all_powers = []
    #root_dir = os.path.join(os.path.dirname(os.getcwd()),'joularjx/joularjx-result')
    root_dir = os.path.join(os.path.dirname(os.getcwd()),'powerjoular_man/joularjx_files')
    average_power ,number_of_runs = 0,0
    for dirpath, dirnames, filenames in os.walk(root_dir):
        dirnames.sort()
        filenames.sort()
        if not 'app/total/methods' in dirpath: continue
        number_of_runs+=1
        for filename in filenames:
            file_path = os.path.join(dirpath, filename)
            try:
                with open(file_path, 'r') as file:
                    content = file.read()
                    filtered_content = content.split('Test.fibonacci')
                    filtered_content = float(str(filtered_content[-1])[1:-1])
                    all_powers.append(filtered_content)
                    average_power += filtered_content
            except Exception as e:
                print(f"Could not read {file_path}: {e}")            
    average_power /=number_of_runs
    return all_powers ,average_power, 

def calculate_standard_deviation(data,average):
    return sqrt(sum([(n-average)**2 for n in data])/len(data))

def calculate_average(data):
    return round(sum(data)/len(data),3)

def plot_hist(data,average_power,orchestrator,color='blue',block = False):
    plt.figure(figsize=(8, 4))
    plt.hist(data, bins=10, edgecolor='black',color=color)  # Adjust bins as needed
    plt.xlabel('Energy')
    plt.ylabel('Number of Runs')
    plt.title(f'Average power of {orchestrator} orchestrator {average_power} J')
    plt.show(block=block)

def plot_graph(data,average_power,orchestrator,color='blue',block = False,std_dev=None,prog_name="No program named passed"):
    if orchestrator == 'java': plt.figure(figsize=(10, 6))
    plt.plot(data,linestyle='-',color=color, label=f"avg: {round(average_power)}j\nstd dev: {round(std_dev,2)}")
    plt.title(f'Power of 100 {prog_name} runs')
    plt.xlabel('Run')
    plt.ylabel('CPU Power')
    plt.legend()
    plt.grid(True)
    plt.show(block=block)
    

def main():
    args = sys.argv[1:]
    files = args[0]
    prog_name = args[1]
    c_or_java_prog = args[2] if args[2] == 'c' else 'java'
    java_files = f"{files}/j_files_{c_or_java_prog}_{prog_name}"
    python_files = f"{files}/p_files_{c_or_java_prog}_{prog_name}"
    c_files = f"{files}/c_files_{c_or_java_prog}_{prog_name}"
    bash_files = f"{files}/b_files_{c_or_java_prog}_{prog_name}"
    # read all files
    all_powers_java = read_java_python_files(java_files)
    all_powers_python = read_java_python_files(python_files)
    all_powers_c = read_java_python_files(c_files)
    all_powers_bash = read_java_python_files(bash_files)

    #get average
    average_power_java = calculate_average(all_powers_java)
    average_power_python = calculate_average(all_powers_python)
    average_power_c = calculate_average(all_powers_c)
    average_power_bash = calculate_average(all_powers_bash)

    #get standard deviation
    standard_deviation_java = calculate_standard_deviation(all_powers_java,average_power_java)
    standard_deviation_python = calculate_standard_deviation(all_powers_python,average_power_python)
    standard_deviation_c = calculate_standard_deviation(all_powers_c,average_power_c)
    standard_deviation_bash = calculate_standard_deviation(all_powers_bash,average_power_bash)
    print(f"standard deviation java: {standard_deviation_java}")
    print(f"standard deviation python: {standard_deviation_python}")
    print(f"standard deviation c: {standard_deviation_c}")
    print(f"standard deviation bash: {standard_deviation_bash}")

    plot_graph(all_powers_java,average_power_java,'java',std_dev=standard_deviation_java)
    plot_graph(all_powers_python,average_power_python,'python',color='orange',std_dev=standard_deviation_python)
    plot_graph(all_powers_c,average_power_c,'c',color='red',std_dev=standard_deviation_c)
    plot_graph(all_powers_bash,average_power_bash,'bash',color='green',std_dev=standard_deviation_bash,prog_name=prog_name,block=True)
    
    #plot_hist(all_powers_java,average_power_java,'java')
    #plot_hist(all_powers_python,average_power_python,'python',color='orange')
    #plot_hist(all_powers_c,average_power_c,'c',color='red')
    #plot_hist(all_powers_bash,average_power_bash,'bash',block=True,color='green')

if __name__ == "__main__":
    main()