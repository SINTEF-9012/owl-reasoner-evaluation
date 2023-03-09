import numpy as np
import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt
import seaborn as sns
from os.path import exists
from pathlib import Path
from matplotlib.ticker import FormatStrFormatter
from matplotlib import ticker

def show_values_on_bars(axs, h_v="v", space=1):
    def _show_on_single_plot(ax):
        if h_v == "v":
            for p in ax.patches:
                _x = p.get_x() + p.get_width() / 2
                _y = p.get_y() + p.get_height() + float(space)
                try:
                    value = int(p.get_height())
                except:
                    value = 0
                ax.text(_x, _y, value, ha="center")
        elif h_v == "h":
            for p in ax.patches:
                _x = p.get_x() + p.get_width() + float(space)
                _y = p.get_y() + p.get_height()
                try:
                    value = int(p.get_width())
                except:
                    value = 0
                ax.text(_x, _y, value, ha="left")

    if isinstance(axs, np.ndarray):
        for idx, ax in np.ndenumerate(axs):
            _show_on_single_plot(ax)
    else:
        _show_on_single_plot(axs)


def load_analyze_csv(file_name):
    data = pd.read_csv(file_name)

    #new_data = data.iloc[:, 13:].copy()


    return data

def process_data(data, reasoners, suffix):

    new_columns = []
    for reasoner in reasoners:
        column_name =  reasoner + "_" + suffix
        new_column_name = reasoner + "_" + "Count"

        if column_name in data.columns:
            data[new_column_name] =  np.where(np.isnan(data[column_name]), 0, 1)
            new_columns.append(new_column_name)

    data["Count"] = data[new_columns].sum(axis=1)

    new_data = data[["Ontology", "Count"]].copy()
    new_data = new_data.groupby(["Count"]).count().reset_index()
    new_data.columns = ["Number of Reasoner", "Number of Ontology"]

    return new_data
    #new_data.to_csv("test.csv")

def plot_bar_chart(data, ax, task):
    sns.set_color_codes("muted")
    g = sns.barplot(ax = ax, data=data, x = "Number of Reasoner", y = "Number of Ontology", color="b")
    g.set(ylabel=None)
    g.set(xlabel=None)
    g.set_title(task)

    g.tick_params(left=False, bottom=False)
    g.set(yticklabels=[])

if __name__ == '__main__':
    input_folder="./output/"
    output_folder="./output"

    Path(output_folder).mkdir(parents=True, exist_ok=True)

    reasoner_name = ["Factpp", "HermiT", "JFact", "Konclude", "KoncludeCLI", "Pellet", "Openllet"]
    task_name = ["Consistency", "Classification", "Realization"]

    #task_name = ["Consistency"]
    i = 0
    fig, axes = plt.subplots(1, 3, sharey=True, figsize=(11,4))

    for task in task_name:
        file_name = input_folder + task + "_mean.csv"
        data = load_analyze_csv(file_name)

        new_data = process_data(data, reasoner_name, task)

        plot_bar_chart(new_data, axes[i], task)
        show_values_on_bars(axes[i], space=20)
        i = i + 1

    axes[0].set(ylabel="Number of Ontologies")
    axes[1].set(xlabel="Number of Reasoners")



    #sns.despine(bottom=True, left=True)
    #sns.despine()
    plt.savefig(output_folder + "/count.pdf", bbox_inches='tight')
    plt.savefig(output_folder + "/count.png", bbox_inches='tight')
    plt.show()


