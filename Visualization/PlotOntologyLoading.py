import numpy as np
import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt
import seaborn as sns
from os.path import exists
from pathlib import Path
from matplotlib.ticker import FormatStrFormatter
from matplotlib import ticker

def process_ontoloy_loading_data(file_name):
    columns = ["Ontology", "Run1", "Run2", "Run3", "Run4", "Run5", "Run6", "Run7", "Run8", "Run9", "Run10", "Mean",
               "Median"]


    data = pd.read_csv(file_name, header=None, names=columns)

    total = data.shape[0]

    isNumeric = data.iloc[:, 1:11].copy()
    isNumeric = isNumeric.apply(lambda s: pd.to_numeric(s, errors='coerce').notnull().all(), axis=1)

    isNumeric = isNumeric.index[isNumeric].tolist()

    flatten_data = data.iloc[isNumeric].sort_values(by="Mean").copy()

    flatten_data = flatten_data.iloc[:, 0:11].copy()

    value_vars = ["Run1", "Run2", "Run3", "Run4", "Run5", "Run6", "Run7", "Run8", "Run9", "Run10"]
    flatten_data = pd.melt(flatten_data, id_vars="Ontology", value_vars=value_vars, value_name="Loading Time")

    #new_data.to_csv("test_ont_loading.csv")

    mean_data = data.iloc[isNumeric].copy()
    mean_data = mean_data[["Ontology", "Mean"]].sort_values(by="Mean").copy()
    mean_data.columns = ["Ontology", "Loading Time"]

    return flatten_data, mean_data




if __name__ == '__main__':
    input_folder: str = "C:\\Users\\anl\\SINTEF\\Skytrack@SINTEF - Documents\\General\\Task T1.3\\EvaluationResult\\ORE2015\\"
    output_folder = "./output"
    bio=False

    #input_folder: str = "C:\\Users\\anl\\SINTEF\\Skytrack@SINTEF - Documents\\General\\Task T1.3\\EvaluationResult\\Bio\\"
    #output_folder = "./output/Bio"
    #bio=True

    ontology_loading_file = input_folder + "Ontology_Loading.csv"

    flatten_data, mean_data = process_ontoloy_loading_data(ontology_loading_file)

    if bio==True:
        #sns.set_theme()
        #sns.set_style("whitegrid")
        mean_data = mean_data.apply(lambda x: x.str[0:-4].str.upper() if x.name in ["Ontology"] else x)

    ax = sns.lineplot(data=mean_data, x = "Ontology", y = "Loading Time")

    if bio==False:
        ax.tick_params(bottom=False)
        ax.set(xticklabels=[])
        ax.set(xlabel="Ontologies")


    if bio==True:
        ax.xaxis.grid(True)
        ax.yaxis.grid(True)
        ax.tick_params(axis='x', rotation=30, labelsize=7)


    ax.set(ylabel="Loading Time (seconds)")

    plt.savefig(output_folder + "/ontology_loading.pdf", bbox_inches='tight')
    plt.savefig(output_folder + "/ontology_loading.png", bbox_inches='tight')
    plt.show()


