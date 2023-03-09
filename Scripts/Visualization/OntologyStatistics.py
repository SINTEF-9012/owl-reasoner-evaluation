import numpy as np
import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt
import seaborn as sns


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



if __name__ == '__main__':
    ore2015_statistics_file:str = "C:\\Users\\anl\\SINTEF\\Skytrack@SINTEF - Documents\\General\\Task T1.3\\EvaluationResult\\ORE2015_Statistics.csv"
    rore2015_statistics_file: str = "C:\\Users\\anl\\SINTEF\\Skytrack@SINTEF - Documents\\General\\Task T1.3\\EvaluationResult\\ORE2015_Realization_Ontology_Statistics.csv"
    bio_statistics_file: str = "C:\\Users\\anl\\SINTEF\\Skytrack@SINTEF - Documents\\General\\Task T1.3\\EvaluationResult\\BioOntology_Statistics.csv"

    ore2015_statistics = pd.read_csv(ore2015_statistics_file)
    ore2015_stattistic_new:DataFrame = ore2015_statistics.iloc[:, [0, 3, 12]].copy()
    ore2015_stattistic_new.rename(columns={"Statistics": "Ontologies", "Unnamed: 12": "Profile"}, inplace=True)
    ore2015_stattistic_new['Size'] = np.where(ore2015_stattistic_new['Axioms'] < 1000, 'Very Small',
                                              np.where(ore2015_stattistic_new['Axioms'] < 10000, 'Small',
                                                       np.where(ore2015_stattistic_new['Axioms'] < 100000, 'Medium',
                                                                np.where(ore2015_stattistic_new['Axioms'] < 1000000, 'Large',
                                                                         np.where(ore2015_stattistic_new['Axioms'] < 10000000, 'Very Large',  'Huge')))))
    ore2015_profiles = ore2015_stattistic_new.groupby(['Size']).count()
    ore2015_profiles = ore2015_profiles.iloc[:, [0]].copy().reset_index()

    rore2015_statistics = pd.read_csv(rore2015_statistics_file)
    rore2015_stattistic_new:DataFrame = rore2015_statistics.iloc[:, [0, 3, 12]].copy()
    rore2015_stattistic_new.rename(columns={"Statistics": "Ontologies", "Unnamed: 12": "Profile"}, inplace=True)
    rore2015_stattistic_new['Size'] = np.where(rore2015_stattistic_new['Axioms'] < 1000, 'Very Small',
                                              np.where(rore2015_stattistic_new['Axioms'] < 10000, 'Small',
                                                       np.where(rore2015_stattistic_new['Axioms'] < 100000, 'Medium',
                                                                np.where(rore2015_stattistic_new['Axioms'] < 1000000, 'Large',
                                                                         np.where(rore2015_stattistic_new['Axioms'] < 10000000, 'Very Large',  'Huge')))))
    rore2015_profiles = rore2015_stattistic_new.groupby(['Size']).count()
    rore2015_profiles = rore2015_profiles.iloc[:, [0]].copy().reset_index()




    bio_statistics = pd.read_csv(bio_statistics_file)
    bio_stattistic_new: DataFrame = bio_statistics.iloc[:, [0, 3, 12]].copy()
    bio_stattistic_new.rename(columns={"Statistics": "Ontologies", "Unnamed: 12": "Profile"}, inplace=True)
    bio_stattistic_new['Size'] = np.where(bio_stattistic_new['Axioms'] < 1000, 'Very Small',
                                              np.where(bio_stattistic_new['Axioms'] < 10000, 'Small',
                                                       np.where(bio_stattistic_new['Axioms'] < 100000, 'Medium',
                                                                np.where(bio_stattistic_new['Axioms'] < 1000000, 'Large',
                                                                         np.where(bio_stattistic_new[
                                                                                      'Axioms'] < 10000000,
                                                                                  'Very Large', 'Huge')))))
    bio_profiles = bio_stattistic_new.groupby(['Size']).count()
    bio_profiles = bio_profiles.iloc[:, [0]].copy().reset_index()








    #ax = sns.catplot(x="Size", y="Ontologies", col ="Profile", data=profiles, col_order=["OWL2_EL", "OWL2_EL"], order=["Very Small", "Small", "Medium", "Large", "Very Large"], kind="bar")





    print(ore2015_profiles)
    print(rore2015_profiles)
    print(bio_profiles)

    #sns.set(style="whitegrid")
    sns.set_color_codes("muted")
    fig, axes = plt.subplots(1, 3, sharey=True, figsize=(11, 4))

    sns.barplot(ax=axes[0], x="Size", y="Ontologies", data=ore2015_profiles, order=["Very Small", "Small", "Medium", "Large", "Very Large", "Huge"], color="b")
    #axes[0].set_yscale("log")
    axes[0].set_title("(a) ORE 2015 Ontologies \n (Loading, Consistency, Classification)")
    axes[0].grid(False)
    axes[0].set_ylabel('Number of Ontologies')
    axes[0].set_xlabel('')

    axes[0].tick_params(left=False, bottom=False)
    axes[0].set(yticklabels=[])

    axes[0].tick_params(axis='x', rotation=25)

    show_values_on_bars(axes[0], space=10)


    sns.barplot(ax=axes[1], x="Size", y="Ontologies", data=rore2015_profiles, order=["Very Small", "Small", "Medium", "Large", "Very Large", "Huge"], color="b")
    #axes[1].set_yscale("log")
    axes[1].set_title("(b) ORE 2015 Ontologies \n (Realization)")
    axes[1].set_ylabel('')
    axes[1].set_xlabel('Size of Ontologies')
    axes[1].grid(False)

    axes[1].tick_params(left=False, bottom=False)
    axes[1].set(yticklabels=[])

    axes[1].tick_params(axis='x', rotation=25)

    show_values_on_bars(axes[1], space=10)

    sns.barplot(ax=axes[2], x="Size", y="Ontologies", data=bio_profiles,
                order=["Very Small", "Small", "Medium", "Large", "Very Large", "Huge"], color="b")

    #axes[2].set_yscale("log")
    axes[2].set_title("(c) NCBO Bio-ontologies")
    axes[2].set_ylabel('')
    axes[2].set_xlabel('')
    axes[2].grid(False)

    axes[2].tick_params(left=False, bottom=False)
    axes[2].set(yticklabels=[])

    axes[2].tick_params(axis='x', rotation=25)


    show_values_on_bars(axes[2], space=10)
    #fig.legend()



    #axes[2].legend(labels=["Very Small: #Axioms < 10E3", "Small: #Axioms < 10E4", "Medium: #Axioms < 10E5", "Large: #Axioms < 10E6", "Very Large: #Axioms < 10E7", "Huge: #Axioms > 10E7"], handlelength=0, handletextpad=0)
    output_folder = "./output/"
    plt.savefig(output_folder + "/onto_stat.pdf", bbox_inches='tight')
    plt.savefig(output_folder + "/onto_stat.png", bbox_inches='tight')



    plt.show()






