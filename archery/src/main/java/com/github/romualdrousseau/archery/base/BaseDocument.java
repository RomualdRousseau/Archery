package com.github.romualdrousseau.archery.base;

import java.util.EnumSet;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.TableParser;
import com.github.romualdrousseau.archery.TagClassifier;
import com.github.romualdrousseau.archery.Model;
import com.github.romualdrousseau.archery.ReadingDirection;
import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.SheetParser;
import com.github.romualdrousseau.archery.classifier.SimpleTagClassifier;
import com.github.romualdrousseau.archery.config.DynamicPackages;
import com.github.romualdrousseau.archery.parser.sheet.SheetBitmapParser;
import com.github.romualdrousseau.archery.parser.sheet.SimpleSheetParser;
import com.github.romualdrousseau.archery.parser.table.SimpleTableParser;
import com.github.romualdrousseau.archery.readdir.GutenbergDiagonal;
import com.github.romualdrousseau.archery.transform.op.StitchRows;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public abstract class BaseDocument implements Document {

    protected abstract EnumSet<Hint> getIntelliCapabilities();

    public BaseDocument() {
        this.sheetParser = new SimpleSheetParser();
        this.tableParser = new SimpleTableParser(this.model, null);
        this.tagClassifier = new SimpleTagClassifier(this.model, TagClassifier.TagStyle.NONE);
        this.readingDirection = new GutenbergDiagonal();
    }

    @Override
    public void close() {
        try {
            if (this.tableParser != null) {
                this.tableParser.close();
                this.tableParser = null;
            }
            if (this.tagClassifier != null) {
                this.tagClassifier.close();
                this.tagClassifier = null;
            }
        } catch (final Exception x) {
            throw new RuntimeException(x);
        }
    }

    @Override
    public SheetParser getSheetParser() {
        return this.sheetParser;
    }

    @Override
    public TableParser getTableParser() {
        return this.tableParser;
    }

    @Override
    public TagClassifier getTagClassifier() {
        return this.tagClassifier;
    }

    @Override
    public Model getModel() {
        return this.model;
    }

    @Override
    public Document setModel(final Model model) {
        this.model = model;
        return this;
    }

    @Override
    public EnumSet<Hint> getHints() {
        return this.hints;
    }

    @Override
    public Document setHints(final EnumSet<Hint> hints) {
        return this.setRawHints(hints);
    }

    public Document setRawHints(final EnumSet<Hint> hints) {
        this.hints = hints;
        return this;
    }

    @Override
    public String getRecipe() {
        if (StringUtils.isBlank(this.recipe)) {
            this.recipe = String.join("\n", this.model.getData().getList("recipe"));
        }
        return this.recipe;
    }

    @Override
    public Document setRecipe(final String recipe) {
        this.recipe = recipe;
        return this;
    }

    @Override
    public Document setRecipe(final String... recipe) {
        this.recipe = String.join("\n", recipe);
        return this;
    }

    @Override
    public ReadingDirection getReadingDirection() {
        return this.readingDirection;
    }

    @Override
    public BaseDocument setReadingDirection(final ReadingDirection readingDirection) {
        this.readingDirection = readingDirection;
        return this;
    }

    @Override
    public Iterable<Sheet> sheets() {
        return new SheetIterable(this);
    }

    public BaseDocument setSheetParser(final SheetParser sheetParser) {
        this.sheetParser = sheetParser;
        return this;
    }

    public BaseDocument setTableParser(final TableParser tableParser) {
        this.tableParser = tableParser;
        return this;
    }

    public BaseDocument setTagClassifier(final TagClassifier tagClassifier) {
        this.tagClassifier = tagClassifier;
        return this;
    }

    public void autoRecipe(final BaseSheet sheet) {
        if (this.hints.contains(Document.Hint.INTELLI_LAYOUT)) {
            StitchRows.Apply(sheet);
        }
    }

    public void updateParsersAndClassifiers() {
        final var capa = this.getIntelliCapabilities();

        if (capa.contains(Document.Hint.INTELLI_EXTRACT) && this.hints.contains(Document.Hint.INTELLI_EXTRACT)) {
            if (this.sheetParser instanceof SimpleSheetParser) {
                this.sheetParser = new SheetBitmapParser();
            }
        }

        if (capa.contains(Document.Hint.INTELLI_LAYOUT) && this.hints.contains(Document.Hint.INTELLI_LAYOUT)) {
            if (this.tableParser instanceof SimpleTableParser) {
                this.tableParser = DynamicPackages.GetElementParserFactory()
                        .map(x -> x.newInstance(this.model, this.tableParser.getParserOptions()))
                        .orElseGet(() -> new SimpleTableParser(this.model, null));
            }
        }

        if (capa.contains(Document.Hint.INTELLI_TAG) && this.hints.contains(Document.Hint.INTELLI_TAG)) {
            if (this.tagClassifier instanceof SimpleTagClassifier) {
                this.tagClassifier = DynamicPackages.GetTagClassifierFactory()
                        .map(x -> x.newInstance(this.model, this.tagClassifier.getTagStyle()))
                        .orElseGet(() -> new SimpleTagClassifier(this.model, this.tagClassifier.getTagStyle()));
            }
        }
    }

    private Model model = Model.Default.get();
    private EnumSet<Hint> hints = EnumSet.noneOf(Document.Hint.class);
    private String recipe = "";
    private SheetParser sheetParser;
    private TableParser tableParser;
    private TagClassifier tagClassifier;
    private ReadingDirection readingDirection;
}
