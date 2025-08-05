#!/bin/bash

# Update and upgrade system packages
sudo apt update -y
sudo apt upgrade -y

echo "Installing Java..."
sudo apt install -y openjdk-17-jdk
echo "Installed Java."

echo "Installing Maven..."
sudo apt install -y maven
echo "Installed Maven."

echo "Installing Python3 and its dependencies..."
sudo apt install -y python3 python3-pip
echo "alias py='python3'" >> ~/.bashrc
source ~/.bashrc
sudo apt install -y python3-venv
echo "Installed Python3 and set alias 'py'."

# Dependencies for PowerJoular
sudo apt install -y gprbuild
sudo apt install -y gnat

# Install PowerJoular
echo "Cloning edited PowerJoular repository..."
git clone https://github.com/afonsoCarreira1/powerjoular_edited.git
cd powerjoular_edited/powerjoular/installer/bash-installer
echo "Running installer..."
bash build-install.sh
echo "Installation complete."
sudo chmod +x /usr/bin/powerjoular # Make powerjoular executable if needed
cd ../../../..

# Compile codegen
echo "Compiling codegen..."
cd codegen/
sudo find src/main/java/com/generated_progs/ -type d -exec rm -r {} +
sudo find src/main/java/com/generated_templates/ -type d -exec rm -r {} +
mvn install
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
mvn clean compile assembly:single
mvn install:install-file -Dfile=target/codegen-1.0-SNAPSHOT-jar-with-dependencies.jar \
    -DgroupId=com.template -DartifactId=codegen -Dversion=1.0-SNAPSHOT -Dpackaging=jar

# Compile parser
echo "Compiling parser..."
cd ..
cd parser/
mvn clean install -U
mvn clean compile assembly:single 
mvn install:install-file -Dfile=target/parser-1.0-SNAPSHOT-jar-with-dependencies.jar \
    -DgroupId=com.parse -DartifactId=parser -Dversion=1.0-SNAPSHOT -Dpackaging=jar

# Compile orchestrator
echo "Compiling orchestrator..."
cd ..
export MAVEN_OPTS="-Xmx512m -Xms128m -Xss2m"
cd orchestrator/
mvn versions:update-parent versions:update-properties versions:use-latest-releases -DgenerateBackupPoms=false # Update pom
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
mvn clean compile assembly:single -U
cd ..
echo "All projects compiled."

# Create virtual environment for Python
cd ml/
VENV_DIR="venv"

# Create venv if it doesn't exist
if [ ! -d "$VENV_DIR" ]; then
    echo "Creating virtual environment..."
    python3 -m venv "$VENV_DIR"
fi

# Activate venv
source "$VENV_DIR/bin/activate"

# Upgrade pip
pip install --upgrade pip

# Install dependencies
pip install -r requirements.txt

# Deactivate venv
deactivate

echo "Environment created. To activate it enter the ml directory and run: source $VENV_DIR/bin/activate"
cd ..
