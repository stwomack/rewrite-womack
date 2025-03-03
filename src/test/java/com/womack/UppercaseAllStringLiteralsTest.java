/*
 * Copyright 2025 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UppercaseAllStringLiteralsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UppercaseAllStringLiterals());
    }

    @DocumentExample
    @Test
    void replaceAppleWithAPPLE() {
        rewriteRun(
          java(
            """
              class Test {
                  String s = "Apple";
              }
              """,
            """
              class Test {
                  String s = "APPLE";
              }
              """
          )
        );
    }
    @Test
    void replaceJanky() {
        rewriteRun(
          java(
            """
              class Test {
                 // We only match the full String literal value
                 String s = "jAnKy";
              }
              """,
            """
              class Test {
                 // We only match the full String literal value
                 String s = "JANKY";
              }
              """
          )
        );
    }
    @Test
    void findInstanceOf() {
        rewriteRun(
          java(
            """
              package com.womack.yolo;
              
              import org.openrewrite.*;
              
              public class YoloClass {
                  static String thisString = "this is a string";
                  String otherString = "ThIS iS A sTrinG";
              
                  public void doStuff() {
                      SteveRecipe steveRecipe = new SteveRecipe();
              
                      if (steveRecipe instanceof Recipe) {
                          System.out.println("yep");
                      }
                  }
              }
              
              class SteveRecipe extends Recipe {
                  @Override
                  public String getDisplayName() {
                      return "Append to release notes";
                  }
              
                  @Override
                  public String getDescription() {
                      return "Adds the specified line to RELEASE.md.";
                  }
              
                  // The shared state between the scanner and the visitor. The custom class ensures we can easily extend the recipe.
                  public static class Accumulator {
                      boolean found;
                  }
                 \s
              }
              """,
            """
              package com.womack.yolo;
              
              import org.openrewrite.*;
              
              public class YoloClass {
                  static String thisString = "this is a string";
                  String otherString = "ThIS iS A sTrinG";
              
                  public void doStuff() {
                      SteveRecipe steveRecipe = new SteveRecipe();
              
                      if (steveRecipe instanceof Recipe) {
                          System.out.println("yep");
                      }
                  }
              }
              
              class SteveRecipe extends Recipe {
                  @Override
                  public String getDisplayName() {
                      return "Append to release notes";
                  }
              
                  @Override
                  public String getDescription() {
                      return "Adds the specified line to RELEASE.md.";
                  }
              
                  // The shared state between the scanner and the visitor. The custom class ensures we can easily extend the recipe.
                  public static class Accumulator {
                      boolean found;
                  }
                 \s
              }
              """
          )
        );
    }

}