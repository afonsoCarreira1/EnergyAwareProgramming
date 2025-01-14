import numpy as np 
import matplotlib.pyplot as plt 
import pandas as pd 
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeRegressor 
from sklearn.metrics import mean_squared_error
from sklearn.metrics import r2_score
from sklearn.tree import plot_tree

data = pd.read_csv('features.csv')

print(len(data))

# remove NaN and infinity
last_column = data.columns[-1]
data = data.replace([np.inf, -np.inf], np.nan)  # Replace infinity with NaN
data = data.dropna(subset=[last_column])

print(len(data))

# Separate features and target
X = data.iloc[:, :-1]  # All columns except the last one
y = data.iloc[:, -1]   # Energy column
  
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Initialize and train the DecisionTreeRegressor
regressor = DecisionTreeRegressor(random_state=42)
regressor.fit(X_train, y_train)

# Evaluate the model
predictions = regressor.predict(X_test)
mse = mean_squared_error(y_test, predictions)
r2 = r2_score(y_test, predictions)

print(f"Mean Squared Error: {mse:.4f}")
print(f"R² Score: {r2:.4f}")


plt.figure(figsize=(25, 15))  # Increase size for better visibility
plot_tree(
    regressor, 
    feature_names=data.columns[:-1],  # Column names for clarity
    filled=True, 
    rounded=True, 
    precision=2
)
plt.title('Decision Tree Visualization')

# Save plot to PDF
plt.savefig('ml/decision_tree_visualization.pdf', format='pdf', bbox_inches='tight')
plt.close()  # Close the plot to avoid displaying it inline