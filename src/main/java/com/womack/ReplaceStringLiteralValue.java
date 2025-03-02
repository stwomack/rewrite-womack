package com.womack;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

@Value
@EqualsAndHashCode(callSuper = false)
public class ReplaceStringLiteralValue extends Recipe {

    @Option(displayName = "Old literal `String` value",
            description = "The `String` value to replace.",
            example = "apple")
    String oldLiteralValue;

    @Option(displayName = "New literal `String` value",
            description = "The `String` value to replace with.",
            example = "orange")
    String newLiteralValue;

    @Override
    public String getDisplayName() {
        return "Replace `String` literal";
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
                if (lit.getType() == JavaType.Primitive.String &&
                    oldLiteralValue.equals(lit.getValue())) {
                    return lit
                            .withValue(newLiteralValue)
                            .withValueSource('"' + newLiteralValue + '"');
                }
                return lit;
            }
        };
    }

}