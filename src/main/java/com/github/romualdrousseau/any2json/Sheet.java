package com.github.romualdrousseau.any2json;

import java.util.List;

public abstract class Sheet implements ISheet {
    public ITable findTableWithItelliTag(ITagClassifier classifier) {
        return this.findTableWithItelliTag(classifier, null);
    }

    public ITable findTableWithItelliTag(ITagClassifier classifier, String[] requiredTags) {
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

            if (requiredTags == null || this.checkValidity(table, requiredTags)) {
                result = table;
                break;
            }
        }

        if(result == null && requiredTags != null) {
            result = this.findTableWithItelliTag(classifier);
        }

        return result;
    }

    public boolean checkValidity(ITable table, String[] requiredTags) {
        int mask = 0;
        for(TableHeader header: table.headers()) {
            for(int j = 0; j < requiredTags.length; j++) {
                if (header.hasTag() && !header.getTag().isUndefined() && header.getTag().getValue().equals(requiredTags[j])) {
                    mask |= (1 << j);
                }
            }
        }
        return (mask == ((1 << requiredTags.length) - 1));
    }
}
