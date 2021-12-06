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
    isNumeric = isNumeric.apply(lambda s: pd.to_numeric(s, errors='coerce').notnull().all(), axis=1)

    isNumeric = isNumeric.index[isNumeric].tolist()

    #isNumeric = isNumeric.dropna(how='all')
    #isNumeric = isNumeric.index.tolist()



    new_data = data.iloc[isNumeric].copy()

    return new_data

def process_data(data, reasoners, suffix):
    value_vars = []
    new_data = pd.DataFrame({"Index": range(1, len(data) + 1)})
    for reasoner in reasoners:
        column_name = reasoner + "_" + suffix
        if column_name in data.columns:
            data.sort_values([column_name], inplace=True)
            column_data = data[column_name].copy().reset_index(drop=True)
            #print(column_data)
            new_data[reasoner] = column_data
            value_vars.append(reasoner)
            #print(new_data)

    return new_data, value_vars

def plot_multi(data, value_vars, task, hue_order, ax):

    print(data)

    #data = data[data["Index"] > 1800]
    print(data.shape)

    stats_temp = pd.melt(data, id_vars=["Index"], value_vars=value_vars, value_name= task, var_name="Reasoner")


    #data.sort_values(["Pellet_Realization"], inplace=True)
    #value_vars.append("Ontology")

    #stats_temp = stats_temp.sample(n = 300)
    #stats_temp.sort_values(["JFact Mean"], inplace=True)
    #stats_temp = pd.melt(stats_temp, id_vars=["Ontology"], value_vars=value_vars, value_name= task + " Mean", var_name="Reasoner")
    #print(stats_temp)

    #sns.set_theme(style="whitegrid")

    ax.grid(False)
    sns.set_color_codes("dark")
    sns.color_palette("dark")

    g = sns.lineplot(ax=ax, data=stats_temp, x="Index", y = task, hue="Reasoner", hue_order=hue_order)


    g.set(yscale="log")
    #g.set(ylim=(0.0001, 1800))
    ax.yaxis.set_major_formatter(ticker.FormatStrFormatter("%g"))
    #plt.tick_params(axis='y', which='minor')
    #ax.yaxis.set_minor_formatter(FormatStrFormatter("%.1f"))
    ax.legend().set_visible(False)

    g.set(ylabel=None)
    g.set(xlabel=task)
    g.set(xticklabels=[])

    #plt.savefig(output_folder + "/" + task + "_mean.pdf")
    #plt.show()

def plot(data, value_vars, task, hue_order):

    print(data)

    #data = data[data["Index"] > 1800]
    print(data.shape)

    stats_temp = pd.melt(data, id_vars=["Index"], value_vars=value_vars, value_name= task, var_name="Reasoner")


    #data.sort_values(["Pellet_Realization"], inplace=True)
    #value_vars.append("Ontology")

    #stats_temp = stats_temp.sample(n = 300)
    #stats_temp.sort_values(["JFact Mean"], inplace=True)
    #stats_temp = pd.melt(stats_temp, id_vars=["Ontology"], value_vars=value_vars, value_name= task + " Mean", var_name="Reasoner")
    #print(stats_temp)

    #sns.set_theme(style="whitegrid")
    f, ax = plt.subplots()
    ax.grid(False)
    sns.color_palette("dark")
    #sns.set_color_codes("muted")


    g = sns.lineplot(data=stats_temp, x="Index", y = task, hue="Reasoner", hue_order=hue_order)
    #g.set(ylim=(10,None))

    g.set(yscale="log")
    ax.yaxis.set_major_formatter(ticker.FormatStrFormatter("%g"))
    #plt.tick_params(axis='y', which='minor')
    #ax.yaxis.set_minor_formatter(FormatStrFormatter("%.1f"))


    g.set(xlabel=None)
    g.set(xticklabels=[])

    #plt.savefig(output_folder + "/" + task + "_mean.pdf")
    plt.show()

def plot_mean_data():
    input_folder: str = "./output/"
    output_folder = "./output/"
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

            axes[i].set(ylim=(0.0001, 1800))

            i = i + 1


    sns.move_legend(axes[1], "lower center", bbox_to_anchor=(.5, 1), ncol=7, title=None, frameon=False)



    axes[1].set(yticklabels=[])
    axes[2].set(yticklabels=[])
    axes[3].set(yticklabels=[])

    plt.savefig(output_folder + "/mean.pdf")
    #plt.show()

def plot_mean_data_added_loading():
        input_folder: str = "./output/"
        output_folder = "./output/"
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

            axes[i].set(ylim=(0.001, 1800))

            i = i + 1

        sns.move_legend(axes[1], "lower center", bbox_to_anchor=(.5, 1), ncol=7, title=None, frameon=False)

        axes[1].set(yticklabels=[])
        axes[2].set(yticklabels=[])

        plt.savefig(output_folder + "/mean_loading_added.pdf")
        #plt.show()
        #break;

if __name__ == '__main__':
    plot_mean_data()
    plot_mean_data_added_loading()