# 🔋 Java Energy Consumption Prediction

This repository contains a framework of a set of Java Maven projects designed to **train machine learning models** to predict the **energy consumption** of Java programs.

The workflow involves:
1. **Generating Java becnhmarks** based on collection methods.
2. **Extracting code features** (e.g., cyclomatic complexity, loop depth, inputs).
3. **Running the benchmarks** and **logging their energy consumption**.
4. **Training a machine learning model** to predict energy usage.
5. **Energy Extension** that estimates the energy consumption of Java programs.

🔧 **Built for Linux** |  **Uses PowerJoular for energy measurement** |  **ML-based prediction**

---

## 📂 Project Structure

## ⚙️ Dependencies
- **PowerJoular** (Linux only) - used for precise energy measurement
- **Spoon** - for Java code analysis and transformation  
- **Python** - for model training

**Install dependencies**  
```sh
./setup.sh
```

---

### 1️⃣ Codegen (Code Generator)  
This module **automatically generates Java benchmarks** using methods from **collections** (Lists,Sets,Maps,Math), or even **custom classes**.

- **Uses** [Spoon](https://spoon.gforge.inria.fr/) to create program variations  
- **Generates random inputs** to enrich the dataset  
- **Supports custom classes** (place them in `codegen/src/main/java/com/template/programsToBenchmark/`)  


**Run for the lists collections (gets all methods)**  
```sh
./run_codegen.sh lists
```

**Run for the lists collections add, get, size methods**  
```sh
./run_codegen.sh lists add,get,size
```

**Run for a custom Class**  
```sh
./run_codegen.sh Fibonacci
```

**Run for a custom Lib**  
```sh
./run_codegen.sh lib_java.lang.Math cos
```

---

### 2️⃣ Parser (Feature Extractor)  
This module analyzes Java programs and extracts **key features** that influence energy consumption, such as:

🔹 Number of `if` statements  
🔹 Loop depth  
🔹 Cyclomatic complexity  
🔹 Other structural characteristics  


It is used by the orchestrator and the energy tool modules.

**Compile Parser:**  
```sh
./run_parser.sh
```

---

### 3️⃣ Orchestrator (Execution Manager)  
This module **coordinates the entire process**, calling CodeGen to generate benchmarks, executing them, and collecting energy profiles. It:

- **Runs each generated benchmark** and logs its energy usage  
- **Calls the Parser** to extract relevant features  
- **Saves all data** into a structured CSV file (`features.csv`)  
- **Logs execution details, errors, and temp files** in `orchestrator/logs/run_<date>/`  

**Run Orchestrator:**  
```sh
./run_orchestrator.sh
```

---

### 4️⃣ Energy Extension 🚧 (In Progress)
This module provides a VSCode extension that combines static analysis and machine learning to predict the energy consumption of Java programs. It uses pre-trained models and takes into account various program features, such as loops, method calls, and control flow structures, to estimate energy usage accurately. The extension also includes interactive sliders that allow users to modify input values, loop counts, and other parameters to visualize how these changes impact overall energy consumption.

 

---

## 📊 Machine Learning Training 🚧 (In Progress)
After data collection, you can train a model using the provided Python script:

**Run ML Training:**  
```sh
python3 ml/train.py
```

---


## 📜 License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

