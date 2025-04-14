package com.template;

import java.io.File;
import java.io.IOException;

public class RunTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        String initialPath = "src/main/java/com/";
        new File(initialPath+"generated_InputTestTemplate").mkdirs();
        TemplateCreator.createProgramsFromTemplates();
    }
    
}
