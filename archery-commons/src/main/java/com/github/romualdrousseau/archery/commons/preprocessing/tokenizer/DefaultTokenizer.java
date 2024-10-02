package com.github.romualdrousseau.archery.commons.preprocessing.tokenizer;

import java.util.Arrays;
import java.util.List;

import com.github.romualdrousseau.archery.commons.preprocessing.Text;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class DefaultTokenizer implements Text.ITokenizer {
    @Override
    public List<String> apply(final String w) {
        final String s = StringUtils.normalizeWhiteSpaces(w).toLowerCase();
        return Arrays.asList(s.split("\s+"));
    }
}
