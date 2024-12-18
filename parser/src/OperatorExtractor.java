import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtOperatorAssignmentImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OperatorExtractor {

    public static ArrayList<String> extractOperators(CtMethod<?> method) {
        
        ArrayList<String> operators = new ArrayList<>();

        // Handle binary operators (e.g., +, -, *, /, %, etc.)
        for (CtBinaryOperator<?> binaryOp : method.getElements(new TypeFilter<>(CtBinaryOperator.class))) {
            String operator = binaryOp.getKind().toString(); // Get the operator kind
            operators.add(operator);
        }

        // Handle unary operators (e.g., ++, --, !, etc.)
        for (CtUnaryOperator<?> unaryOp : method.getElements(new TypeFilter<>(CtUnaryOperator.class))) {
            String operator = unaryOp.getKind().toString(); // Get the operator kind
            operators.add(operator);
        }

        // Handle assignments (e.g., +=, -=, *=, etc.)
        for (CtAssignment<?, ?> assignment : method.getElements(new TypeFilter<>(CtAssignment.class))) {
            if (assignment instanceof CtOperatorAssignmentImpl) {
                // Some assignments (e.g., +=) may also be binary operators
                String operator = ((CtOperatorAssignmentImpl) assignment).getKind().toString();
                operators.add(operator);
            } else {
                operators.add("ASSIGNMENT");
            }
        }
        return operators;
    }
}
