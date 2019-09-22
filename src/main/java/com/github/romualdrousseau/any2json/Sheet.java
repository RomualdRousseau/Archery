package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.shuju.cv.ISearchBitmap;

import org.apache.poi.ss.formula.eval.NotImplementedException;

public abstract class Sheet implements ISheet {
    public ISearchBitmap getSearchBitmap(int headerColumns, int headerRows) {
        throw new NotImplementedException("Not implemented");
    }

    public ITable findTableWithIntelliTag(ITagClassifier classifier) {
        return this.findTableWithIntelliTag(classifier, classifier.getRequiredTagList());
    }

    public ITable findTableWithIntelliTag(ITagClassifier classifier, String[] requiredTagList) {
        Iterable<ITable> tables = this.findTables(classifier.getSampleCount(), classifier.getSampleCount());
        return new IntelliTable(this, tables, classifier, requiredTagList);
    }
}
