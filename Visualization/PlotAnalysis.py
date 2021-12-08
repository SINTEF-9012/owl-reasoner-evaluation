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
    return data

def plot_stacked_bar(data, series_labels, category_labels=None,
                     show_values=False, value_format="{}", y_label=None,
                     colors=None, grid=True, reverse=False):
    """Plots a stacked bar chart with the data and labels provided.

    Keyword arguments:
    data            -- 2-dimensional numpy array or nested list
                       containing data for each series in rows
    series_labels   -- list of series labels (these appear in
                       the legend)
    category_labels -- list of category labels (these appear
                       on the x-axis)
    show_values     -- If True then numeric value labels will
                       be shown on each bar
    value_format    -- Format string for numeric value labels
                       (default is "{}")
    y_label         -- Label for y-axis (str)
    colors          -- List of color labels
    grid            -- If True display grid
    reverse         -- If True reverse the order that the
                       series are displayed (left-to-right
                       or right-to-left)
    """

    ny = len(data[0])
    ind = list(range(ny))

    axes = []
    cum_size = np.zeros(ny)

    data = np.array(data)

    if reverse:
        data = np.flip(data, axis=1)
        category_labels = reversed(category_labels)

    for i, row_data in enumerate(data):
        color = colors[i] if colors is not None else None
        axes.append(plt.bar(ind, row_data, bottom=cum_size,
                            label=series_labels[i], color=color))
        cum_size += row_data

    if category_labels is not None:
        plt.xticks(ind, category_labels)

    if y_label:
        plt.ylabel(y_label)

    plt.legend()

    if grid:
        plt.grid()

    if show_values:
        for axis in axes:
            for bar in axis:
                w, h = bar.get_width(), bar.get_height()
                plt.text(bar.get_x() + w/2, bar.get_y() + h/2,
                         value_format.format(h), ha="center",
                         va="center")


def plot(data, task, ax):
    bars = []
    handle = []


    bar = sns.barplot(ax=ax, data=data, x= "Reasoners", y="Success", color="green")
    bars.append(bar)

    #sequential_colors = sns.color_palette("Reds", 10)
    #sns.set_palette(sequential_colors)

    columns = ["Success", "Timeout", "OutOfMemory", "Other Error"]
    colors=["lime", "gold", "mistyrose", "red"]

    if task == "Classification" or task == "Realization":
        columns = ["Success", "Inconsistent Error", "Timeout", "OutOfMemory", "Other Error"]
        colors = ["lime", "green", "gold", "mistyrose", "red"]

    new_data = data[columns].copy().to_numpy().transpose()

    category_labels = data["Reasoners"].copy().to_numpy()

    plot_stacked_bar(
        new_data,
        series_labels=columns,
        category_labels=category_labels,
        show_values=True,
        value_format="{:.1f}",
        colors=colors,
        y_label=None
    )



if __name__ == '__main__':
    input_folder="./output/"
    output_folder="./output"

    Path(output_folder).mkdir(parents=True, exist_ok=True)

    reasoner_name = ["Factpp", "HermiT", "JFact", "Konclude", "KoncludeCLI", "Pellet", "Openllet"]
    task_name = ["Loading", "Consistency", "Classification", "Realization"]

    task_name = ["Loading"]

    i = 0
    fig, axes = plt.subplots(1, 2, figsize=(11, 5))

    for task in task_name:
            file_name = input_folder + task + ".csv"
            data = load_analyze_csv(file_name)



            plot(data, task, axes[i])


            i = i + 1

    plt.show()

    #input="./output/Bio/"
    #output="./output/Bio/"


