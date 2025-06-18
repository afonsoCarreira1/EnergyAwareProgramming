import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
from pathlib import Path
import re
import pandas as pd
import math
import matplotlib.patches as mpatches


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

                r2_match = re.search(r'R² Score:\s*([0-9.]+)', line)
                mse_match = re.search(r'Mean Squared Error:\s*([0-9.eE+-]+)', line)

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


def main():
    dir_names = get_method_dirs()
    all_dfs = []
    for dir in dir_names:
        df = parse_log_file("out/"+dir+"/log.txt")
        all_dfs.append(df)

    #panel_charts(all_dfs,"MSE")
    combined_df = pd.concat(all_dfs, ignore_index=True)
    sorted_mse = combined_df['MSE'].sort_values(ascending=False)
    print(sorted_mse)

    

def single_chart(df, ax, model_colors,val='R²'):
    method_name = df.iloc[0]['Method'].rstrip("_") 
    df_best = df.groupby('Model', as_index=False)[val].max()
    bar_colors = [model_colors[model] for model in df_best['Model']]
    
    bars = ax.bar(df_best['Model'], df_best[val], color=bar_colors)
    ax.set_ylim(0, 1.15)  # Give space above bars
    ax.set_xlabel('')
    ax.set_title(f'Method: {method_name}')
    ax.set_xticks([])

    for bar, value in zip(bars, df_best[val]):
        height = bar.get_height()
        label = f"{value:.2f}"[:4]
        
        if height >= 0.92:
            # Label inside bar
            ax.text(
                bar.get_x() + bar.get_width() / 2,
                height - 0.04,
                label,
                ha='center',
                va='top',
                fontsize=9,
                color='white'
            )
        else:
            # Label above bar
            ax.text(
                bar.get_x() + bar.get_width() / 2,
                height + 0.015,
                label,
                ha='center',
                va='bottom',
                fontsize=9,
                color='black'
            )


#panel bar char
def panel_charts(df_list,val, cols=2):
    all_models = sorted(set().union(*[df['Model'].unique() for df in df_list]))
    base_colors = plt.cm.tab10.colors
    model_colors = {model: base_colors[i % len(base_colors)] for i, model in enumerate(all_models)}

    n = len(df_list)
    rows = math.ceil(n / cols)

    fig_height = rows * 5 + 1.5 
    fig, axes = plt.subplots(rows, cols, figsize=(cols * 6, fig_height))
    axes = axes.flatten()

    for i, df in enumerate(df_list):
        single_chart(df, axes[i], model_colors,val)
    for j in range(i + 1, len(axes)):
        fig.delaxes(axes[j])

    handles = [mpatches.Patch(color=color, label=model) for model, color in model_colors.items()]
    fig.legend(
        handles,
        model_colors.keys(),
        title='Models',
        loc='lower center',
        ncol=len(model_colors),
        bbox_to_anchor=(0.5, 0.02)
    )
    fig.suptitle("R² Score Comparison", fontsize=16, fontweight='bold')
    plt.tight_layout(rect=[0, 0.05, 1, 0.93])  # leave space for legend and title
    plt.subplots_adjust(hspace=.5, bottom=0.2, top=0.9)
    plt.show()

main()