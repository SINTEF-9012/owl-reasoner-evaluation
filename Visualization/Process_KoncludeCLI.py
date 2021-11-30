import numpy as np
import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt
import seaborn as sns
from os.path import exists
from pathlib import Path


def load_consistency_result(filename):
    data = pd.read_csv(filename, header=None)

    data = data.iloc[:, 0:2].copy()
    #print(data)
    data.columns = ["Ontology", "IsConsitency"]
    return data


def load_evaluation_csv(file_name):
    data = pd.read_csv(file_name, header=None)

    isNumeric = data.iloc[:, 1:11].copy()
    isNumeric = isNumeric.apply(lambda s: pd.to_numeric(s, errors='coerce').notnull().all(), axis=1)



    #print(isNumeric)
    isNumeric = isNumeric.index[isNumeric].tolist()


    timeout = data.applymap(lambda x: 'timeout' in str(x).lower() ).any(axis=1)
    timeout = data[timeout].copy()
    time_count = timeout.shape[0]




    new_data = data.iloc[isNumeric].copy()

    #print(isNumeric)
    return new_data


if __name__ == '__main__':
    ore2015_statistics_file:str = "C:\\Users\\anl\\SINTEF\\Skytrack@SINTEF - Documents\\General\\Task T1.3\\EvaluationResult\\ORE2015_Statistics.csv"
    input_folder: str = "C:\\Users\\anl\\SINTEF\\Skytrack@SINTEF - Documents\\General\\Task T1.3\\EvaluationResult\\ORE2015\\"
    output_folder = "./output/"

    columns = ["Ontology", "Run1", "Run2", "Run3", "Run4", "Run5", "Run6" ,"Run7" ,"Run8" ,"Run9" ,"Run10", "Mean", "Median"]

    reasoner = "KoncludeCLI"
    task_name = ["Consistency", "Classification", "Realization"]
    #task_name = ["Classification", "Realization"]

    filelist = {"Classification": "classification.list", "Realization":"realization.list"}

    constency_dict = {}

    for task in task_name:
        file_name = input_folder + reasoner + "_" + task + ".csv"

        full_data = pd.read_csv(file_name, header=None)
        full_data.columns = columns
        #print(full_data)

        success_data = load_evaluation_csv(file_name)
        success_data.columns = columns
        #print(success_data)

        unsuccess_data = full_data.merge(success_data, on="Ontology", how="left", indicator=True).query('_merge == "left_only"').drop('_merge', 1).iloc[:, 0:13].copy()
        unsuccess_data.columns = columns

        #print(unsuccess_data)

        if task=="Consistency":



            consistency_result_file = input_folder + reasoner + "_ConsistencyResult.csv"
            consitency_result = load_consistency_result(consistency_result_file)
            #print(consitency_result)
            constency_dict = pd.Series(consitency_result.IsConsitency.values, index=consitency_result.Ontology).to_dict()
            #print(constency_dict)

            new_success_data = success_data.merge(consitency_result, on="Ontology", how="inner").iloc[:, 0:13].copy()
            new_success_data.columns = columns
            malicious_data = success_data.merge(new_success_data, on = "Ontology", how="left", indicator=True).query('_merge == "left_only"').drop('_merge', 1).iloc[:, 0:13].copy()
            malicious_data.columns = columns

            firstCol=[]
            secondCol=[]
            for index, row in malicious_data.iterrows():
                firstCol.append(row['Ontology'])
                secondCol.append("Unexpected Error")
            new_mal_data = {"Ontology": firstCol, "Run1": secondCol}
            malicious_data = pd.DataFrame(new_mal_data)


            new_success_data = new_success_data.append(malicious_data, ignore_index=True)
            new_full_data = new_success_data.append(unsuccess_data, ignore_index=True)

            new_full_data.to_csv(output_folder + "/KoncludeCLI_" + task + ".csv", index=False, header=False)

            #print(new_full_data)

        else:
            list_successfull_ont = pd.read_csv(input_folder + filelist[task])
            list_successfull_ont.columns = ["Ontology"]

            new_success_data = success_data.merge(list_successfull_ont, on="Ontology", how="inner").iloc[:, 0:13].copy()
            new_success_data.columns = columns

            malicious_data = success_data.merge(new_success_data, on="Ontology", how="left", indicator=True).query('_merge == "left_only"').drop('_merge', 1).iloc[:, 0:13].copy()
            malicious_data.columns = columns

            firstCol = []
            secondCol = []
            for index, row in malicious_data.iterrows():
                firstCol.append(row['Ontology'])
                if row['Ontology'] in constency_dict and not constency_dict[row['Ontology']]:
                    secondCol.append("InconsistentOntology")
                else:
                    secondCol.append("Unexpected Error")

            new_mal_data = {"Ontology": firstCol, "Run1": secondCol}
            malicious_data = pd.DataFrame(new_mal_data)

            new_success_data = new_success_data.append(malicious_data, ignore_index=True)
            new_full_data = new_success_data.append(unsuccess_data, ignore_index=True)

            new_full_data.to_csv(output_folder + "/KoncludeCLI_" + task + ".csv", index=False, header=False)

            #print(new_full_data)



