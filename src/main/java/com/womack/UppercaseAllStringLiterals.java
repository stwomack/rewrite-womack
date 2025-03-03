package com.womack;

import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

public class UppercaseAllStringLiterals extends Recipe {
    @Override
    public String getDisplayName() {
        return "Uppercase `String` literal";
    }

    @Override
    public String getDescription() {
        return "Replace the value of a complete `String` literal.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.Literal visitLiteral(J.Literal literal, ExecutionContext ctx) {
                J.Literal lit = super.visitLiteral(literal, ctx);
                if (lit.getType() == JavaType.Primitive.String) {
                    String originalValue = lit.getValue().toString();
                    String originalValueSource = lit.getValueSource();
                    String upperCaseValue = originalValue.toUpperCase();
                    String upperCaseValueSource = '"' + upperCaseValue + '"';

                    System.out.println("Original Value Source: " + originalValueSource);
                    System.out.println("New Value Source: " + upperCaseValueSource);

                    J.Literal transformed = lit
                            .withValue(upperCaseValue)
                            .withValueSource(upperCaseValueSource);

                    String transformedValueSource = transformed.getValueSource();
                    System.out.println("Transformed Value Source: " + transformedValueSource);

                    assert originalValueSource != null;
                    if (!originalValueSource.equals(transformedValueSource)) {
                        return transformed;
                    }
                }
                return lit;
            }
        };
    }
}
