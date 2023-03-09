import numpy as np
import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt
import seaborn as sns
from os.path import exists
from pathlib import Path
from matplotlib.ticker import FormatStrFormatter
from matplotlib import ticker

def is_float(x):
    try:
        float(x)
    except ValueError:
        return False
    return True

def load_analyze_csv(file_name):
    data = pd.read_csv(file_name)

    isNumeric = data.iloc[:, 13:].copy()


    #isNumeric = isNumeric.apply(lambda s: pd.to_numeric(s, errors='coerce').notnull().all(), axis=1)
    #isNumeric = isNumeric.index[isNumeric].tolist()

    isNumeric = isNumeric.dropna(how='any')
    isNumeric = isNumeric.index.tolist()



    new_data = data.iloc[isNumeric].copy()

    #new_data = new_data.sort_values(by="Factpp")

    return new_data


def load_analyze_csv_combined_error(file_name):
    data = pd.read_csv(file_name)

    #isNumeric = data.iloc[:, 13:].copy()


    #isNumeric = isNumeric.apply(lambda s: pd.to_numeric(s, errors='coerce').notnull().all(), axis=1)
    #isNumeric = isNumeric.index[isNumeric].tolist()

    #data = data.applymap(lambda x: 3600 if 'TO' in str(x) else x)
    #data = data.applymap(lambda x: np.nan if "MEM" in str(x) or "ERR" in str(x) or "IC" in str (x) else x)

    data = data.applymap(lambda x: np.nan if not is_float(x) else x)

    #data = data.astype(np.float)



    #isNumeric = isNumeric.dropna(how='all')
    #isNumeric = isNumeric.index.tolist()



    #new_data = data.iloc[isNumeric].copy()

    new_data = data.sort_values(by="Axioms")

    new_data = new_data.iloc[:, 13:].copy()
    new_data = new_data.dropna(how="all")

    new_data = new_data.astype(np.float64)


    #print(new_data)


    new_data["Mean"] = new_data.mean(skipna=True, axis=1)


    #new_data = new_data.sort_values(by="Mean")
    new_data = new_data.drop("Mean", axis=1)


    print(new_data)


    return new_data

def process_data(data, reasoners, suffix):
    value_vars = []
    new_data = pd.DataFrame()
    for reasoner in reasoners:
        column_name = reasoner + "_" + suffix
        if column_name in data.columns:
            data.sort_values([column_name], inplace=True)
            column_data = data[column_name].copy().reset_index(drop=True)
            #print(column_data)
            new_data[reasoner] = column_data
            value_vars.append(reasoner)
            #print(new_data)
            #new_data.drop("Index")


    return new_data, value_vars

def process_data_2(data, reasoners, suffix):
    value_vars = []
    new_data = pd.DataFrame()
    for reasoner in reasoners:
        column_name = reasoner + "_" + suffix
        if column_name in data.columns:
            #data.sort_values([column_name], inplace=True)
            column_data = data[column_name].copy().reset_index(drop=True)
            #print(column_data)
            new_data[reasoner] = column_data
            value_vars.append(reasoner)
            #print(new_data)
            #new_data.drop("Index")


    return new_data, value_vars


def plot_multi(data, task, hue_order, ax):

    #data.drop(columns=["Index"])
    #print(data)

    #data = data[data["Index"] > 1800]
    #print(data.shape)

    #stats_temp = pd.melt(data, id_vars=["Index"], value_vars=value_vars, value_name= task, var_name="Reasoner")


    #data.sort_values(["Pellet_Realization"], inplace=True)
    #value_vars.append("Ontology")

    #stats_temp = stats_temp.sample(n = 300)
    #stats_temp.sort_values(["JFact Mean"], inplace=True)
    #stats_temp = pd.melt(stats_temp, id_vars=["Ontology"], value_vars=value_vars, value_name= task + " Mean", var_name="Reasoner")
    #print(stats_temp)

    #sns.set_theme(style="whitegrid")

    ax.grid(False)
    #sns.set_color_codes("dark")
    #sns.color_palette("dark")

    #g = sns.lineplot(ax=ax, data=stats_temp, x="Index", y = task, hue="Reasoner", hue_order=hue_order)

    g = sns.lineplot(ax=ax, data=data, hue_order=hue_order, linewidth=0.3)


    g.set(yscale="log")
    #g.set(ylim=(0.0001, 1800))
    ax.yaxis.set_major_formatter(ticker.FormatStrFormatter("%g"))
    #plt.tick_params(axis='y', which='minor')
    #ax.yaxis.set_minor_formatter(FormatStrFormatter("%.1f"))
    ax.legend().set_visible(False)

    g.set(ylabel=None)
    if task is not None:
        g.set_title(task)
    g.set(xlabel=None)
    g.set(xticklabels=[])
    g.tick_params(bottom=False)

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

def plot_mean_data(input_folder, output_folder):
    Path(output_folder).mkdir(parents=True, exist_ok=True)

    reasoner_name = ["Factpp","HermiT",  "JFact", "Konclude", "KoncludeCLI", "Pellet" , "Openllet"]
    task_name = ["Loading", "Consistency", "Classification", "Realization"]

    fig_name = ["(a) ", "(b) ", "(c) ", "(d) "]

    #task_name = ["Loading"]
    k = 0
    i = 0
    j = 0
    fig, axes = plt.subplots(2, 2, figsize=(6.5,7))

    for task in task_name:
            file_name = input_folder + task + "_mean.csv"
            data = load_analyze_csv(file_name)
            #print(data)
            new_data, value_vars = process_data(data, reasoner_name, task)
            #new_data = data

            plot_multi(new_data, fig_name[k] + task, reasoner_name, axes[i][j])
            #plt.figure(i)


            axes[i][j].set(ylim=(0.0001, 1800))

            k = k + 1
            j = j + 1
            if j > 1:
                j = 0
                i = i + 1


    sns.move_legend(axes[1][0], "upper center",  bbox_to_anchor=(1, 0), ncol=7, title=None, frameon=False)



    #axes[1].set(yticklabels=[])
    axes[0][1].set(yticklabels=[])
    axes[1][1].set(yticklabels=[])

    axes[0][0].set(ylabel="Reasoning Time (seconds)")
    axes[1][0].set(ylabel="Reasoning Time (seconds)")
    #axes[1].set(xlabel="Number of Reasoners")

    plt.savefig(output_folder + "/mean.pdf", bbox_inches='tight')
    plt.savefig(output_folder + "/mean.png", bbox_inches='tight')
    #plt.show()

def plot_mean_data_2(input_folder, output_folder):
    Path(output_folder).mkdir(parents=True, exist_ok=True)

    #reasoner_name = ["Factpp","HermiT",  "JFact", "Konclude", "KoncludeCLI", "Pellet" , "Openllet"]
    reasoner_name = ["Factpp", "HermiT", "JFact", "Konclude", "Pellet", "Openllet"]
    task_name = ["Loading", "Consistency", "Classification", "Realization"]

    fig_name = ["(a) ", "(b) ", "(c) ", "(d) "]

    #task_name = ["Loading"]
    k = 0
    i = 0
    j = 0
    fig, axes = plt.subplots(2, 2, figsize=(9,9.5))

    for task in task_name:
            file_name = input_folder + task + "_mean_combine_error.csv"

            print(file_name)

            data = load_analyze_csv_combined_error(file_name)
            #print(data)
            #new_data, value_vars = process_data(data, reasoner_name, task)

            if "KoncludeCLI" in data.columns:
                data = data.drop("KoncludeCLI", axis=1)

            plot_multi(data, fig_name[k] + task, reasoner_name, axes[i][j])
            #plt.figure(i)


            axes[i][j].set(ylim=(0.0001, 1800))

            k = k + 1
            j = j + 1
            if j > 1:
                j = 0
                i = i + 1


    sns.move_legend(axes[1][0], "upper center",  bbox_to_anchor=(1, 0), ncol=7, title=None, frameon=False)



    #axes[1].set(yticklabels=[])
    axes[0][1].set(yticklabels=[])
    axes[1][1].set(yticklabels=[])

    axes[0][0].set(ylabel="Reasoning Time (seconds)")
    axes[1][0].set(ylabel="Reasoning Time (seconds)")
    #axes[1].set(xlabel="Number of Reasoners")

    plt.savefig(output_folder + "/mean_2.pdf", bbox_inches='tight')
    plt.savefig(output_folder + "/mean_2.png", bbox_inches='tight')


def plot_mean_data_3(input_folder, output_folder):
    Path(output_folder).mkdir(parents=True, exist_ok=True)

    #reasoner_name = ["Factpp","HermiT",  "JFact", "Konclude", "KoncludeCLI", "Pellet" , "Openllet"]
    reasoner_name = ["Factpp", "HermiT", "JFact", "Konclude", "Pellet", "Openllet"]
    task_name = ["Loading", "Consistency", "Classification", "Realization"]

    fig_name = ["(a) ", "(b) ", "(c) ", "(d) "]

    #task_name = ["Loading"]
    k = 0
    fig, axes = plt.subplots(4, 1, figsize=(9,8))
    plt.subplots_adjust(hspace = 0.02)

    for task in task_name:
            file_name = input_folder + task + "_mean_combine_error.csv"

            print(file_name)

            data = load_analyze_csv_combined_error(file_name)
            #print(data)
            #new_data, value_vars = process_data(data, reasoner_name, task)

            if "KoncludeCLI" in data.columns:
                data = data.drop("KoncludeCLI", axis=1)

            plot_multi(data, None, reasoner_name, axes[k])
            #plot_multi(data, fig_name[k] + task, reasoner_name, axes[k])
            #plt.figure(i)

            axes[k].set(ylabel= fig_name[k] + task)
            axes[k].set(ylim=(0.0001, 1800))

            k = k + 1



    sns.move_legend(axes[3], "upper center",  bbox_to_anchor=(0.5, 0), ncol=7, title=None, frameon=False)



    #axes[1].set(yticklabels=[])
    #axes[0][1].set(yticklabels=[])
    #axes[1][1].set(yticklabels=[])

    #axes[2].set(ylabel="Reasoning Time (seconds)")
    #axes[1][0].set(ylabel="Reasoning Time (seconds)")
    #axes[1].set(xlabel="Number of Reasoners")

    #plt.show()

    plt.savefig(output_folder + "/mean_3.pdf", bbox_inches='tight')
    plt.savefig(output_folder + "/mean_3.png", bbox_inches='tight')


def plot_mean_data_added_loading(input_folder, output_folder):

        Path(output_folder).mkdir(parents=True, exist_ok=True)

        reasoner_name = ["Factpp", "HermiT", "JFact", "Konclude", "KoncludeCLI", "Pellet", "Openllet"]
        task_name = ["Consistency", "Classification", "Realization"]
        fig_name = ["(a) ", "(b) ", "(c) ", "(d) "]

        # task_name = ["Loading"]

        i = 0
        fig, axes = plt.subplots(1, 3, figsize=(8, 5))

        for task in task_name:
            file_name = input_folder + task + "_mean_LoadingAdded.csv"
            data = load_analyze_csv(file_name)
            # print(data)
            new_data, value_vars = process_data(data, reasoner_name, task + "_LoadingAdded")

            plot_multi(new_data, fig_name[i] + task, reasoner_name, axes[i])

            axes[i].set(ylim=(0.001, 1800))

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
    fig_name = ["(a) ", "(b) ", "(c) ", "(d) "]


    i = 0
    fig, axes = plt.subplots(1, 3, figsize=(11, 3.5))



    for task in task_name:
        file_name = input_folder + task + "_mean_AllLoadingAdded.csv"
        data = load_analyze_csv(file_name)
        # print(data)
        new_data, value_vars = process_data(data, reasoner_name, task + "_AllLoadingAdded")

        plot_multi(new_data, fig_name[i]  + task, reasoner_name, axes[i])

        axes[i].set(ylim=(0.001, 1800))

        i = i + 1

    sns.move_legend(axes[1], "upper center", bbox_to_anchor=(.5, 0), ncol=7, title=None, frameon=False)

    axes[1].set(yticklabels=[])
    axes[2].set(yticklabels=[])

    axes[0].set(ylabel="Reasoning Time (seconds)")

    plt.savefig(output_folder + "/mean_all_loading_added.pdf", bbox_inches='tight')
    plt.savefig(output_folder + "/mean_all_loading_added.png", bbox_inches='tight')

def plot_mean_data_added_all_loading_2(input_folder, output_folder):

    Path(output_folder).mkdir(parents=True, exist_ok=True)

    reasoner_name = ["Factpp", "HermiT", "JFact", "Konclude", "KoncludeCLI", "Pellet", "Openllet"]
    task_name = ["Consistency", "Classification", "Realization"]
    fig_name = ["(a) ", "(b) ", "(c) ", "(d) "]


    i = 0
    fig, axes = plt.subplots(1, 3, figsize=(11, 3.5))



    for task in task_name:
        file_name = input_folder + task + "_mean_AllLoadingAdded.csv"
        data = load_analyze_csv_combined_error(file_name)

        new_data, value_vars = process_data_2(data, reasoner_name, task + "_AllLoadingAdded")

        print(new_data)

        plot_multi(new_data, fig_name[i]  + task, reasoner_name, axes[i])

        axes[i].set(ylim=(0.001, 1800))

        i = i + 1

    sns.move_legend(axes[1], "upper center", bbox_to_anchor=(.5, 0), ncol=7, title=None, frameon=False)

    axes[1].set(yticklabels=[])
    axes[2].set(yticklabels=[])

    axes[0].set(ylabel="Reasoning Time (seconds)")

    plt.savefig(output_folder + "/mean_all_loading_added_2.pdf", bbox_inches='tight')
    plt.savefig(output_folder + "/mean_all_loading_added_2.png", bbox_inches='tight')

if __name__ == '__main__':
    input="./output/"
    output="./output"

    #input="./output/Bio/"
    #output="./output/Bio/"
    plot_mean_data_3(input, output)
    #plot_mean_data_added_all_loading_2(input, output)

    #plot_mean_data(input, output)
    #plot_mean_data_added_loading(input, output)
    #plot_mean_data_added_all_loading(input, output)
