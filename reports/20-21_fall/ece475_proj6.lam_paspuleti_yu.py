# -*- coding: utf-8 -*-
"""fml_proj6.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1Pdk1Yyxu8LAURLYE0UgT_gNhf_oe2Uay

# ECE 475 Project 6: Market Basket Analysis
Tiffany Yu, Jonathan Lam, Harris Paspuleti

The goal of this project is to use market basket analysis to draw interesting inferences (association rules) about some dataset.
"""

# mlxtend has a priori algorithm implementation
!pip install mlxtend

import pandas as pd
import numpy as np
import seaborn as sns
import tensorflow as tf
import matplotlib.pyplot as plt

from mlxtend.preprocessing import TransactionEncoder
from mlxtend.frequent_patterns import apriori
from mlxtend.frequent_patterns import association_rules

"""## Dataset
[FIFA 18 Player Statistics](https://github.com/4m4n5/fifa18-all-player-statistics/)

This dataset contains the attributes for every player that is registered in the latest edition of FIFA 18 database; it can be used for soccer/videogame analysis as it contains attributes such as skill moves, overall, potential, position, etc. While none of us know much about soccer, we wanted to see if there were any interesting assocations we could learn from the data even without knowing much about the sport.

> ### Content
- Every player featuring in FIFA 18
- 70+ attributes
- Player and Flag Images
- Playing Position Data
- Attributes based on actual data of the latest EA's FIFA 18 game
- Attributes include on all player style statistics like Dribbling, Aggression, - GK Skills etc.
- Player personal data like Nationality, Photo, Club, Age, Wage, Salary etc.
> 
> ### Data Source
The data is scraped from the website https://sofifa.com by extracting the Player personal data and Player Ids and then the playing and style statistics.
"""

# grab the data
raw_data = 'https://raw.githubusercontent.com/4m4n5/fifa18-all-player-statistics/master/2019/data.csv'
dataframe = pd.read_csv(raw_data, sep=',', header='infer', error_bad_lines=False)

"""## Preprocessing

### Preprocessing Step 1: Drop unusable columns and rows
Some columns were extraneous, included data that we didn't know how to interpret, or included data that was unique to a player. For example, there was a column for the player's index, the player photo, and some fields like "Real Face" that we weren't familiar with. Additionally, there were many rows with acronyms (e.g., "LS", "ST", etc.) with values that we weren't sure how to interpret.

Some rows also had missing data; we dropped this using `pd::dropna()`.

The dataframe after this initial preprocessing step is shown below.
"""

# preprocessing the data by removing unnecessary columns
dataframe = dataframe.drop(columns=[
    'Unnamed: 0','ID','Photo','Flag','Club Logo','Loaned From','Real Face','LS',
    'ST','RS','LW','LF','CF','RF','RW','LAM','CAM','RAM','LM','LCM','CM','RCM',
    'RM','LWB','LDM','LB','LCB','CB','RCB','RB','CDM','RDM','RWB'
    ]).dropna()
dataframe

"""### Preprocessing Step 2: Determining bins
Before binning features, we plotted histograms of some of the quantitative fields so that we could see what the distributions look like.

Since this is a videogame, much of the numerical ratings were presented as numbers out of 100, so we decided that binning most fields into quartiles was good enough for our purposes. (Using larger bins was also problematic because of the amount of time it took to run the algorithm with more items.)
"""

# list of numerical fields that can be binned
to_bin = ['Age', 'Overall', 'Potential', 'Value', 'Wage', 'Release Clause',
    'Jersey Number', 'FKAccuracy', 'LongPassing', 'BallControl', 'Acceleration',
    'SprintSpeed', 'Agility', 'Reactions', 'Balance', 'ShotPower', 'Jumping',
    'Stamina', 'Strength', 'LongShots', 'Aggression', 'Interceptions',
    'Positioning', 'Vision', 'Penalties', 'Composure', 'Marking',
    'StandingTackle', 'SlidingTackle', 'GKDiving', 'GKHandling', 'GKKicking',
    'GKPositioning', 'GKReflexes'
]

# histograms; see: https://realpython.com/python-histograms/
for col in to_bin:
    # An "interface" to matplotlib.axes.Axes.hist() method
    n, bins, patches = plt.hist(x=dataframe.loc[:, col], bins='auto', color='#0504aa',
                                alpha=0.7, rwidth=0.85)
    plt.title(col)
    plt.grid(axis='y', alpha=0.75)
    plt.xlabel('Value')
    plt.ylabel('Frequency')
    plt.text(23, 45, r'$\mu=15, b=3$')
    maxfreq = n.max()
    # Set a clean upper y-axis limit.
    plt.ylim(ymax=np.ceil(maxfreq / 10) * 10 if maxfreq % 10 else maxfreq + 10)
    plt.figure()

"""### Preprocessing Step 3: Extracting numbers and binning

Some of the numerical fields were formatted, and the numerical value had to be extracted first before binning. For example, money values were written with a euro sign prefix and the magnitude was indicated with a "M" or "K" postfix (indicating millions or thousands).

We decided to bin the players' ages into bins of age 10, and monetary values (e.g., "Wage," "Release Clause," "Value") into logarithmic bins. Most of the player stats are values between 0 and 100, and we decided to bin these into quartiles (larger bins would be noninformative, and smaller bins would make the algorithm take too long).

The dataframe of the binned values is shown below the code.
"""

# binning quantitative values
def bin(col, bins, col_name):
    new_col = col.copy()
    
    for i, bin_min in enumerate(bins):
        bin_max = float('Inf') if i==len(bins)-1 else bins[i+1]
        new_col[(col >= bin_min) & (col < bin_max)] = f'{bin_min}<={col_name}<{bin_max}'
  
    return new_col

# make a money column a number
def money_to_number(df_col):
    col = df_col.copy()
  
    for i, val in enumerate(col):
        if val[-1] == 'M':
            val = int(float(val[1:-1]) * 1000000)
        elif val[-1] == 'K':
            val = int(float(val[1:-1]) * 1000)
        else:
            val = int(float(val[1:]))
        col.iloc[i] = val
    
    return col.astype('int32')
  
# create a copy so we don't modify the original dataframe in place
dataframe_binned = dataframe.copy()

dataframe_binned.loc[:, 'Age'] = bin(dataframe.loc[:, 'Age'],
                                     [0, 10, 20, 30, 40, 50], 'Age')
dataframe_binned.loc[:, 'Overall'] = bin(dataframe.loc[:, 'Overall'],
                                         [0, 25, 50, 75, 100], 'Overall')
dataframe_binned.loc[:, 'Potential'] = bin(dataframe.loc[:, 'Potential'],
                                           [25, 50, 75, 100], 'Potential')
dataframe_binned.loc[:, 'Value'] = bin(money_to_number(dataframe.loc[:, 'Value']),
                                       [0, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9], 'Value')
dataframe_binned.loc[:, 'Wage'] = bin(money_to_number(dataframe.loc[:, 'Wage']),
                                      [0, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9], 'Wage')
dataframe_binned.loc[:, 'Release Clause'] = bin(money_to_number(dataframe.loc[:, 'Release Clause']),
                                                [0, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9], 'Release Clause')
 
to_bin = [
    'Jersey Number', 'FKAccuracy', 'LongPassing', 'BallControl',
    'Acceleration', 'SprintSpeed', 'Agility', 'Reactions', 'Balance',
    'ShotPower', 'Jumping', 'Stamina', 'Strength', 'LongShots', 'Aggression',
    'Interceptions', 'Positioning', 'Vision', 'Penalties', 'Composure',
    'Marking', 'StandingTackle', 'SlidingTackle', 'GKDiving', 'GKHandling',
    'GKKicking', 'GKPositioning', 'GKReflexes']

for col in to_bin:
    dataframe_binned.loc[:, col] = bin(dataframe.loc[:, col], [0, 25, 50, 75, 100], col)
dataframe_binned

"""### Preprocessing Step 4: Choosing features

It is clear that the table becomes very wide at this point. If we included all of the features from the previous section (categorical and binned quantitative features), the a priori algorithm took way too long to run. We experimented with a few different combinations of which features to include. The following is one possible combination of features to perform an analysis on (an explanation of this choice will follow in a later section).
"""

# didn't include most of the rows in the analysis, because the a priori
# algorithm takes too long
cols = [
    'Preferred Foot', 'Age', 'Aggression', 'Nationality',
    'Body Type', 'Reactions','Position', 'Balance',
    'Contract Valid Until', 'Jersey Number', 'Penalties', 'Vision',
    # 'SprintSpeed', 'Agility', 'Reactions', 'ShotPower', 'Jumping', 'Stamina',
    # 'Strength', 'LongShots', 'Aggression','Composure', 'Agility', 
    # 'Interceptions', 'Positioning' , 'Composure', 'Marking', 'StandingTackle',
    # 'SlidingTackle', 'GKDiving','International Reputation','Body Type', 
    # 'GKHandling', 'GKKicking', 'GKPositioning', 'GKReflexes','Overall',
    # 'Value', 'Wage', 'FKAccuracy', 'LongPassing', 'BallControl', 'Aceleration'
]

"""### Preprocessing Step 5: One-hot encoding items
Now that all of the data is binned, it is one-hot encoded. This is the necessary data input format for the a priori algorithm.
"""

# format data for the apriori algorithm
def one_hot_encode_column(df, col):
  items = np.unique(np.array(df.loc[:,col]))
  items_onehot = df.loc[:,col][:, np.newaxis] == items[np.newaxis, :]
  return  pd.DataFrame(columns=[col+' '+str(item) for item in items],
                       data=items_onehot, 
                       dtype=np.int32)

basket_sets = pd.concat([
    one_hot_encode_column(dataframe_binned, col) for col in cols
], axis=1)
basket_sets

"""## Performing Market Basket Analysis

### Finding itemsets
Now that the data is correctly formatted, we can run the a priori market basket analysis. We use mlxetend's a priori implementation to find itemsets with a minimum support of 0.05.

A preview of some of the itemsets with the highest support are shown below. The supports of the visible itemsets make sense. E.g., most of the players are right-footed, most of them are in their twenties, and the itemsets with small support are more specific.

(Note that, with the current feature set, this implementation takes a few minutes to complete this step. Using all of the features, this algorithm did not even complete overnight.)
"""

# for usage of apriori() see: https://pbpython.com/market-basket-analysis.html
frequent_itemsets = apriori(basket_sets, min_support=0.05, use_colnames=True)
frequent_itemsets

"""### Finding association rules

We grab a list of the association rules from the itemsets where the confidence is greater than 0.8, and the lift is greater than 7. A preview of these is shown below.
"""

rules = association_rules(frequent_itemsets, metric="confidence", min_threshold=0.5)

pd.set_option('max_colwidth', 100)

rules[(rules['lift'] >= 7) & (rules['confidence'] >= 0.8)]

"""## Discussion

The table shown above is for associations with high confidence (> 0.8) and lift (> 7).

The first thing to notice is that many of the association rules shown seem to be centered around goalkeepers. It seems that goalkeepers have very distinctive attributes, e.g.:

- They are not very aggressive (0 <= aggression < 25)
- They are not prone to causing penalties (0 <= penalties < 25)
- They have a "normal" body type, have medium reactions, and have few penalties.

There are a lot of repeated associations in this list, but far and large they are mostly about goalkeepers. This is likely related to the bimodal distributions in the earlier histograms, where goalkeepers clearly stood out from the rest; most likely the goalkeepers are very specialized while the other players are more diverse, and therefore all of these selected associations involve goalkeepers.

These results are specific to this choice of features. When we tried different set of features (e.g., including values such as "International Reputation," "Value," "Wage," "Overall," etc.), there were many less-interesting correlations. Many of the associations we saw were that the highest-paid players were those with the highest international reputation, and who had the highest overall and potential scores.

We also did not include many of the player statistics because the algorithm takes too long to run otherwise. If we knew more about soccer or FIFA 18, we could probably draw much more interesting conclusions by using the most relevant or interesting statistics by choosing different sets of features.
"""