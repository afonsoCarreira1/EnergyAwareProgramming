package com.parse;

import java.util.HashSet;

public class ModelInfo {

    String modelName;
    String colType;
    String methodType;
    String args;
    HashSet<String> ids;

    public ModelInfo() {
        this.ids = new HashSet<>();
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

}
