import spoon.Launcher;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.Factory;
import spoon.template.Template;
import spoon.template.TemplateParameter;

import java.util.Arrays;
import java.util.List;

public class SpoonTemplateExample {
    public static void main(String[] args) {
        // creating a template instance
        Template t = new ListAddTemplate();
        t._col_ = createVariableAccess(method.getParameters().get(0)); 

        // getting the final AST
        CtStatement injectedCode = t.apply();

        // adds the bound check at the beginning of a method
        method.getBody().insertBegin(injectedCode);
    }
}
