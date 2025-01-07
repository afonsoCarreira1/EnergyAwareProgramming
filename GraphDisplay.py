import sys
import pandas as pd
import matplotlib.pyplot as plt
import glob
import os
from math import sqrt
import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
import seaborn as sns

def script_usage(args):
    if len(args) != 4:
        raise ValueError ("""Incorrect number of arguments arguments!
                Usage example: python3 GraphDisplay.py 4orch_fib Fib j""")

def create_orchestrators(orchestrators,files,c_or_java_prog,prog_name):
    return [{
        "name": orch[0],
        "files": f"{files}/{orch[0][0]}_files_{c_or_java_prog}_{prog_name}",
        "color":orch[1],
    } for orch in orchestrators]

def read_java_python_files(orch):
    file_paths = glob.glob(f'{orch["files"]}/*.csv')#sorted(glob.glob(f'{files}/*.csv'))
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

def calculate_standard_deviation(orch):
    return sqrt(sum([(n-orch["average_power"])**2 for n in orch["all_powers"]])/len(orch["all_powers"]))

def calculate_average(orch):
    return round(sum(orch["all_powers"])/len(orch["all_powers"]),3)

def plot_hist(orch):
    plt.figure(figsize=(8, 4))
    plt.hist(orch["all_powers"], bins=10, edgecolor='black',color=orch["color"])
    plt.xlabel('Energy')
    plt.ylabel('Number of Runs')
    plt.title(f'Average power of {orch["name"]} orchestrator {orch["average_power"]}j')
    plt.show(block=orch["name"]=="bash")

def plot_graph(orch,prog_name,c_or_java_prog):
    if orch["name"] == 'java':
        plt.figure(figsize=(10, 6))
        plt.title(f'Power of 100 {prog_name} runs in {c_or_java_prog}',fontsize=16)
    sns.lineplot(data=orch["all_powers"], color=orch["color"], linestyle='-', label=f"({orch["name"]}) avg: {round(orch["average_power"])}j\nstd dev: {round(orch["standard_deviation"], 2)}")
    plt.xlabel('Run',fontsize=16)
    plt.ylabel('CPU Power',fontsize=16)
    plt.legend(fontsize=16)
    plt.grid(False)
    plt.show(block=orch["name"]=="bash")


def plot_combined_graph(orchs, prog_name, c_or_java_prog, pdf_filename="power_plot.pdf"):
    """
    Plot all orchestrator data on a single figure and save to PDF.
    """
    plt.figure(figsize=(10, 6))
    plt.title(f'Power of 100 {prog_name} runs in {c_or_java_prog}', fontsize=18)
    
    for orch in orchs:
        sns.lineplot(
            data=orch["all_powers"],
            color=orch["color"],
            linestyle='-',
            label=f"({orch['name']}) avg: {round(orch['average_power'])}j\nstd dev: {round(orch['standard_deviation'], 2)}"
        )
    
    plt.xlabel('Run', fontsize=16)
    plt.ylabel('CPU Power', fontsize=16)
    plt.legend(fontsize=18)
    plt.grid(False)
    plt.tight_layout()
    
    # Save the plot to a PDF
    with PdfPages(pdf_filename) as pdf:
        pdf.savefig()  # Saves the current figure to the PDF
    plt.show()

def main():
    script_usage(sys.argv)
    args = sys.argv[1:]
    files = args[0]
    prog_name = args[1]
    c_or_java_prog = args[2] if args[2] == 'c' else 'java'
    orchs = create_orchestrators([("java","blue"),("python","orange"),("c","red"),("bash","green")],files,c_or_java_prog,prog_name)
    for orch in orchs:
        orch["all_powers"] = read_java_python_files(orch) # get the data from the files
        orch["average_power"] = calculate_average(orch)
        orch["standard_deviation"] = calculate_standard_deviation(orch)
        #plot_graph(orch,prog_name,c_or_java_prog)
        #plot_hist(orch)
    plot_combined_graph(orchs, prog_name, c_or_java_prog, pdf_filename=f"{prog_name}_power_plot.pdf")

if __name__ == "__main__":
    main()