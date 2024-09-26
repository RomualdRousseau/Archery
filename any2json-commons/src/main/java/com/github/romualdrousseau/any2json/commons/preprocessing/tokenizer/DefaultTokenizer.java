package com.github.romualdrousseau.any2json.commons.preprocessing.tokenizer;

import java.util.Arrays;
import java.util.List;

import com.github.romualdrousseau.any2json.commons.preprocessing.Text;
import com.github.romualdrousseau.any2json.commons.strings.StringUtils;

public class DefaultTokenizer implements Text.ITokenizer {
    @Override
    public List<String> apply(final String w) {
        final String s = StringUtils.normalizeWhiteSpaces(w).toLowerCase();
        return Arrays.asList(s.split("\s+"));
    }
}
