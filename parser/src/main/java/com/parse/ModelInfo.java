package com.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import spoon.reflect.code.CtInvocation;

public class ModelInfo {

    private String modelName;
    private String colType;
    private String methodType;
    private String args;
    private HashSet<String> ids;
    private HashMap<String,HashMap<String,Object>> inputToVarName;
    private ArrayList<String> loopIds;
    private boolean isMethodCall;
    private int line;
    private String expression;
    private CtInvocation<?> realInvocation;
    

    public ModelInfo(String modelName,CtInvocation<?> realInvocation) {
        this.modelName = modelName;
        this.realInvocation = realInvocation;
        this.ids = new HashSet<>();
        this.inputToVarName = new HashMap<>();
        this.loopIds = new ArrayList<>();
    }
    
    @Override
    public String toString() {
        realInvocation.setComments(new ArrayList<>());
        return realInvocation.toString();
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public void setLine(int line) {
        this.line = line;
    }

    

    public void setInputToVarName(HashMap<String, HashMap<String,Object>> inputToVarName) {
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

    public void associateInputToVar(String input, String varName, boolean isLiteral) {
        HashMap<String,Object> m = new HashMap<>();
        m.put("input", varName);
        m.put("isLiteral", isLiteral);
        this.inputToVarName.put(input, m);
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

    public HashMap<String, HashMap<String,Object>> getInputToVarName() {
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

    public int getLine() {
        return line;
    }

    public CtInvocation<?> getRealInvocation() {
        return realInvocation;
    }

}
