package com.womack;

import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.List;

public class UppercaseAllStringLiterals extends Recipe {
    private static final MethodMatcher MATCHER = new MethodMatcher("org.junit.jupiter.api.Assertions assertEquals(..)");

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Uppercase Everything";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "This will take every string literal and uppercase it.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesType<>("org.junit.jupiter.api.Assertions", null),
                new JavaIsoVisitor<ExecutionContext>() {
                    @Override
                    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                        J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
                        if (!MATCHER.matches(m)) {
                            return m;
                        }
                        List<Expression> arguments = m.getArguments();
                        maybeAddImport("org.assertj.core.api.Assertions");
                        maybeRemoveImport("org.junit.jupiter.api.Assertions");
                        if (arguments.size() == 2) {
                            Expression expected = arguments.get(0);
                            Expression actual = arguments.get(1);

                            m = JavaTemplate.builder("Assertions.assertThat(#{any()}).isEqualTo(#{any()})")
                                    .imports("org.assertj.core.api.Assertions")
                                    .javaParser(JavaParser.fromJavaVersion()
                                            .classpath("assertj-core"))
                                    .build()
                                    .apply(getCursor(), m.getCoordinates().replace(), actual, expected);
                        } else if (arguments.size() == 3) {
                            Expression expected = arguments.get(0);
                            Expression actual = arguments.get(1);
                            Expression description = arguments.get(2);

                            m = JavaTemplate.builder("Assertions.assertThat(#{any()}).as(#{any()}).isEqualTo(#{any()})")
                                    .imports("org.assertj.core.api.Assertions")
                                    .javaParser(JavaParser.fromJavaVersion()
                                            .classpath("assertj-core"))
                                    .build()
                                    .apply(getCursor(), m.getCoordinates().replace(), actual, description, expected);
                        }
                        return m;
                    }
                });
    }
}
