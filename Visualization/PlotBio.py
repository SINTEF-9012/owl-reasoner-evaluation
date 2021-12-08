import numpy as np
import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt
import seaborn as sns
from os.path import exists
from pathlib import Path
from matplotlib.ticker import FormatStrFormatter
from matplotlib import ticker


def load_analyze_csv(file_name):
    data = pd.read_csv(file_name)

    isNumeric = data.iloc[:, 13:].copy()
    #isNumeric = isNumeric.apply(lambda s: pd.to_numeric(s, errors='coerce').notnull().all(), axis=1)

    #isNumeric = isNumeric.index[isNumeric].tolist()

    isNumeric = isNumeric.dropna(how='all')
    isNumeric = isNumeric.index.tolist()



    new_data = data.iloc[isNumeric].copy()

    new_data = new_data.apply(lambda x: x.str[0:-4].str.upper() if x.name in ["Ontology"] else x)

    return new_data

def process_data(data, reasoners, suffix):
    value_vars = []

    old_columns = ["Ontology"]
    new_columns = ["Ontology"]

    for reasoner in reasoners:
        column_name = reasoner + "_" + suffix
        if column_name in data.columns:
            #data.sort_values([column_name], inplace=True)
            #column_data = data[column_name].copy().reset_index(drop=True)
            #print(column_data)
            old_columns.append(column_name)
            value_vars.append(reasoner)
            new_columns.append(reasoner)
            #print(new_data)
    new_data = data[old_columns].copy()
    new_data.columns = new_columns
    return new_data, value_vars

def plot_multi(data, value_vars, task, hue_order, ax):

    print(data)

    #data = data[data["Index"] > 1800]
    #print(data.shape)

    stats_temp = pd.melt(data, id_vars=["Ontology"], value_vars=value_vars, value_name= task, var_name="Reasoner")


    data.sort_values(["Konclude"], inplace=True)
    #value_vars.append("Ontology")

    #stats_temp = stats_temp.sample(n = 300)
    #stats_temp.sort_values(["JFact Mean"], inplace=True)
    #stats_temp = pd.melt(stats_temp, id_vars=["Ontology"], value_vars=value_vars, value_name= task + " Mean", var_name="Reasoner")
    #print(stats_temp)

    #sns.set_theme(style="whitegrid")

    ax.grid(False)
    #sns.set_color_codes("dark")
    #sns.color_palette("dark")

    g = sns.scatterplot(ax=ax, data=stats_temp, x="Ontology", y = task, hue="Reasoner", hue_order=hue_order)


    g.set(yscale="log")
    #g.set(ylim=(0.0001, 1800))
    ax.yaxis.set_major_formatter(ticker.FormatStrFormatter("%g"))
    #plt.tick_params(axis='y', which='minor')
    #ax.yaxis.set_minor_formatter(FormatStrFormatter("%.1f"))
    ax.legend().set_visible(False)

    g.set(ylabel=None)
    g.set_title(task)
    g.set(xlabel=None)
    #g.set(xticklabels=[])
    #g.tick_params(bottom=False)

    #plt.savefig(output_folder + "/" + task + "_mean.pdf")
    #plt.show()


def plot_mean_data(input_folder, output_folder):
    Path(output_folder).mkdir(parents=True, exist_ok=True)

    reasoner_name = ["Factpp","HermiT",  "JFact", "Konclude", "KoncludeCLI", "Pellet" , "Openllet"]
    task_name = ["Loading", "Consistency", "Classification", "Realization"]

    #task_name = ["Loading"]

    i = 0
    fig, axes = plt.subplots(1, 4, figsize=(11,5))

    for task in task_name:
            file_name = input_folder + task + "_mean.csv"
            data = load_analyze_csv(file_name)
            #print(data)
            new_data, value_vars = process_data(data, reasoner_name, task)


            plot_multi(new_data, value_vars, task, reasoner_name, axes[i])

            #axes[i].set(ylim=(0.0001, 1800))

            i = i + 1


    sns.move_legend(axes[1], "upper center",  bbox_to_anchor=(1, -0.05), ncol=7, title=None, frameon=False)



    axes[1].set(yticklabels=[])
    axes[2].set(yticklabels=[])
    axes[3].set(yticklabels=[])

    axes[0].set(ylabel="Reasoning Time (seconds)")
    #axes[1].set(xlabel="Number of Reasoners")

    plt.savefig(output_folder + "/mean.pdf", bbox_inches='tight')
    plt.savefig(output_folder + "/mean.png", bbox_inches='tight')
    plt.show()

def plot_mean_data_added_loading(input_folder, output_folder):

        Path(output_folder).mkdir(parents=True, exist_ok=True)

        reasoner_name = ["Factpp", "HermiT", "JFact", "Konclude", "KoncludeCLI", "Pellet", "Openllet"]
        task_name = ["Consistency", "Classification", "Realization"]

        # task_name = ["Loading"]

        i = 0
        fig, axes = plt.subplots(1, 3, figsize=(11, 5))

        for task in task_name:
            file_name = input_folder + task + "_mean_LoadingAdded.csv"
            data = load_analyze_csv(file_name)
            # print(data)
            new_data, value_vars = process_data(data, reasoner_name, task + "_LoadingAdded")

            plot_multi(new_data, value_vars, task, reasoner_name, axes[i])

            #axes[i].set(ylim=(0.001, 1800))

            i = i + 1

        sns.move_legend(axes[1], "upper center", bbox_to_anchor=(0.5, 0), ncol=7, title=None, frameon=False)

        axes[1].set(yticklabels=[])
        axes[2].set(yticklabels=[])

        axes[0].set(ylabel="Reasoning Time (seconds)")

        plt.savefig(output_folder + "/mean_loading_added.pdf", bbox_inches='tight')
        plt.savefig(output_folder + "/mean_loading_added.png", bbox_inches='tight')
        #plt.show()
        #break;


def plot_mean_data_added_all_loading(input_folder, output_folder):

    Path(output_folder).mkdir(parents=True, exist_ok=True)

    reasoner_name = ["Factpp", "HermiT", "JFact", "Konclude", "KoncludeCLI", "Pellet", "Openllet"]
    task_name = ["Consistency", "Classification", "Realization"]



    i = 0
    fig, axes = plt.subplots(1, 3, figsize=(11, 5))



    for task in task_name:
        file_name = input_folder + task + "_mean_AllLoadingAdded.csv"
        data = load_analyze_csv(file_name)
        # print(data)
        new_data, value_vars = process_data(data, reasoner_name, task + "_AllLoadingAdded")

        plot_multi(new_data, value_vars, task, reasoner_name, axes[i])

        #axes[i].set(ylim=(0.001, 1800))

        i = i + 1

    sns.move_legend(axes[1], "upper center", bbox_to_anchor=(.5, 0), ncol=7, title=None, frameon=False)

    axes[1].set(yticklabels=[])
    axes[2].set(yticklabels=[])

    axes[0].set(ylabel="Reasoning Time (seconds)")

    plt.savefig(output_folder + "/mean_all_loading_added.pdf", bbox_inches='tight')
    plt.savefig(output_folder + "/mean_all_loading_added.png", bbox_inches='tight')


if __name__ == '__main__':
    input="./output/Bio/"
    output="./output/Bio/"

    #input="./output/Bio/"
    #output="./output/Bio/"

    plot_mean_data(input, output)
    #plot_mean_data_added_loading(input, output)
    #plot_mean_data_added_all_loading(input, output)
