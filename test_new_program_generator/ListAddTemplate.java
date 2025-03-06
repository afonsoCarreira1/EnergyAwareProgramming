import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.template.AbstractTemplate;
import spoon.template.Parameter;
import spoon.template.Template;
import spoon.template.TemplateParameter;
import spoon.template.StatementTemplate;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class ListAddTemplate<T> extends StatementTemplate {
    TemplateParameter<Collection<?>> _col_;
	@Override
	public void statement() {
		if (_col_.S().size() > 10)
			throw new OutOfMemoryError();
	}
}
