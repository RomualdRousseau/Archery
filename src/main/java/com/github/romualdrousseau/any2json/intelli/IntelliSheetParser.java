package com.github.romualdrousseau.any2json.intelli;

import org.python.util.PythonInterpreter;

import com.github.romualdrousseau.any2json.event.SheetPreparedEvent;

public abstract class IntelliSheetParser {

    public abstract CompositeTable parseAllTables(final IntelliSheet sheet);

    public boolean transformSheet(final IntelliSheet sheet) {
        sheet.stichRows();

        final String recipe = sheet.getClassifierFactory().getLayoutClassifier().get().getRecipe();
        if (recipe != null) {
            try (PythonInterpreter pyInterp = new PythonInterpreter()) {
                pyInterp.set("sheet", sheet);
                pyInterp.exec(recipe);
            }
        }

        return sheet.notifyStepCompleted(new SheetPreparedEvent(sheet));
    }
}
