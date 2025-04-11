package com.template;

public class InputData {

    int in;
    int arrSize;
    String type;
    Integer maxInput;

    public InputData(int in, int arrSize, String type, Integer maxInput){
        this.in = in;
        this.arrSize = arrSize;
        this.type = type;
        this.maxInput = maxInput;
    }

    @Override
    public String toString() {
        return "in" + in + " | " +"arrSize: "+arrSize+" | "+ "type: "+type + " -> "+ "maxInput: "+maxInput+"\n";
    }
    
}
