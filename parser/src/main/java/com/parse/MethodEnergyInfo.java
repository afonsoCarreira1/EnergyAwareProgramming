package com.parse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MethodEnergyInfo {

    private String methodName;
    private ArrayList<ModelInfo> modelInfos;
    private double totalEnergy;
    private Path methodPath;

    public MethodEnergyInfo(String methodName, Path path) {
        this.methodName = methodName;
        this.methodPath = path;
        this.modelInfos = new ArrayList<>();
        this.totalEnergy = 0.0;
    }

    public void addModelInfo(ModelInfo modelInfo) {
        this.modelInfos.add(modelInfo);
    }

    public String getMethodName() {
        return methodName;
    }

    public ArrayList<ModelInfo> getModelInfos() {
        return modelInfos;
    }

    public double getTotalEnergy() {
        return totalEnergy;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setModelInfos(ArrayList<ModelInfo> modelInfos) {
        this.modelInfos = modelInfos;
    }

    public void setTotalEnergy(double totalEnergy) {
        this.totalEnergy = totalEnergy;
    }

    public Path getMethodPath() {
        return methodPath;
    }
    
    public String getUri() {
        return methodPath.toUri().toString();
    }
}
