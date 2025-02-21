import numpy as np 
import matplotlib.pyplot as plt 
import pandas as pd 
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeRegressor 
from sklearn.metrics import mean_squared_error
from sklearn.metrics import r2_score
from sklearn.tree import plot_tree
from sklearn.model_selection import train_test_split, GridSearchCV, cross_val_score
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor


import re, seaborn as sns
from mpl_toolkits.mplot3d import Axes3D
from matplotlib.colors import ListedColormap

df = pd.read_csv('ml/features.csv')

def print_full_df(df):
    with pd.option_context('display.max_rows', None, 'display.max_columns', None):  # more options can be specified also
        print(df)

def clean_data(df):
    # remove NaN and infinity
    last_column = df.columns[-2]
    df = df.replace([np.inf, -np.inf], np.nan)  # Replace infinity with NaN
    #df = df.replace(0.0, np.nan)
    #df = df[df[last_column] != 0.0]
    df = df.dropna(subset=[last_column])
    return df

def separate_data(df,collection):
    if collection == 'ArrayList': return df[
        df['Filename'].str.contains(collection, case=False, na=False) & 
        ~df['Filename'].str.contains('CopyOnWriteArrayList', case=False, na=False)]
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


def comparison(y,y_pred):
    comparison_df = pd.DataFrame({
    'Real Energy': y[:20],
    'Predicted Energy': y_pred[:20],
    'Difference': y[:20] - y_pred[:20]
    })
    # Exibir a comparação
    print(comparison_df)

def plot_prediction_vs_feature(regressor, X, y, column_name):
    """
    Plots predicted energy vs. a selected feature on a log scale, alongside real energy values.

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

    comparison(y,y_pred)

    plt.figure(figsize=(8, 6))
    
    # Log scale transformation (handle zero or negative values carefully)
    X_log = np.log1p(X[column_name])  # log1p handles zero safely (log(1 + x))

    # Plotting the actual values in blue
    plt.scatter(X_log, y, label="Real Energy", alpha=0.6, color="blue", marker="o")

    # Plotting the predicted values in red
    plt.scatter(X_log, y_pred, label="Predicted Energy", alpha=0.6, color="red", marker="x")
    
    plt.xlabel(f"log(1 + {column_name})")
    plt.ylabel("Energy")
    plt.xscale("log")  # Log scale for the X-axis
    plt.title(f"Log-Scaled Predicted vs Real Energy for {column_name}")
    plt.legend()
    plt.grid(False)
    plt.show()

def model(df):
    # Separate features and target
    X = df.iloc[:, :-1]  # All columns except the last one
    y = df.iloc[:, -1]   # Energy column
    #y = y*10**6
    #print(y)
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42) 

    # Initialize and train the DecisionTreeRegressor
    regressor = DecisionTreeRegressor(random_state=42)
    regressor.fit(X_train, y_train)
    get_scores(regressor,X_test,y_test)
    cross_validate_model(regressor,X,y)
    best_model = tune_hyperparameters(X_train,y_train)
    print('Scores of best model')
    get_scores(best_model,X_test,y_test)
    cross_validate_model(best_model,X,y)
    random_forest_regression(X_train, X_test, y_train, y_test)
    gradient_boosting_regression(X_train, X_test, y_train, y_test)
    #save_figure_pdf(df,regressor)
    #plot_prediction_vs_feature(regressor, X_test, y_test, 'Input2')
    #plot3D(regressor,X_test,y_test)



def tune_hyperparameters(X_train, y_train):
    param_grid = {
        'max_depth': [3, 5, 10, 15, None],
        'min_samples_split': [2, 5, 10],
        'min_samples_leaf': [1, 2, 5],
        'max_features': [None, 'sqrt', 'log2'],
    }
    
    grid_search = GridSearchCV(estimator=DecisionTreeRegressor(random_state=42),
                               param_grid=param_grid, 
                               cv=5,
                               scoring='neg_mean_squared_error',
                               n_jobs=-1,
                               verbose=1)
    
    grid_search.fit(X_train, y_train)
    best_model = grid_search.best_estimator_
    #print(f"Best Parameters: {grid_search.best_params_}")
    
    return best_model

def cross_validate_model(model, X, y):
    cv_scores = cross_val_score(model, X, y, cv=10, scoring='neg_mean_squared_error')
    mean_cv_mse = -cv_scores.mean()  # Negate because cross_val_score returns negative values
    print(f"Mean Cross-Validation MSE: {mean_cv_mse}")
    return mean_cv_mse

def random_forest_regression(X_train, X_test, y_train, y_test):
    rf_regressor = RandomForestRegressor(random_state=42)
    rf_regressor.fit(X_train, y_train)
    rf_r2 = rf_regressor.score(X_test, y_test)
    rf_mse = mean_squared_error(y_test, rf_regressor.predict(X_test))
    print(f"Random Forest R²: {rf_r2}")
    print(f"Random Forest MSE: {rf_mse}")
    return rf_regressor

def gradient_boosting_regression(X_train, X_test, y_train, y_test):
    gb_regressor = GradientBoostingRegressor(random_state=42)
    gb_regressor.fit(X_train, y_train)
    gb_r2 = gb_regressor.score(X_test, y_test)
    gb_mse = mean_squared_error(y_test, gb_regressor.predict(X_test))
    print(f"Gradient Boosting R²: {gb_r2}")
    print(f"Gradient Boosting MSE: {gb_mse}")
    return gb_regressor


def plot3D(regressor, X, y):
    # axes instance
    fig = plt.figure(figsize=(8,8))
    ax = Axes3D(fig, auto_add_to_figure=False)
    fig.add_axes(ax)
    # get colormap from seaborn
    cmap = ListedColormap(sns.color_palette("husl", 256).as_hex())
    x = np.log1p(X['Input1'])
    y = np.log1p(X['Input2'])
    z = regressor.predict(X)
    # plot
    sc = ax.scatter(x, y, z, s=40, c=x, marker='o', cmap=cmap, alpha=1)
    ax.set_xlabel('Input1')
    ax.set_ylabel('Input2')
    ax.set_zlabel('Energy')
    # legend
    plt.legend(*sc.legend_elements(), bbox_to_anchor=(1.05, 1), loc=2)
    plt.show()

#print(df)
df = clean_data(df)
df = separate_data(df,'ArrayList')
#print(df)
df = drop_column(df,'Filename')


model(df)

#df_sorted = df.sort_values(by='EnergyUsed', ascending=False)
#print(df_sorted.head(10))
