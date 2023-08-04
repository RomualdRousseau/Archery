package com.github.romualdrousseau.any2json.intelli;

import org.python.util.PythonInterpreter;

import com.github.romualdrousseau.any2json.base.BaseSheetParser;

public abstract class TransformableSheetParser implements BaseSheetParser {

    public void transformSheet(TransformableSheet sheet) {
        sheet.stichRows();

        final String recipe = sheet.getClassifierFactory().getLayoutClassifier().get().getRecipe();
        if (recipe != null) {
            try(PythonInterpreter pyInterp = new PythonInterpreter()) {
                pyInterp.set("sheet", sheet);
                pyInterp.exec(recipe);
            }
        }
    }
}
