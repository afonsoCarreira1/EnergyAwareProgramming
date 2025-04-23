package com.parse;

import java.util.HashMap;
import java.util.Map;


public class App 
{
    public static void main( String[] args )
    {
        ASTFeatureExtractor parser = new ASTFeatureExtractor("src/main/java/com/parse/","TestModelProg",false);
        HashMap<String, Map<String, Object>> methods = parser.getFeatures();
        Map<String, Object> methodfeatures = methods.get("TestModelProg.fun(ArrayList | ArrayList)");
        System.out.println(methodfeatures);

        //ASTFeatureExtractor parser = new ASTFeatureExtractor("src/main/java/com/parse/","TestParseInputs",false);;
        //parser.getNumberOfInputs();
    }
}
