package com.github.romualdrousseau.any2json;

import java.util.List;

import com.github.romualdrousseau.shuju.cv.ISearchBitmap;

import org.apache.poi.ss.formula.eval.NotImplementedException;

public abstract class Sheet implements ISheet {
    public ISearchBitmap getSearchBitmap(int headerColumns, int headerRows) {
        throw new NotImplementedException("Not implemented");
    }

    public ITable findTableWithIntelliTag(ITagClassifier classifier) {
        List<ITable> tables = this.findTables(classifier.getSampleCount(), classifier.getSampleCount());
        ITable table = null;

        if (tables.size() == 0) {
            table = getTable();
            if(table != null) {
                table.enableIntelliTable(false);
                table.updateHeaderTags(classifier, false);
            }
        }
        else if (tables.size() == 1) {
            table = tables.get(0);
            table.updateHeaderTags(classifier, false);
        } else {
            table = new IntelliTable(this, tables, classifier);
            if(table.getNumberOfRows() == 0) {
                table = tables.get(0);
                table.updateHeaderTags(classifier, false);
            }
        }

        return table;
    }
}
