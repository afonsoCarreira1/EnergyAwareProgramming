import numpy as np 
import matplotlib.pyplot as plt 
import pandas as pd 
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeRegressor 
from sklearn.metrics import mean_squared_error
from sklearn.metrics import r2_score
from sklearn.tree import plot_tree

df = pd.read_csv('ml/features.csv')

def print_full_df(df):
    with pd.option_context('display.max_rows', None, 'display.max_columns', None):  # more options can be specified also
        print(df)

def clean_data(df):
    # remove NaN and infinity
    last_column = df.columns[-2]
    df = df.replace([np.inf, -np.inf], np.nan)  # Replace infinity with NaN
    df = df.dropna(subset=[last_column])
    return df

def separate_data(df,collection):
    return df[df['Filename'].str.contains(collection, case=False, na=False)]

def drop_column(df,column_name):
    if column_name in df.columns:df = df.drop(columns=[column_name])
    else: print(f"Column '{column_name}' not found in DataFrame.")
    return df  

def get_scores(regressor,X_test,y_test):
    # Evaluate the model
    predictions = regressor.predict(X_test)
    mse = mean_squared_error(y_test, predictions)
    r2 = r2_score(y_test, predictions)
    print(f"Mean Squared Error: {mse:.4f}")
    print(f"R² Score: {r2:.4f}")

def save_figure_pdf(df, regressor):
    plt.figure(figsize=(25, 15))  # Increase size for better visibility
    plot_tree(
        regressor, 
        feature_names=df.columns[:-1],  # Column names for clarity
        filled=True, 
        rounded=True, 
        precision=2
    )
    plt.title('Decision Tree Visualization')

    # Save plot to PDF with higher resolution
    plt.savefig('ml/decision_tree_visualization.svg', format='svg', bbox_inches='tight', dpi=300)
    plt.close()  # Close the plot to avoid displaying it inline


def plot_prediction_vs_feature(regressor, X, y, column_name):
    """
    Plots predicted energy vs. a selected feature on a log scale.

    Parameters:
    - regressor: Trained DecisionTreeRegressor model
    - X: Feature dataset (DataFrame)
    - y: True energy values (Series)
    - column_name: The feature column to plot against predictions
    """
    if column_name not in X.columns:
        print(f"Column '{column_name}' not found in dataset!")
        return

    y_pred = regressor.predict(X)  # Predict using full feature set
    feature_values_input1 = set(X['Input1'])
    print(f"Input1 (Size of each List) - Unique Values: {feature_values_input1}")
    feature_values_input2 = set(X['Input2'])
    print(f"Input2 (Number of Lists in Array) - Unique Values: {feature_values_input2}")
    plt.figure(figsize=(8, 6))
    
    # Log scale transformation (handle zero or negative values carefully)
    X_log = np.log1p(X[column_name])  # log1p handles zero safely (log(1 + x))
    #y_pred_log = np.log1p(y_pred)

    plt.scatter(X_log, y_pred, label="Predicted Energy (log scale)", alpha=0.6, color="red", marker="x")
    plt.xlabel(f"log(1 + {column_name})")
    plt.ylabel("Energy")
    plt.xscale("log")
    plt.title(f"Log-Scaled Predicted Energy vs. {column_name}")
    plt.legend()
    plt.grid(False)
    plt.show()


def model(df):
    # Separate features and target
    X = df.iloc[:, :-1]  # All columns except the last one
    y = df.iloc[:, -1]   # Energy column
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)

    # Initialize and train the DecisionTreeRegressor
    regressor = DecisionTreeRegressor(random_state=42)
    regressor.fit(X_train, y_train)
    get_scores(regressor,X_test,y_test)
    #save_figure_pdf(df,regressor)
    plot_prediction_vs_feature(regressor, X_train, y_train, 'Input2')


#print(df)
df = clean_data(df)
df = separate_data(df,'')
#print(df)
df = drop_column(df,'Filename')
#print(df)
model(df)
