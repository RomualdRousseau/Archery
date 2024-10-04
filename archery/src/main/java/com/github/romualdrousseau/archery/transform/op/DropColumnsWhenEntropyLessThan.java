package com.github.romualdrousseau.archery.transform.op;

import java.util.HashMap;
import java.util.Map.Entry;

import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class DropColumnsWhenEntropyLessThan {

    public static void Apply(final BaseSheet sheet, final float minEntropy) {
        for(int j = 0; j <= sheet.getLastColumnNum(); j++) {
            final var x = new HashMap<String, Double>();
            var n = 0;
            for(int i = 0; i <= sheet.getLastRowNum(); i++) {
                if(sheet.hasCellDataAt(j, i)) {
                    final var value = sheet.getCellDataAt(j, i);
                    if (!StringUtils.isFastBlank(value)) {
                        x.put(value, x.getOrDefault(value, 0.0) + 1.0);
                        n++;
                    }
                }
            }
            final var e = (float) computeEntropy(x, n);
            if (e <= minEntropy) {
                sheet.markColumnAsNull(j);
            }
        }
        sheet.removeAllNullColumns();
    }

    public static void Apply(final BaseSheet sheet, final float minEntropy, final int start, final int stop) {
        for(int j = 0; j <= sheet.getLastColumnNum(); j++) {
            final var x = new HashMap<String, Double>();
            var n = 0;
            for(int i = start; i <= stop; i++) {
                if(sheet.hasCellDataAt(j, i)) {
                    final var value = sheet.getCellDataAt(j, i);
                    if (!StringUtils.isFastBlank(value)) {
                        x.put(value, x.getOrDefault(value, 0.0) + 1.0);
                        n++;
                    }
                }
            }
            final var e = (float) computeEntropy(x, n);
            if (e <= minEntropy) {
                sheet.markColumnAsNull(j);
            }
        }
        sheet.removeAllNullColumns();
    }

    private static double computeEntropy(HashMap<String, Double> x, double n) {
        var result = 0.0;
        for (final Entry<String, Double> e: x.entrySet()) {
            double p = e.getValue() / n;
            result += p * Math.log(p) / Math.log(2);
        }
        return -result;
    }
}
