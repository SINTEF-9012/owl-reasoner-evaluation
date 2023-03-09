import numpy as np
import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt
import seaborn as sns
from os.path import exists
from pathlib import Path
from matplotlib.ticker import FormatStrFormatter
from matplotlib import ticker


def load_analyze_csv(file_name, filesize):
    data = pd.read_csv(file_name)

    filesize_data = pd.read_csv(filesize)

    data = data.merge(filesize_data, on="Ontology")

    data = data.drop(columns=["Ontology", "Profiles", "Unnamed: 12"])

    first_column = data.pop("FileSize")
    data.insert(0, "FileSize", first_column)

    columns = []

    for column_name in data.columns:
        if "Realization" in column_name or "Classification" in column_name or "Consistency" in column_name or "Loading" in column_name:
            new_name = column_name.split("_")[0]
            columns.append(new_name)
        else:
            columns.append(column_name)

    data.columns=columns

    #data = data.applymap(lambda x: 3600 if 'TO' in str(x) else x)
    #data = data.applymap(lambda x: np.nan if "MEM" in str(x) or "ERR" in str(x) or "IC" in str (x) else x)

    #data = data.astype(np.float)


    print(data)

    return data



def analyze_corr(input_folder, output_folder):
    Path(output_folder).mkdir(parents=True, exist_ok=True)

    reasoner_name = ["Factpp","HermiT",  "JFact", "Konclude", "KoncludeCLI", "Pellet" , "Openllet"]
    task_name = ["Loading", "Consistency", "Classification", "Realization"]

    fig_name = ["(a) ", "(b) ", "(c) ", "(d) "]

    #task_name = ["Loading"]

    i = 0
    j = 0
    k = 0

    fig, axes = plt.subplots(2, 2, figsize=(9, 12.2), sharey=True)

    cbar = False
    for task in task_name:

            filesize = input_folder + "/ORE2015_FileSize.csv"
            file_name = input_folder + task + "_mean.csv"
            data = load_analyze_csv(file_name, filesize)
            if task=="Loading":
                data["KoncludeCLI"] = np.nan
            print(data.columns)

            corr = data.corr()

            new_corr = corr.iloc[0:10,11:]
            #print(new_corr)
            new_corr = new_corr.drop(["RBox"], axis=0)
            new_corr = new_corr.round(2)
            #print(new_corr)


            #sns.set(font_scale=1.2)
            g = sns.heatmap(ax=axes[i][j], data=new_corr, annot=True, square=True, cmap="YlGnBu", cbar=cbar, robust=True, vmax=1)

            #if(i > 0):
            g.tick_params(left=False, bottom=False)

            axes[i][j].set_title(fig_name[k] + task)

            k = k + 1

            j = j + 1
            if j > 1:
                j = 0
                i = i + 1

    axes[1][0].set(xticklabels=[])
    axes[1][1].set(xticklabels=[])

    plt.savefig(output + "/corr.pdf", bbox_inches='tight')
    plt.show()




if __name__ == '__main__':
    input="./output/"
    output="./output"

    analyze_corr(input, output)




