package com.parse;

import java.util.HashMap;
import java.util.Map;


public class App 
{
    public static void main( String[] args )
    {
        HashMap<String, Map<String, Object>> methods = ASTFeatureExtractor.getFeatures("file",false);
        Map<String, Object> methodfeatures = methods.get("TestFile.t()");
        System.out.println(methodfeatures);
    }
}
