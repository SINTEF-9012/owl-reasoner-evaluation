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

def process_data(data, reasoners, task):
    value_vars = []
    new_data = pd.DataFrame({"Index": range(1, len(data) + 1)})
    for reasoner in reasoners:
        column_name = reasoner + "_" + task
        if column_name in data.columns:
            data.sort_values([column_name], inplace=True)
            column_data = data[column_name].copy().reset_index(drop=True)
            #print(column_data)
            new_data[reasoner] = column_data
            value_vars.append(reasoner)
            #print(new_data)

    return new_data, value_vars

def plot(data, value_vars, task):

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

    sns.set_theme(style="whitegrid")
    f, ax = plt.subplots()
    ax.grid(False)
    #sns.set_color_codes("muted")


    g = sns.lineplot(data=stats_temp, x="Index", y = task, hue="Reasoner")
    #g.set(ylim=(10,None))

    g.set(yscale="log")
    ax.yaxis.set_major_formatter(ticker.FormatStrFormatter("%g"))
    #plt.tick_params(axis='y', which='minor')
    #ax.yaxis.set_minor_formatter(FormatStrFormatter("%.1f"))


    g.set(xlabel=None)
    g.set(xticklabels=[])

    #plt.savefig(output_folder + "/" + task + "_mean.pdf")
    plt.show()

if __name__ == '__main__':
    input_folder: str = "./output/"
    output_folder = "./output/"
    Path(output_folder).mkdir(parents=True, exist_ok=True)
    reasoner_name = ["Factpp", "HermiT", "JFact", "Konclude", "Openllet", "Pellet", "KoncludeCLI"]
    task_name = ["Loading", "Consistency", "Classification", "Realization"]

    task_name = ["Consistency"]

    for task in task_name:
            file_name = input_folder + task + "_mean.csv"
            data = load_analyze_csv(file_name)
            #print(data)
            new_data, value_vars = process_data(data, reasoner_name, task)
            plot(new_data, value_vars, task)
        #break;

