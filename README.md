# 🔋 Java Energy Consumption Prediction

This repository contains a set of Java Maven projects designed to **train machine learning models** to predict the **energy consumption** of Java programs—without executing them.

The workflow involves:
1. **Generating Java programs** based on collection methods.
2. **Extracting code features** (e.g., cyclomatic complexity, loop depth).
3. **Running the programs** and **logging their energy consumption**.
4. **Training a machine learning model** to predict energy usage.

🔧 **Built for Linux** |  **Uses PowerJoular for energy measurement** |  **ML-based prediction**

---

## 📂 Project Structure

### 1️⃣ Codegen (Code Generator)  
This module **automatically generates Java programs** using methods from **Lists, Sets, and Maps**, or even **custom classes**.

- **Uses** [Spoon](https://spoon.gforge.inria.fr/) to create program variations  
- **Generates random inputs** to enrich the dataset  
- **Supports custom classes** (place them in `codegen/src/main/java/com/template/programsToBenchmark/`)  

**Run CodeGen:**  
```sh
./run_codegen.sh
```

---

### 2️⃣ Parser (Feature Extractor)  
This module analyzes Java programs and extracts **key features** that influence energy consumption, such as:

🔹 Number of `if` statements  
🔹 Loop depth  
🔹 Cyclomatic complexity  
🔹 Other structural characteristics  

**Run Parser:**  
```sh
./run_parser.sh
```

---

### 3️⃣ Orchestrator (Execution Manager)  
This module **coordinates the entire process**, calling CodeGen to generate programs, executing them, and collecting feature data. It:

- **Runs each generated program** and logs its energy usage  
- **Calls the Parser** to extract relevant features  
- **Saves all data** into a structured CSV file (`features.csv`)  
- **Logs execution details, errors, and temp files** in `orchestrator/logs/run_<date>/`  

**Run Orchestrator (must be run last):**  
```sh
./run_orchestrator.sh
```

---

## 📊 Machine Learning Training 🚧 (In Progress)
After data collection, you can train a model using the provided Python script:

**Run ML Training:**  
```sh
python3 ml/train.py
```

---

## ⚙️ Dependencies 🚧 (In Progress)
- **PowerJoular** (for energy measurement)  
- **Spoon** (for Java code analysis and transformation)  
- **Python** (for model training)  

**Install dependencies**  
```sh
./setup.sh
```

---

## 📜 License
This project is licensed under the MIT License. See the LICENSE file for details.

