package com.github.romualdrousseau.archery;

import java.util.List;
import java.util.EnumSet;

import org.python.util.PythonInterpreter;

import com.github.romualdrousseau.archery.base.BaseDocument;
import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.transform.op.AutoCrop;
import com.github.romualdrousseau.archery.transform.op.DropColumn;
import com.github.romualdrousseau.archery.transform.op.DropColumnsWhenEntropyLessThan;
import com.github.romualdrousseau.archery.transform.op.DropColumnsWhenFillRatioLessThan;
import com.github.romualdrousseau.archery.transform.op.DropRow;
import com.github.romualdrousseau.archery.transform.op.DropRowsWhenEntropyLessThan;
import com.github.romualdrousseau.archery.transform.op.DropRowsWhenFillRatioLessThan;
import com.github.romualdrousseau.archery.transform.op.RepeatColumnCell;
import com.github.romualdrousseau.archery.transform.op.RepeatRowCell;
import com.github.romualdrousseau.archery.transform.op.SwapRows;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

/**
 * TransformableSheet Class is responsible to apply transformations to a sheet
 * such as remove columns or rows. The
 * transformations are called through a recipe written in Python.
 */
public class TransformableSheet {

    /**
     * This is a private constructor for the TransformableSheet class to apply
     * transformations to the given sheet.
     *
     * @param sheet the sheet to transform
     */
    private TransformableSheet(final BaseSheet sheet) {
        this.sheet = sheet;
    }

    /**
     * This method returns a new instance of TransformableSheet initialized with the
     * given sheet. the TransformableSheet
     * can be used to apply transformation of the sheet such as remove columns or
     * rows.
     *
     * @param sheet the sheet to transform
     * @return a new instance
     */
    public static TransformableSheet of(final BaseSheet sheet) {
        return new TransformableSheet(sheet);
    }

    /**
     * This method performs all the transformations on the sheet by calling the
     * "autoRecipe" method on the sheet's
     * associated document and executing the custom recipe using a Python
     * interpreter.
     */
    public void applyAll() {
        ((BaseDocument) this.sheet.getDocument()).autoRecipe(this.sheet);

        final var recipe = this.sheet.getDocument().getRecipe();
        if (!StringUtils.isBlank(recipe)) {
            try (var pyInterp = new PythonInterpreter()) {
                pyInterp.set("sheet", this);
                pyInterp.exec(pyInterp.compile(recipe));
            }
        }

        if (sheet.isAutoCropEnabled()) {
            AutoCrop.Apply(this.sheet, 0.0f);
        }

        ((BaseDocument) this.sheet.getDocument()).updateParsersAndClassifiers();
    }

    /**
     * This method sets the hints for the associated document by overwriting the
     * default hints.
     *
     * @param hints the hints: INTELLI_EXTRACT, INTELLI_LAYOUT, INTELLI_TAG
     */
    public void setDocumentHints(final String... hints) {
        final var document = (BaseDocument) this.sheet.getDocument();
        final var newHintSet = EnumSet.copyOf(List.of(hints).stream().map(Document.Hint::valueOf).toList());
        document.setRawHints(newHintSet);
    }

    /**
     * This method unsets the hints for the associated document by removing the
     * hints from the default hints.
     *
     * @param hints the hints: INTELLI_EXTRACT, INTELLI_LAYOUT, INTELLI_TAG
     */
    public void unsetDocumentHints(final String... hints) {
        final var document = (BaseDocument) this.sheet.getDocument();
        final var newHintSet = document.getHints();
        final var hintSetToRemove = EnumSet.copyOf(List.of(hints).stream().map(Document.Hint::valueOf).toList());
        newHintSet.removeIf(hintSetToRemove::contains);
        document.setRawHints(newHintSet);
    }

    /**
     * This method sets the extraction threshold for the sheet. The extraction
     * threshold represents the strength of close elements to be combined together.
     * With a value of 0, the elements with the smallest area will be extracted.
     *
     * Prerequisities: INTELLI_EXTRACT
     *
     * @param threshold the bitmap threshold
     *
     * @deprecated use {@link TransformableSheet#setCapillarityThreshold(float)}
     */
    @Deprecated
    public void setBitmapThreshold(final float threshold) {
        this.sheet.setCapillarityThreshold(threshold);
    }

    /**
     * This method sets the extraction threshold for the sheet. The extraction
     * threshold represents the strength of close elements to be combined together.
     * With a value of 0, the elements with the smallest area will be extracted.
     *
     * Prerequisities: INTELLI_EXTRACT
     *
     * @param threshold the extraction threshold
     */
    public void setCapillarityThreshold(final float threshold) {
        this.sheet.setCapillarityThreshold(threshold);
    }

    /**
     * This method sets the reading direction. The reading direction controls how
     * the different elements of a sheets are
     * linked together. The reading direction is a reading directional preferences
     * in perception of visual stimuli
     * depending of the cultures and writing systems.
     *
     * By default, the reading direction is set to GutenbergReading (or Left-Right
     * Then Top-Botton, or LRTB, or normal Western reading order).
     *
     * Prerequisities: INTELLI_LAYOUT
     *
     * @param readingDirection the reading direction
     */
    public void setReadingDirection(final ReadingDirection readingDirection) {
        this.sheet.getDocument().setReadingDirection(readingDirection);
    }

    /**
     * This method sets the parser options for the table parser used by the sheet's
     * associated document.
     *
     * Prerequisities: INTELLI_LAYOUT
     *
     * @param options the parser options
     *
     * @deprecated use {@link TransformableSheet#setParserOptions(String)}
     */
    @Deprecated
    public void setDataTableParserFactory(final String options) {
        this.setParserOptions(options);
    }

    /**
     * This method sets the parser options for the table parser used by the sheet's
     * associated document.
     *
     * Prerequisities: INTELLI_LAYOUT
     *
     * @param options the parser options
     */
    public void setParserOptions(final String options) {
        this.sheet.getDocument().getTableParser().setParserOptions(options);
    }

    /**
     * This method sets the classifer case for the tag classifer used by the sheet's
     * associated document.
     *
     * @param tagCase the classifer case: NONE, SNAKE, CAMEL
     *
     * @deprecated use {@link TransformableSheet#setClassifierTagStyle(String)}
     */
    @Deprecated
    public void setClassifierCaseMode(final String tagCase) {
        if ("SNAKE".equals(tagCase)) {
            this.sheet.getDocument().getTagClassifier().setTagStyle(TagClassifier.TagStyle.SNAKE);
        } else if ("CAMEL".equals(tagCase)) {
            this.sheet.getDocument().getTagClassifier().setTagStyle(TagClassifier.TagStyle.CAMEL);
        } else {
            this.sheet.getDocument().getTagClassifier().setTagStyle(TagClassifier.TagStyle.NONE);
        }
    }

    /**
     * This method sets the classifer case for the tag classifer used by the sheet's
     * associated document.
     *
     * @param tagStyle the classifer case: NONE, SNAKE, CAMEL
     */
    public void setClassifierTagStyle(final String tagStyle) {
        if ("SNAKE".equals(tagStyle)) {
            this.sheet.getDocument().getTagClassifier().setTagStyle(TagClassifier.TagStyle.SNAKE);
        } else if ("CAMEL".equals(tagStyle)) {
            this.sheet.getDocument().getTagClassifier().setTagStyle(TagClassifier.TagStyle.CAMEL);
        } else {
            this.sheet.getDocument().getTagClassifier().setTagStyle(TagClassifier.TagStyle.NONE);
        }
    }

    /**
     * This method enables auto naming the headers of tables. The tables will
     * retain its original name.
     */
    public void enableAutoHeaderName() {
        this.sheet.enableAutoHeaderName();
    }

    /**
     * This method disables auto naming the headers of tables. The tables will
     * retain its original name.
     */
    public void disableAutoHeaderName() {
        this.sheet.disableAutoHeaderName();
    }

    /**
     * This method enables auto cropping of thesheet. The auto cropping drops
     * all empty rows and columns on the edges of the sheets.
     */
    public void enableAutoCrop() {
        this.sheet.enableAutoCrop();
    }

    /**
     * This method disables auto cropping of thesheet. The auto cropping drops
     * all empty rows and columns on the edges of the sheets.
     */
    public void disableAutoCrop() {
        this.sheet.disableAutoCrop();
    }

    /**
     * This method enables auto meta conversion of the sheet. The auto meta
     * conversion converts
     * all data without a match of a layex to meta.
     */
    public void enableAutoMeta() {
        this.sheet.enableAutoMeta();
    }

    /**
     * This method disables auto meta conversion of the sheet. The auto meta
     * conversion converts
     * all data without a match of a layex to meta.
     */
    public void disableAutoMeta() {
        this.sheet.disableAutoMeta();
    }

    /**
     * This method enables the extraction of meta data of the sheet's associated
     * document. meta and table data will be extracted.
     */
    public void enableMeta() {
        this.sheet.enableMeta();
    }

    /**
     * This method disables the extraction of meta data of the sheet's associated
     * document. Only table data will be extracted.
     */
    public void disableMeta() {
        this.sheet.disableMeta();
    }

    /**
     * This method enables the pivot functionality of the sheet's associated
     * document.
     */
    public void enablePivot() {
        this.sheet.enablePivot();
    }

    /**
     * This method disables the pivot functionality of the sheet's associated
     * document.
     */
    public void disablePivot() {
        this.sheet.disablePivot();
    }

    /**
     * This method sets the pivot option for the sheet using the given option
     * string.
     *
     * @param option the option string: "NONE", "WITH_TYPE", "WITH_TYPE_AND_VALUE"
     */
    public void setPivotOption(final String option) {
        this.sheet.setPivotOption(Enum.valueOf(PivotOption.class, option));
    }

    /**
     * This method sets the pivot entities for the sheet using the given list of
     * entities.
     *
     * @param pivotEntityList the list of entities as a list of string
     *
     * @deprecated use {@link TransformableSheet#setPivotKeyEntityList(List<String>)}
     */
    public void setPivotEntityList(final List<String> pivotEntityList) {
        this.sheet.setPivotKeyEntityList(pivotEntityList);
    }

    /**
     * This method sets the pivot key for the sheet using the given list of
     * entities.
     *
     * @param pivotKeyEntityList the list of entities as a list of string
     */
    public void setPivotKeyEntityList(final List<String> pivotKeyEntityList) {
        this.sheet.setPivotKeyEntityList(pivotKeyEntityList);
    }

    /**
     * This method sets the pivot type for the sheet using the given list of
     * entities.
     *
     * @param pivotTypeEntityList the list of entities as a list of string
     */
    public void setPivotTypeEntityList(final List<String> pivotTypeEntityList) {
        this.sheet.setPivotTypeEntityList(pivotTypeEntityList);
    }

    /**
     * This method sets the name of the pivot key header for the sheet using the
     * given format.
     *
     * @param format the format used as {@link String#format(String, Object...)}
     */
    public void setPivotKeyFormat(final String format) {
        this.sheet.setPivotKeyFormat(format);
    }

    /**
     * This method sets the name of the pivot type header for the sheet using the
     * given format.
     *
     * @param format the format used as {@link String#format(String, Object...)}
     */
    public void setPivotTypeFormat(final String format) {
        this.sheet.setPivotTypeFormat(format);
    }

    /**
     * This method sets the name of the pivot value header for the sheet using the
     * given format.
     *
     * @param format the format used as {@link String#format(String, Object...)}
     */
    public void setPivotValueFormat(final String format) {
        this.sheet.setPivotValueFormat(format);
    }

    /**
     * This method sets the name of the group header for the sheet using the given
     * format.
     *
     * @param format the format used as String#format(String, Object...)}
     */
    public void setGroupValueFormat(final String format) {
        this.sheet.setGroupValueFormat(format);
    }

    /**
     * This method sets the name of a column header wihtout name for the sheet using the given
     * format.
     *
     * @param format the format used as String#format(String, Object...)}
     */
    public void setColumnValueFormat(final String format) {
        this.sheet.setColumnValueFormat(format);
    }

    /**
     * This method crops the sheet by dropping all rows and columns on
     * the edges of the sheet.
     */
    public void cropAll() {
        AutoCrop.Apply(this.sheet, 0.0f);
    }

    /**
     * This method crops the sheet by dropping all rows and columns on
     * the edges of the sheet with a fill ratio less than the given
     * minimum ratio.
     *
     * @param minRatio the minimum ratio
     */
    public void cropWhenFillRatioLessThan(final float minRatio) {
        AutoCrop.Apply(this.sheet, minRatio);
    }

    /**
     * This method unmerges all merged cells in the sheet.
     */
    public void unmergeAll() {
        this.sheet.unmergeAll();
    }

    /**
     * This method repeat the value for all the cells in the column specified by the
     * given column index. The value of a given cell is copied to all blank cells
     * below it.
     *
     * @param colIndex the column index
     *
     * @deprecated use {@link TransformableSheet#repeatRowCell(int)}
     */
    @Deprecated
    public void mergeCell(final int colIndex) {
        RepeatColumnCell.Apply(this.sheet, colIndex);
    }

    /**
     * This method repeat the value for all the cells in the column specified by the
     * given column index. The value of a given cell is copied to all blank cells
     * below it.
     *
     * @param colIndex the column index
     */
    public void repeatColumnCell(final int colIndex) {
        RepeatColumnCell.Apply(this.sheet, colIndex);
    }

    /**
     * This method repeat the value for all the cells in the row specified by the
     * given row index. The value of a given cell is copied to all blank cells on
     * the right of it.
     *
     * @param rowIndex the row index
     */
    public void repeatRowCell(final int rowIndex) {
        RepeatRowCell.Apply(this.sheet, rowIndex);
    }

    /**
     * This method patches the cell of the given column and row indices with the
     * given value. The style is copied from an existing cell.
     *
     * @param colIndex1 the column index to copy the style from
     * @param rowIndex1 the row index to copy the style from
     * @param colIndex2 the column index to copy the style to
     * @param rowIndex2 the row index to copy the style to
     * @param value     the value of the destination cell
     */
    public void patchCell(final int colIndex1, final int rowIndex1, final int colIndex2, final int rowIndex2,
            final String value) {
        this.sheet.patchCell(colIndex1, rowIndex1, colIndex2, rowIndex2, value);
    }

    /**
     * This method patches the sequence of cells from the given column and row indices with the
     * given values. The style is copied from an existing cell.
     *
     * @param colIndex1 the column index to copy the style from
     * @param rowIndex1 the row index to copy the style from
     * @param colIndex2 the column index to copy the style to
     * @param rowIndex2 the row index to copy the style to
     * @param values     the values of the destination cells
     */
    public void patchCells(final int colIndex1, final int rowIndex1, final int colIndex2, final int rowIndex2,
            final List<String> values) {
        this.sheet.patchCells(colIndex1, rowIndex1, colIndex2, rowIndex2, values);
    }

    /**
     * This method drops the column specified by the given column index from the
     * sheet.
     *
     * Refrain to use this method as the colIndex is absolute and weak to layout
     * changes.
     *
     * @param colIndex the column index to drop
     */
    public void dropColumn(final int colIndex) {
        DropColumn.Apply(this.sheet, colIndex);
    }

    /**
     * This method drops columns from the sheet that have a fill ratio less than the
     * given minimum ratio.
     *
     * @param minRatio the minimum ratio
     *
     * @deprecated use
     *             {@link TransformableSheet#dropColumnsWhenFillRatioLessThan(float)}
     */
    @Deprecated
    public void dropNullColumns(final float minRatio) {
        DropColumnsWhenFillRatioLessThan.Apply(this.sheet, minRatio);
    }

    /**
     * This method drops columns from the sheet that have a fill ratio less than the
     * given minimum ratio.
     *
     * @param minRatio the minimum ratio
     */
    public void dropColumnsWhenFillRatioLessThan(final float minRatio) {
        DropColumnsWhenFillRatioLessThan.Apply(this.sheet, minRatio);
    }

    /**
     * This method drops columns from the sheet that have a fill ratio less than the
     * given minimum ratio.
     *
     * @param minRatio the minimum ratio
     * @param start    the start column
     * @param stop     the stop column
     */
    public void dropColumnsWhenFillRatioLessThan(final float minRatio, final int start, final int stop) {
        DropColumnsWhenFillRatioLessThan.Apply(this.sheet, minRatio, start, stop);
    }

    /**
     * This method drops columns from the sheet that have an entropy less than the
     * given minimum entropy.
     *
     * @param minEntropy the minimum entropy
     */
    public void dropColumnsWhenEntropyLessThan(final float minEntropy) {
        DropColumnsWhenEntropyLessThan.Apply(this.sheet, minEntropy);
    }

    /**
     * This method drops columns from the sheet that have an entropy less than the
     * given minimum entropy.
     *
     * @param minEntropy the minimum entropy
     * @param start      the start column
     * @param stop       the stop column
     */
    public void dropColumnsWhenEntropyLessThan(final float minEntropy, final int start, final int stop) {
        DropColumnsWhenEntropyLessThan.Apply(this.sheet, minEntropy, start, stop);
    }

    /**
     * This method drops the row specified by the given row index from the sheet.
     *
     * Refrain to use this method as the rowIndex is absolute and weak to layout
     * changes.
     *
     * @param rowIndex the row index to drop
     */
    public void dropRow(final int rowIndex) {
        DropRow.Apply(this.sheet, rowIndex);
    }

    /**
     * This method swap 2 rows from the sheet.
     *
     * @param rowIndex1 the index of row1
     * @param rowIndex2 the index of row2
     */
    public void swapRows(final int rowIndex1, final int rowIndex2) {
        SwapRows.Apply(this.sheet, rowIndex1, rowIndex2);
    }

    /**
     * This method drops rows from the sheet that have a fill ratio less than the
     * given minimum ratio.
     *
     * @param minRatio the minimum ratio
     *
     * @deprecated use
     *             {@link TransformableSheet#dropRowsWhenFillRatioLessThan(float)}
     */
    @Deprecated
    public void dropNullRows(final float minRatio) {
        DropRowsWhenFillRatioLessThan.Apply(this.sheet, minRatio);
    }

    /**
     * This method drops rows from the sheet that have a fill ratio less than the
     * given minimum ratio.
     *
     * @param minRatio the minimum ratio
     */
    public void dropRowsWhenFillRatioLessThan(final float minRatio) {
        DropRowsWhenFillRatioLessThan.Apply(this.sheet, minRatio);
    }

    /**
     * This method drops rows from the sheet that have a fill ratio less than the
     * given minimum ratio.
     *
     * @param minRatio the minimum ratio
     * @param start    the start column
     * @param stop     the stop column
     */
    public void dropRowsWhenFillRatioLessThan(final float minRatio, final int start, final int stop) {
        DropRowsWhenFillRatioLessThan.Apply(this.sheet, minRatio, start, stop);
    }

    /**
     * This method drops rows from the sheet that have an entropy less than the
     * given minimum entropy.
     *
     * @param minEntropy the minimum entropy
     */
    public void dropRowsWhenEntropyLessThan(final float minEntropy) {
        DropRowsWhenEntropyLessThan.Apply(this.sheet, minEntropy);
    }

    /**
     * This method drops rows from the sheet that have an entropy less than the
     * given minimum entropy.
     *
     * @param minEntropy the minimum entropy
     * @param start      the start column
     * @param stop       the stop column
     */
    public void dropRowsWhenEntropyLessThan(final float minEntropy, final int start, final int stop) {
        DropRowsWhenEntropyLessThan.Apply(this.sheet, minEntropy, start, stop);
    }

    /**
     * This method searches for the first occurence of a value that match a regex within a given row.
     *
     * @param regex the regex to search
     * @param rowIndex the row indexc to search
     */
    public List<Integer> searchFirstValue(final String regex, final int rowIndex) {
        return this.sheet.searchCell(regex, rowIndex, 1, 1);
    }

    /**
     * This method searches for the first occurence of a value that match a regex within a given region of rows.
     * The region of rows begins at a given offset and has a given number of rows.
     *
     * @param regex the regex to search
     * @param offset the starting offset of the region to search
     * @param length the number of rows to search
     */
    public List<Integer> searchFirstValue(final String regex, final int offset, final int length) {
        return this.sheet.searchCell(regex, offset, length, 1);
    }

    /**
     * This method searches for the nth occurence of a value that match a regex within a given region of rows.
     * The region of rows begins at a given offset and has a given number of rows.
     *
     * @param regex the regex to search
     * @param offset the starting offset of the region to search
     * @param length the number of rows to search
     * @param nth the nth occurence to match
     */
    public List<Integer> searchNthValue(final String regex, final int offset, final int length, final int nth) {
        return this.sheet.searchCell(regex, offset, length, nth);
    }

    private final BaseSheet sheet;
}
