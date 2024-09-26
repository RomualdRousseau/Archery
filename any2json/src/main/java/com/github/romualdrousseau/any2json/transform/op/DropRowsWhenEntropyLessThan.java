package com.github.romualdrousseau.any2json.transform.op;

import java.util.HashMap;
import java.util.Map.Entry;

import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.commons.strings.StringUtils;

public class DropRowsWhenEntropyLessThan {

    public static void Apply(final BaseSheet sheet, final float minEntropy) {
        for(int i = 0; i <= sheet.getLastRowNum(); i++) {
            final var x = new HashMap<String, Double>();
            final var lastColumnNum = sheet.getLastColumnNum(i);
            int n = 0;
            for(int j = 0; j <= lastColumnNum; j++) {
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
                sheet.markRowAsNull(i);
            }
        }
        sheet.removeAllNullRows();
    }

    public static void Apply(final BaseSheet sheet, final float minEntropy, final int start, final int stop) {
        for(int i = 0; i <= sheet.getLastRowNum(); i++) {
            final var x = new HashMap<String, Double>();
            int n = 0;
            for(int j = start; j <= stop; j++) {
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
                sheet.markRowAsNull(i);
            }
        }
        sheet.removeAllNullRows();
    }

    private static double computeEntropy(final HashMap<String, Double> x, final double n) {
        var result = 0.0;
        for (final Entry<String, Double> e: x.entrySet()) {
            final double p = e.getValue() / n;
            result += p * Math.log(p) / Math.log(2);
        }
        return -result;
    }
}
