package com.parse;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
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
        //String expected = "{PREINCDepth_0=0, VariableCountDepth_0=3, PREINCDepth_1=1, VariableCountDepth_1=0, VariableDeclarationsDepth_1=0, MethodInvocationsDepth_1=0, MethodInvocationsDepth_0=0, ReassignmentsDepth_1=0, POSTINCDepth_0=1, ReassignmentsDepth_0=1, MaxLoopDepth=1, BinaryOperatorsDepth_1=0, BinaryOperatorsDepth_0=2, BranchCount=0, java.lang.Boolean=1, LTDepth_0=1, PLUSDepth_0=2, LoopCount=1, ThreadUsage=0, CyclomaticComplexity=2, LiteralCountDepth_1=0, LiteralCountDepth_0=6, int=2, ReturnType_void=1, ImportsUsed=0, AssignmentsDepth_1=0, AssignmentsDepth_0=1, VariableDeclarationsDepth_0=3}";
        ASTFeatureExtractor parser = new ASTFeatureExtractor("src/main/java/com/parse/","Test",false);
        MethodEnergyInfo m = new MethodEnergyInfo("com.parse.Test.f()");
        ModelInfo mi = new ModelInfo();
        m.addModelInfo(mi);
        HashMap<String, HashMap<String,Object>> map = new HashMap<>();
        HashMap<String,Object> map2 = new HashMap<>();
        map2.put("MethodEnergyInfo", m);
        map.put("com.parse.Test.f()", map2);
        parser.getToolParser().methodsUsageCounter(map);
        System.err.println("stopped!");
    }
}
