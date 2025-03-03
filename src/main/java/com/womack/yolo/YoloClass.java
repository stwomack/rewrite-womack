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

}
