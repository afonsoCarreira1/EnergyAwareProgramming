package com.parse;

import java.util.ArrayList;

public class MethodEnergyInfo {

    private String methodName;
    private ArrayList<ModelInfo> modelInfos;
    private double totalEnergy;

    public MethodEnergyInfo(String methodName) {
        this.methodName = methodName;
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

    
    
}
