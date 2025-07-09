PROG=$1
SRC_DIR="src/main/java/com/template/programsToBenchmark"
BIN="bin"
PKG="com.template.programsToBenchmark"
PKG_PATH="com/template/programsToBenchmark"

# Clean old build
rm -rf $BIN
mkdir -p $BIN

# Compile all java files in the package directory
javac -d $BIN $SRC_DIR/*.java

if [ $? -ne 0 ]; then
  echo "Compilation failed."
  exit 1
fi

cd $BIN

# Create jar
jar cfe ${PROG}.jar ${PKG}.${PROG} ${PKG_PATH}/*.class

cp ${PROG}.jar ../../ext/server/custom_progs
