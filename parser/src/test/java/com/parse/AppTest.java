package com.parse;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void test()
    {
        ASTFeatureExtractor parser = new ASTFeatureExtractor("src/main/java/com/parse/","Test",false);
        //parser.getToolParser().methodsUsageCounter();
        List<MethodEnergyInfo> methodEnergyInfos = parser.getToolParser().getMethodsForSliders(new HashSet<>());
        for (MethodEnergyInfo mei : methodEnergyInfos) {
            for (ModelInfo mi : mei.getModelInfos()) {
                if (mi.getLoopIds() != null) {
                    for (String s : mi.getLoopIds()) {
                        System.out.println("loop: "+s);
                    }
                }
                
            }
        }
    }
}
