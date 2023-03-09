import os

import numpy as np
import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt
import seaborn as sns
from os.path import exists
from pathlib import Path
from matplotlib.ticker import FormatStrFormatter
from matplotlib import ticker



if __name__ == '__main__':

    ore2015_folder = "D:\\SkyTrack\\workspace\\ore2015_sample\\pool_sample\\files\\"
    ore2015_output_folder = "./output/"

    bio_folder = "D:\\SkyTrack\\workspace\\ore2015_sample\\pool_sample\\files\\"
    bio_output_folder = "./output/Bio/"


    filenames = []
    filesizes = []


    for x in os.listdir(ore2015_folder):
        #print(x)
        if x.endswith(".owl"):
            filename = ore2015_folder + "\\" + x
            file_size = os.path.getsize(filename)
            filenames.append(x)
            filesizes.append(file_size)

            #print(filename + " : " + str(file_size))

    data_dict = {"Ontology": filenames, "FileSize" : filesizes}

    data = pd.DataFrame(data_dict)

    data.to_csv(ore2015_output_folder + "/ORE2015_FileSize.csv", index=False)


