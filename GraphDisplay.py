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

def plot_hist(data,color='blue',block = False):
    plt.figure(figsize=(10, 6))
    plt.hist(data, bins=10, edgecolor='black',color=color)  # Adjust bins as needed
    plt.xlabel('Energy')
    plt.ylabel('Number of Runs')
    plt.title('Histogram of Energy Data')
    plt.show(block=block)

def plot_graph(data,average_power,color='blue',block = False):
    plt.figure(figsize=(10, 6))
    plt.plot(data, marker='o', linestyle='-',color=color, label='J')
    plt.title(f'Average power of java files {average_power} J')
    plt.xlabel('Run')
    plt.ylabel('CPU Power')
    plt.legend()
    plt.grid(True)
    plt.show(block=block)


def main():
    java_files = "fib_100ms_delay/java_files_c_fib"
    python_files = "fib_100ms_delay/python_files_c_fib"
    # read all files
    all_powers_java = read_java_python_files(java_files)
    all_powers_python = read_java_python_files(python_files)

    #get average
    average_power_java = calculate_average(all_powers_java)
    average_power_python = calculate_average(all_powers_python)

    #get standard deviation
    standard_deviation_java = calculate_standard_deviation(all_powers_java,average_power_java)
    standard_deviation_python = calculate_standard_deviation(all_powers_python,average_power_python)
    print(f"standard deviation java: {standard_deviation_java}")
    print(f"standard deviation python: {standard_deviation_python}")

    #plots
    plot_graph(all_powers_java,average_power_java)
    plot_graph(all_powers_python,average_power_python,color='orange')
    plot_hist(all_powers_java)
    plot_hist(all_powers_python,block=True,color='orange')


if __name__ == "__main__":
    main()