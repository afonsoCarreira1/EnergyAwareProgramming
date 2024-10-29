import pandas as pd
import matplotlib.pyplot as plt
import glob
import os

files = input("p | j | jx: ")
if files == 'p': files = "python_files"
elif files == 'j': files = "java_files"
else: files = "jx"

if files != "jx": 
    file_paths = sorted(glob.glob(f'{files}/*.csv'))
    all_powers = [pd.read_csv(file)['CPU Power'].sum() for file in file_paths]
    average_power = round(sum(all_powers)/len(all_powers),3)
else:
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
    #print(f'Average power of joularjx was {average_power}j')

plt.figure(figsize=(10, 6))
plt.plot(all_powers, marker='o', linestyle='-', label='J')
plt.title(f'Average power of {average_power} J')
plt.xlabel('run')
plt.ylabel('CPU Power')
plt.legend()
plt.grid(True)
plt.show()