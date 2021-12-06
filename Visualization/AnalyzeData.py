import numpy as np
import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt
import seaborn as sns
from os.path import exists
from pathlib import Path

from scipy.constants import value


def change_width(ax, new_value) :
    for patch in ax.patches :
        current_width = patch.get_width()
        diff = current_width - new_value

        # we change the bar width
        patch.set_width(new_value)

        # we recenter the bar
        patch.set_x(patch.get_x() + diff * .5)


def load_consistency_result(filename):
    data = pd.read_csv(filename, header=None)

    data = data.iloc[:, 0:2].copy()
    #print(data)
    data.columns = ["Ontology", "IsConsitency"]
    return data

def load_evaluation_csv(file_name):
    columns = ["Ontology", "Run1", "Run2", "Run3", "Run4", "Run5", "Run6", "Run7", "Run8", "Run9", "Run10", "Mean",
               "Median"]


    data = pd.read_csv(file_name, header=None, names=columns)

    total = data.shape[0]

    isNumeric = data.iloc[:, 1:11].copy()
    isNumeric = isNumeric.apply(lambda s: pd.to_numeric(s, errors='coerce').notnull().all(), axis=1)


    isNumeric = isNumeric.index[isNumeric].tolist()


    timeout = data.applymap(lambda x: 'timeout' in str(x).lower() ).any(axis=1)
    timeout = data[timeout].copy()
    time_count = timeout.shape[0]


    inconstent = data.applymap(lambda x: 'inconsistentontology' in str(x).lower()).any(axis=1)
    inconstent = data[inconstent].copy()
    inconstent_count = inconstent.shape[0]

    mem = data.applymap(lambda x: 'outofmemory' in str(x).lower()).any(axis=1)
    mem = data[mem].copy()
    mem_count = mem.shape[0]

    new_data = data.iloc[isNumeric].copy()

    return new_data, total, time_count, inconstent_count, mem_count

if __name__ == '__main__':
    stat_csv_file = "C:\\Users\\anl\\SINTEF\\Skytrack@SINTEF - Documents\\General\\Task T1.3\\EvaluationResult\\ORE2015_Statistics.csv"
    input_folder: str = "C:\\Users\\anl\\SINTEF\\Skytrack@SINTEF - Documents\\General\\Task T1.3\\EvaluationResult\\ORE2015\\"
    output_folder = "./output/"

    #stat_csv_file = "C:\\Users\\anl\\SINTEF\\Skytrack@SINTEF - Documents\\General\\Task T1.3\\EvaluationResult\\BioOntology_Statistics.csv"
    ##output_folder = "./output/Bio"

    Path(output_folder).mkdir(parents=True, exist_ok=True)
    reasoner_name = ["Factpp", "HermiT", "JFact", "Konclude", "Openllet", "Pellet", "KoncludeCLI"]
    task_name = [ "Loading", "Consistency", "Classification", "Realization"]





    stats = pd.read_csv(stat_csv_file)

    loading_data = stats.copy().rename(columns={"Statistics": "Ontology"})


    data_map = {}

    for task in task_name:
        task_map = {}
        task_success = []
        task_other_error = []
        task_timeout=[]
        task_total = []
        task_mean = []
        task_sum = []
        task_consistent = []
        resoners = []
        #value_vars = []
        task_inconsistent_error = []
        task_mem = []
        task_total_fail = []

        stats_temp = stats.copy().rename(columns={"Statistics": "Ontology"})



        #print(stats_temp)
        for reasoner in reasoner_name:
            file_name = input_folder + reasoner + "_" + task + ".csv"

            if not exists(file_name):
                print(file_name + " not exist")
                continue

            print(file_name)

            resoners.append(reasoner)

            data, total, timeout, inconsistent_error_count, mem = load_evaluation_csv(file_name)
            success = data.shape[0]

            total_fail = total - success

            other_error = total - success - timeout

            if task == "Classification" or task == "Realization":
                other_error = other_error - inconsistent_error_count

            other_error = other_error - mem

            task_success.append(success)
            task_total_fail.append(total_fail)
            task_other_error.append(other_error)
            task_total.append(total)
            task_timeout.append(timeout)
            task_inconsistent_error.append(inconsistent_error_count)
            task_mem.append(mem)
            task_map[reasoner] = data


            mean = data.iloc[:, 11].mean()
            sum = data.iloc[:, 11].sum()
            task_mean.append(mean)
            task_sum.append(sum)


            data_temp = data.iloc[:, [0,11]].copy()
            data_temp.columns= ["Ontology", reasoner + "_" + task]
            #print(data_temp)
            stats_temp = stats_temp.merge(data_temp, on="Ontology", how="outer")
            stats_temp.rename(columns={stats_temp.shape[1] - 1: reasoner + "_" + task}, inplace=True)
            #value_vars.append(reasoner + " Mean")


            if(task == "Consistency"):
                consistency_result_file = input_folder + reasoner + "_ConsistencyResult.csv"
                consitency_result = load_consistency_result(consistency_result_file)
                consitency_result = consitency_result.merge(data_temp, on="Ontology", how="inner").iloc[:, 0:2].copy()

                consitency_result = consitency_result.groupby(by="IsConsitency").count().reset_index()
                consitency_result = consitency_result[consitency_result['IsConsitency'] == False]

                if(consitency_result.empty):
                    task_consistent.append(0)
                else:
                    task_consistent.append(consitency_result.iat[0, 1])

        stats_temp.to_csv(output_folder + "/" + task + "_mean.csv", index=False)


        if task == "Loading":
            extract_column_names = ["Ontology"]
            for reasoner in reasoner_name:
                loading_column_name = reasoner + "_Loading"
                if loading_column_name in stats_temp.columns:
                    extract_column_names.append(loading_column_name)

            loading_data = stats_temp.loc[:, extract_column_names].copy()
        else:
            stats_temp_copy = stats_temp.copy().merge(loading_data, on="Ontology", how="inner")

            for reasoner in reasoner_name:
                task_column_name = reasoner + "_" + task
                loading_column_name =  reasoner + "_Loading"
                new_column_name = reasoner + "_" + task + "_LoadingAdded"
                if loading_column_name in stats_temp_copy.columns and task_column_name in stats_temp_copy.columns:
                    stats_temp_copy[new_column_name] = stats_temp_copy[task_column_name] + stats_temp_copy[loading_column_name]

                if reasoner == "KoncludeCLI":
                    stats_temp_copy[new_column_name] = stats_temp_copy[task_column_name]

            stats_temp_copy.to_csv(output_folder + "/" + task + "_mean_LoadingAdded.csv", index=False)





        data_map[task] = task_map

        temp_data = {"Reasoners": resoners, "Success" : task_success, "Total": task_total, "OutOfMemory": task_mem, "Timeout": task_timeout}



        if task == "Classification" or task == "Realization":
            temp_data["Inconsistent Error"] = task_inconsistent_error

        temp_data["Other Error"] = task_other_error

        temp_data["Total Error"] = task_total_fail

        if (task == "Consistency"):
            temp_data["Inconstent Ontologies"] = task_consistent

        temp_data["Mean"] = task_mean
        temp_data["Sum"] = task_sum


        df = pd.DataFrame(temp_data)
        df = df.sort_values(['Success', "Mean"], ascending=[False, True])

        df.to_csv(output_folder + "/" + task + ".csv", index=False)

        print(df)





        # ## Draw
        # sns.set_theme(style="whitegrid")
        # f, ax = plt.subplots()
        #
        # sns.set_color_codes("muted")
        # sns.barplot(x="Reasoners", y="Total", data=df,
        #             label="Total", color="r", ax=ax)
        #
        # sns.barplot(x="Reasoners", y="Success", data=df,
        #             label="Success", color="g", dodge=False, ax=ax)
        #
        # #change_width(ax, 0.3)
        # ax.grid(False)
        # ax2 = ax.twinx()
        # ax2.grid(False)
        # sns.lineplot(x="Reasoners", y="Sum", data=df, marker='o', ax=ax2, sort=False, label="Sum")
        # plt.savefig(output_folder + "/" + task + ".pdf")

        ##Draw
        # stats_temp.sort_values(["Axioms"], inplace=True)
        # value_vars.append("Ontology")
        #
        # stats_temp = stats_temp.sample(n = 300)
        # stats_temp.sort_values(["JFact Mean"], inplace=True)
        # #stats_temp = pd.melt(stats_temp, id_vars=["Ontology"], value_vars=value_vars, value_name= task + " Mean", var_name="Reasoner")
        # print(stats_temp)
        #
        # sns.set_theme(style="whitegrid")
        # f, ax = plt.subplots()
        # ax.grid(False)
        # sns.set_color_codes("muted")
        #
        #
        # g = sns.scatterplot(data=stats_temp, x = "Ontology", y = "HermiT Mean")
        # #g.set(yscale="log")
        # g.set(xlabel=None)
        # g.set(xticklabels=[])
        #
        # #plt.savefig(output_folder + "/" + task + "_mean.pdf")
        # plt.show(block=True)


        #break;



