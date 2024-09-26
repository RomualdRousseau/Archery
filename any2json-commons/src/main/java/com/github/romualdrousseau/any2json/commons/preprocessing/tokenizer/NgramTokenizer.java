package com.github.romualdrousseau.any2json.commons.preprocessing.tokenizer;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.commons.preprocessing.Text;
import com.github.romualdrousseau.any2json.commons.strings.StringUtils;

public class NgramTokenizer implements Text.ITokenizer {

    private final int n;

    public NgramTokenizer(final int n) {
        this.n = n;
    }

    @Override
    public List<String> apply(final String w) {
        String s = StringUtils.normalizeWhiteSpaces(w);

        // Join by space and underscore
        s = s.replaceAll("[\\s_]+", "").trim();

        // Fill up with ? to have at least one token
        while (s.length() < this.n) {
            s += "?";
        }

        final ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < s.length() - this.n + 1; i++) {
            final String ss = s.substring(i, i + this.n);
            result.add(ss.toLowerCase());
        }

        return result;
    }
}
