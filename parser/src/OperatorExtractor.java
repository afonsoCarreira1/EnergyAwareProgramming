import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtOperatorAssignmentImpl;

import java.util.ArrayList;

public class OperatorExtractor {
    public static ArrayList<String> extractOperators(CtBlock<?> body, String complementFeatureName) {
        
        ArrayList<String> operators = new ArrayList<>();

        // Handle binary operators (e.g., +, -, *, /, %, etc.)
        for (CtBinaryOperator<?> binaryOp : body.getElements(new TypeFilter<>(CtBinaryOperator.class))) {
            String operator = binaryOp.getKind().toString(); // Get the operator kind
            operators.add(operator+complementFeatureName);
        }

        // Handle unary operators (e.g., ++, --, !, etc.)
        for (CtUnaryOperator<?> unaryOp : body.getElements(new TypeFilter<>(CtUnaryOperator.class))) {
            String operator = unaryOp.getKind().toString(); // Get the operator kind
            operators.add(operator+complementFeatureName);
        }

        // Handle assignments (e.g., +=, -=, *=, etc.)
        for (CtAssignment<?, ?> assignment : body.getElements(new TypeFilter<>(CtAssignment.class))) {
            if (assignment instanceof CtOperatorAssignmentImpl) {
                // Some assignments (e.g., +=) may also be binary operators
                String operator = ((CtOperatorAssignmentImpl<?,?>) assignment).getKind().toString();
                operators.add(operator+complementFeatureName);
            } 
            //else {
            //    operators.add("ASSIGNMENT"+complementFeatureName);
            //}
        }
        return operators;
    }
}
