import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
from pathlib import Path
import re
import pandas as pd
import math
import matplotlib.patches as mpatches
import matplotlib as mpl

mpl.rcParams.update({
    "font.family": "serif",
    #"font.serif": ["Times New Roman"],

    # Font sizes
    "axes.titlesize": 9,
    "axes.labelsize": 5,   
    "xtick.labelsize": 5,    
    "ytick.labelsize": 5,   
    "legend.fontsize": 5,   
    "figure.titlesize": 5  
})


def parse_log_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        log = f.read()

    blocks = re.split(r'-{10,}', log)
    data = []

    for block in blocks:
        if not block.strip():
            continue

        # Extract method name
        method_match = re.search(r'Training model (out/.+?\.csv)', block)
        method = method_match.group(1).split('/')[1] if method_match else 'Unknown'

        # Split by model
        model_sections = re.split(r'\n\n+', block.strip())
        current_model = None

        for section in model_sections:
            lines = section.strip().splitlines()
            if not lines:
                continue

            header = lines[0].lower()
            if 'decision tree' in header:
                current_model = 'Decision Tree'
            elif 'random forest' in header:
                current_model = 'Random Forest'
            elif 'gradient boosting' in header:
                current_model = 'Gradient Boosting'
            elif 'linear regression' in header:
                current_model = 'Linear Regression'
            elif 'pysr' in header:
                current_model = 'PySR'

            if not current_model:
                continue

            # Default: Before tuning
            tuning = 'Before'

            for line in lines:
                if 'best model scores' in line.lower():
                    tuning = 'After'

                #r2_match = re.search(r'R² Score:\s*([0-9.]+)', line)
                #mse_match = re.search(r'Mean Squared Error:\s*([0-9.eE+-]+)', line)
                r2_match = re.search(r'R² Score:\s*(-?[0-9.eE+-]+)', line)
                mse_match = re.search(r'Mean Squared Error:\s*(-?[0-9.eE+-]+)', line)

                if r2_match:
                    r2_score = float(r2_match.group(1))
                if mse_match:
                    mse_score = float(mse_match.group(1))

            if current_model and 'r2_score' in locals() and 'mse_score' in locals():
                data.append({
                    'Method': method,
                    'Model': current_model,
                    'Tuning': tuning,
                    'R²': r2_score,
                    'MSE': mse_score
                })

                # Clear vars for next round
                del r2_score
                del mse_score

    return pd.DataFrame(data)


def get_method_dirs():
    path = Path("out")
    return [d.name for d in path.iterdir() if d.is_dir()]

def ignore_some_methods(dir_names,ignore):
    return [dir for dir in dir_names if dir not in ignore]

def change_pos(df,pos1,pos2):
    temp = df[pos1]
    df[pos1] = df[pos2] 
    df[pos2] = temp
    return df

def main():
    dir_names = get_method_dirs()
    dir_names = ignore_some_methods(dir_names,{'subList_int_int_','MultiplyAv_int_double___double___','MultiplyAtv_int_double___double___','MultiplyAtAv_int_double___double___','fannkuch_int_','energy__','Approximate_int_','advance_double_','A_int_int_','loops_int_int_','trees_int_','checkTree_com_template_programsToBenchmark_BinaryTrees_TreeNode_','createTree_int_'})
    all_dfs = []
    for dir in dir_names:
        df = parse_log_file("out/"+dir+"/log.txt")
        all_dfs.append(df)
        #print(dir)
    all_dfs = change_pos(all_dfs,3,4)
    panel_charts(all_dfs,"MSE")#MSE R²

    #combined_df = pd.concat(all_dfs, ignore_index=True)
    #sorted_mse = combined_df['MSE'].sort_values(ascending=False)
    #print(sorted_mse)

def clean_method_name(method_name):
    if method_name == "retainAll_java_util_Collection": return "retainAll(Collection)"
    if method_name == "remove_java_lang_Object": return "remove(Object)"
    if method_name == "values": return "values()"
    if method_name == "containsKey_java_lang_Object": return "containsKey(Object)"
    if method_name == "get_int": return "get(Int)"
    if method_name == "put_java_lang_Object_java_lang_Object": return "put(Object, Object)"
    if method_name == "isEmpty": return "isEmpty()"
    if method_name == "addAll_int_java_util_Collection": return "addAll(int, Collection)"
    if method_name == "clear": return "clear()"
    if method_name == "iterator": return "iterator()"
    if method_name == "get_java_lang_Object": return "get(Object)"
    if method_name == "contains_java_lang_Object": return "contains(Object)"
    if method_name == "set_int_java_lang_Object": return "set(int, Object)"
    if method_name == "size": return "size()"
    if method_name == "containsAll_java_util_Collection": return "containsAll(Collection)"
    if method_name == "add_java_lang_Object": return "add(Object)"
    if method_name == "addAll_java_util_Collection": return "addAll(Collection)"
    if method_name == "equals_java_lang_Object": return "equals(Object)"
    if method_name == "containsValue_java_lang_Object": return "containsValue(Object)"
    if method_name == "entrySet": return "entrySet()"
    if method_name == "add_int_java_lang_Object": return "add(int, Object)"
    if method_name == "values": return "values()"

def single_chart(df, ax, model_colors,val='R²'):
    method_name = df.iloc[0]['Method'].rstrip("_") 
    method_name = clean_method_name(method_name)
    df_best = df.groupby('Model', as_index=False)[val].max()
    bar_colors = [model_colors[model] for model in df_best['Model']]
    
    bars = ax.bar(df_best['Model'], df_best[val], color=bar_colors)
    if (val == "R²"): ax.set_ylim(0, 1.15)  # Give space above bars
    ax.set_xlabel('')
    #ax.set_title(f'Method: {method_name}')
    ax.set_title(f'{method_name}')
    ax.set_xticks([])

    for bar, value in zip(bars, df_best[val]):
        height = bar.get_height()
        if abs(value) < 0.001:label = f"{value:.0e}"
        else:label = f"{value:.2f}"
        y_min, y_max = ax.get_ylim()
        offset = 0.015 * (y_max - y_min)

        #the .8 is the percentage of the bar the needs to reach for the value to go inside the bar
        ax.text(
            bar.get_x() + bar.get_width() / 2,
            height - offset if height >= 0.8 * y_max else height + offset,
            label,
            ha='center',
            va='top' if height >= 0.8 * y_max else 'bottom',
            fontsize=6,
            color='black',
            )    


#panel bar char
def panel_charts(df_list,val, cols=3):
    all_models = sorted(set().union(*[df['Model'].unique() for df in df_list]))
    base_colors = plt.cm.tab10.colors
    model_colors = {model: base_colors[i % len(base_colors)] for i, model in enumerate(all_models)}

    n = len(df_list)
    rows = math.ceil(n / cols)

    #fig_height = rows * 5 + 1.5 
    #fig, axes = plt.subplots(rows, cols, figsize=(cols * 6, fig_height))
    fig_width_inch = 7  # matches \textwidth in LaTeX
    cell_width = fig_width_inch / cols
    cell_height = 1.5  # reasonable row height

    fig_height_inch = rows * cell_height
    fig, axes = plt.subplots(rows, cols, figsize=(fig_width_inch, fig_height_inch))


    axes = axes.flatten()

    for i, df in enumerate(df_list):
        #print(f'names -> {df['Method']}')
        single_chart(df, axes[i], model_colors,val)
    for j in range(i + 1, len(axes)):
        fig.delaxes(axes[j])

    handles = [mpatches.Patch(color=color, label=model) for model, color in model_colors.items()]

    handles = [mpatches.Patch(color=color, label=model) for model, color in model_colors.items()]
    fig.legend(
        handles,
        model_colors.keys(),
        title='Models',
        loc='lower center',
        ncol=min(len(model_colors), cols),
        bbox_to_anchor=(0.5, 0.07),
        fontsize=9,
        title_fontsize=10
    )

    #fig.suptitle(f"{val} Score Comparison", fontsize=25, fontweight='bold')
    #plt.tight_layout(rect=[0, 0.05, 1, 0.93])  # leave space for legend and title
    #plt.subplots_adjust(hspace=.5, bottom=0.2, top=0.9)
    plt.tight_layout(rect=[0, 0.05, 1, 0.95])
    plt.subplots_adjust(hspace=0.6, bottom=0.15, top=0.88, wspace= .3)
    plt.savefig("../relatorio/figures/mse_comparison.pdf", bbox_inches='tight', dpi=300)
    #plt.savefig("mse_comparison.pdf")
    plt.show()

main()