package com.template;

import java.io.File;

public class RunTest {
    public static void main(String[] args) throws Exception {
        TemplateCreator.main(new String[]{"lists","addAll,containsAll"});
        //String initialPath = "src/main/java/com/";
        //new File(initialPath+"generated_InputTestTemplate").mkdirs();
        //TemplateCreator.createProgramsFromTemplates();
    }
    
}
