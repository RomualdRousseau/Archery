package com.github.romualdrousseau.any2json;

import java.util.List;

public abstract class Sheet implements ISheet {
    public ITable findTableWithItelliTag(ITagClassifier classifier) {
        return this.findTableWithItelliTag(classifier, classifier.getRequiredTagList());
    }

    public ITable findTableWithItelliTag(ITagClassifier classifier, String[] requiredTagList) {
        ITable result = null;

        ITable bestTable = this.findTable(classifier.getSampleCount(), classifier.getSampleCount());
        if (bestTable == null) {
            return null;
        }

        List<ITable> tables = this.findTables(classifier.getSampleCount(), classifier.getSampleCount());
        for (ITable table : tables) {
            if (Table.IsEmpty(table) || table.getNumberOfHeaders() < bestTable.getNumberOfHeaders()) {
                continue;
            }

            table.updateHeaderTags(classifier);

            if (this.checkValidity(table, requiredTagList)) {
                result = table;
                break;
            }
        }

        return result;
    }

    public boolean checkValidity(ITable table, String[] requiredTagList) {
        if(requiredTagList == null || requiredTagList.length == 0) {
            return true;
        }

        int mask = 0;
        for(TableHeader header: table.headers()) {
            for(int j = 0; j < requiredTagList.length; j++) {
                if (header.hasTag() && !header.getTag().isUndefined() && header.getTag().getValue().equals(requiredTagList[j])) {
                    mask |= (1 << j);
                }
            }
        }
        return (mask == ((1 << requiredTagList.length) - 1));
    }
}
