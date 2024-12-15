package com.github.romualdrousseau.archery.classifier;

import java.util.List;
import java.util.regex.Pattern;

import com.github.romualdrousseau.archery.Header;
import com.github.romualdrousseau.archery.Model;
import com.github.romualdrousseau.archery.Table;
import com.github.romualdrousseau.archery.TagClassifier;
import com.github.romualdrousseau.archery.commons.preprocessing.tokenizer.ShingleTokenizer;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class SimpleTagClassifier implements TagClassifier {

    public SimpleTagClassifier() {
        this(null, TagClassifier.TagStyle.NONE);
    }

    public SimpleTagClassifier(final Model model, final TagClassifier.TagStyle tagStyle) {
        this.model = model;
        this.tagStyle = tagStyle;

        this.lexicon = (model != null && model.getData().get("lexicon").isPresent())
                ? model.getData().getList("lexicon")
                : StringUtils.getSymbols().stream().toList();
        this.tagTokenizer = new ShingleTokenizer(this.getLexicon(), 1);
    }

    @Override
    public void close() throws Exception {
    }

    public void updateModelData() {
    }

    @Override
    public Model getModel() {
        return this.model;
    }

    @Override
    public TagClassifier setModel(final Model model) {
        this.model = model;
        if (this.model != null) {
            this.updateModelData();
        }
        return this;
    }

    @Override
    public TagClassifier.TagStyle getTagStyle() {
        return this.tagStyle;
    }

    @Override
    public TagClassifier setTagStyle(final TagClassifier.TagStyle mode) {
        this.tagStyle = mode;
        return this;
    }

    @Override
    public List<String> getLexicon() {
        return lexicon;
    }

    @Override
    public TagClassifier setLexicon(final List<String> lexicon) {
        this.lexicon = lexicon;
        return this;
    }

    @Override
    public String ensureTagStyle(final String text) {
        final var cleanText = (this.model == null)
                ? text
                : this.model.getFilterList().stream().reduce(text, (a, x) -> a.replaceAll("(?i)" + x, " "));
        if (this.tagStyle == TagClassifier.TagStyle.SNAKE) {
            this.tagTokenizer.disableLemmatization();
            return StringUtils.toSnake(cleanText, this.tagTokenizer);
        }
        if (this.tagStyle == TagClassifier.TagStyle.CAMEL) {
            this.tagTokenizer.disableLemmatization();
            return StringUtils.toCamel(cleanText, this.tagTokenizer);
        }
        if (cleanText.indexOf(" ") > 0 || cleanText.indexOf("_") > 0) {
            this.tagTokenizer.enableLemmatization();
            return StringUtils.toSnake(cleanText, this.tagTokenizer);
        } else {
            this.tagTokenizer.enableLemmatization();
            return StringUtils.toCamel(cleanText, this.tagTokenizer);
        }
    }

    @Override
    public String predict(final Table table, final Header header) {
        final var name = header.getName();
        final var m = pattern.matcher(name);
        if (m.find()) {
            return m.group(1);
        } else {
            return this.ensureTagStyle(name);
        }
    }

    private final Pattern pattern = Pattern.compile(" \\(\\$(.*)\\)$");
    private final ShingleTokenizer tagTokenizer;

    private Model model;
    private TagClassifier.TagStyle tagStyle;
    private List<String> lexicon;
}
