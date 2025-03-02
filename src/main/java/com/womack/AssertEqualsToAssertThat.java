/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.womack;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = false)
public class AssertEqualsToAssertThat extends Recipe {
    private static final MethodMatcher MATCHER = new MethodMatcher("org.junit.jupiter.api.Assertions assertEquals(..)");

    @Override
    public String getDisplayName() {
        // language=markdown
        return "JUnit `assertEquals()` to Assertj `assertThat()`";
    }

    @Override
    public String getDescription() {
        return "Use AssertJ assertThat instead of JUnit assertEquals().";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        TreeVisitor<?, ExecutionContext> preconditions = createPreconditions();
        JavaIsoVisitor<ExecutionContext> visitor = createVisitor();
        return Preconditions.check(preconditions, visitor);
    }

    private TreeVisitor<?, ExecutionContext> createPreconditions() {
        return new UsesType<>("org.junit.jupiter.api.Assertions", null);
    }

    private JavaIsoVisitor<ExecutionContext> createVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation modifiedMethod = super.visitMethodInvocation(method, ctx);
                if (!isMethodMatch(modifiedMethod)) {
                    return modifiedMethod;
                }

                List<Expression> arguments = modifiedMethod.getArguments();
                addAssertJImport();
                removeJUnitImport();

                if (isTwoArguments(arguments)) {
                    modifiedMethod = handleTwoArguments(modifiedMethod, arguments);
                } else if (isThreeArguments(arguments)) {
                    modifiedMethod = handleThreeArguments(modifiedMethod, arguments);
                }

                return modifiedMethod;
            }

            private boolean isMethodMatch(J.MethodInvocation method) {
                return MATCHER.matches(method);
            }

            private void addAssertJImport() {
                maybeAddImport("org.assertj.core.api.Assertions");
            }

            private void removeJUnitImport() {
                maybeRemoveImport("org.junit.jupiter.api.Assertions");
            }

            private boolean isTwoArguments(List<Expression> arguments) {
                return arguments.size() == 2;
            }

            private boolean isThreeArguments(List<Expression> arguments) {
                return arguments.size() == 3;
            }

            private J.MethodInvocation handleTwoArguments(J.MethodInvocation method, List<Expression> arguments) {
                Expression expected = getExpectedArgument(arguments);
                Expression actual = getActualArgument(arguments);
                JavaTemplate template = createTwoArgTemplate();
                return template.apply(getCursor(), method.getCoordinates().replace(), actual, expected);
            }

            private J.MethodInvocation handleThreeArguments(J.MethodInvocation method, List<Expression> arguments) {
                Expression expected = getExpectedArgument(arguments);
                Expression actual = getActualArgument(arguments);
                Expression description = getDescriptionArgument(arguments);
                JavaTemplate template = createThreeArgTemplate();
                return template.apply(getCursor(), method.getCoordinates().replace(), actual, description, expected);
            }

            private Expression getExpectedArgument(List<Expression> arguments) {
                return arguments.get(0);
            }

            private Expression getActualArgument(List<Expression> arguments) {
                return arguments.get(1);
            }

            private Expression getDescriptionArgument(List<Expression> arguments) {
                return arguments.get(2);
            }

            private JavaTemplate createTwoArgTemplate() {
                return JavaTemplate.builder("Assertions.assertThat(#{any()}).isEqualTo(#{any()})")
                        .imports("org.assertj.core.api.Assertions")
                        .javaParser(createJavaParser()) // Correct: Pass the Builder
                        .build();
            }

            private JavaTemplate createThreeArgTemplate() {
                return JavaTemplate.builder("Assertions.assertThat(#{any()}).as(#{any()}).isEqualTo(#{any()})")
                        .imports("org.assertj.core.api.Assertions")
                        .javaParser(createJavaParser()) // Correct: Pass the Builder
                        .build();
            }

            private JavaParser.Builder createJavaParser() {
                return JavaParser.fromJavaVersion().classpath("assertj-core");
            }
        };
    }
}