import pandas as pd
import matplotlib.pyplot as plt
import glob
import os
from math import sqrt

#files = input("p | j | jx: ")
#if files == 'p': files = "python_files"
#elif files == 'j': files = "java_files"
#else: files = "jx"


file_paths_java = sorted(glob.glob(f'{"java_files"}/*.csv'))
file_paths_python = sorted(glob.glob(f'{"python_files"}/*.csv'))
all_powers_java = [pd.read_csv(file)['CPU Power'].sum() for file in file_paths_java]
all_powers_java.sort()
all_powers_python = [pd.read_csv(file)['CPU Power'].sum() for file in file_paths_python]
all_powers_python.sort()
average_power_java = round(sum(all_powers_java)/len(all_powers_java),3)
average_power_python = round(sum(all_powers_python)/len(all_powers_python),3)
#if files == "jx":
#    all_powers = []
#    #root_dir = os.path.join(os.path.dirname(os.getcwd()),'joularjx/joularjx-result')
#    root_dir = os.path.join(os.path.dirname(os.getcwd()),'powerjoular_man/joularjx_files')
#    average_power ,number_of_runs = 0,0
#    for dirpath, dirnames, filenames in os.walk(root_dir):
#        dirnames.sort()
#        filenames.sort()
#        if not 'app/total/methods' in dirpath: continue
#        number_of_runs+=1
#        for filename in filenames:
#            file_path = os.path.join(dirpath, filename)
#            try:
#                with open(file_path, 'r') as file:
#                    content = file.read()
#                    filtered_content = content.split('Test.fibonacci')
#                    filtered_content = float(str(filtered_content[-1])[1:-1])
#                    all_powers.append(filtered_content)
#                    average_power += filtered_content
#            except Exception as e:
#                print(f"Could not read {file_path}: {e}")            
#    average_power /=number_of_runs
#    #print(f'Average power of joularjx was {average_power}j')

standard_deviation_java = sqrt(sum([(n-average_power_java)**2 for n in all_powers_java])/len(all_powers_java))
print(f"standard deviation java: {standard_deviation_java}")
standard_deviation_python = sqrt(sum([(n-average_power_python)**2 for n in all_powers_python])/len(all_powers_python))
print(f"standard deviation python: {standard_deviation_python}")

plt.figure(figsize=(10, 6))
plt.plot(all_powers_java, marker='o', linestyle='-', label='J')
plt.title(f'Average power of java files {average_power_java} J')
plt.xlabel('Run')
plt.ylabel('CPU Power')
plt.legend()
plt.grid(True)
plt.show(block=False)


plt.figure(figsize=(10, 6))
plt.plot(all_powers_python, marker='o', linestyle='-', color='orange', label='J2')  # Customize as needed
plt.title(f'Average power of python files {average_power_python} J')
plt.xlabel('Run')
plt.ylabel('CPU Power')
plt.legend()
plt.grid(True)
plt.show()  

