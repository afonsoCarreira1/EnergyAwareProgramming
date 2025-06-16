package com.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ModelInfo {

    private String modelName;
    private String colType;
    private String methodType;
    private String args;
    private HashSet<String> ids;
    private HashMap<String,String> inputToVarName;
    private ArrayList<String> loopIds;
    private boolean isMethodCall;

    

    public ModelInfo() {
        this.ids = new HashSet<>();
        this.inputToVarName = new HashMap<>();
        this.loopIds = new ArrayList<>();
        this.isMethodCall = false;
    }

    public ModelInfo(String modelName) {
        this.modelName = modelName;
        this.ids = new HashSet<>();
        this.inputToVarName = new HashMap<>();
        this.loopIds = new ArrayList<>();
    }

    public void setInputToVarName(HashMap<String, String> inputToVarName) {
        this.inputToVarName = inputToVarName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setColType(String colType) {
        this.colType = colType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setIds(HashSet<String> ids) {
        this.ids = ids;
    }

    public void addId(String id) {
        this.ids.add(id);
    }

    public void associateInputToVar(String input, String varName) {
        this.inputToVarName.put(input, varName);
    }

    public String getColType() {
        return colType;
    }

    public String getMethodType() {
        return methodType;
    }

    public String getArgs() {
        return args;
    }

    public HashSet<String> getIds() {
        return ids;
    }

    public String getModelName() {
        return this.modelName;
    }

    public HashMap<String, String> getInputToVarName() {
        return inputToVarName;
    }

    public ArrayList<String> getLoopIds() {
        return this.loopIds;
    }

    public void addLoopId(String id) {
        this.loopIds.add(id);
    }

    public void setMethodCall(boolean isMethodCall) {
        this.isMethodCall = isMethodCall;
    }

    public boolean isMethodCall() { 
        return this.isMethodCall;
    }

}
