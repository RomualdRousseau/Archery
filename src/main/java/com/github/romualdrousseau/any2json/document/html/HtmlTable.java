package com.github.romualdrousseau.any2json.document.html;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.shuju.util.StringUtility;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class HtmlTable extends Table {
    public HtmlTable(Elements htmlElements) {
        processOneTable(htmlElements);
        if (this.rows.size() == 0) {
            return;
        }
        buildDataTable(0, 0, this.rows.get(0).getNumberOfCells(), this.rows.size() - 1, 0);
    }

    public HtmlTable(ArrayList<HtmlRow> rows, int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId) {
        this.rows = rows;
        buildMetaTable(firstColumn, firstRow, lastColumn, lastRow, groupId);
    }

    protected HtmlRow getInternalRowAt(int i) {
        return (i < this.rows.size()) ? this.rows.get(i) : null;
    }

    protected HtmlTable createMetaTable(int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId) {
        return new HtmlTable(this.rows, firstColumn, firstRow, lastColumn, lastRow, groupId);
    }

    private void processOneTable(Elements htmlElements) {
        this.rows = new ArrayList<HtmlRow>();
        if (htmlElements == null || htmlElements.size() == 0) {
            return;
        }
        processRows(htmlElements);
    }

    private void processRows(Elements htmlElements) {
        for (int i = 0; i < htmlElements.size(); i++) {
            Elements htmlTableCells = parseOneRow(htmlElements.get(i));

            String[] tokens = new String[htmlTableCells.size()];
            for (int j = 0; j < htmlTableCells.size(); j++) {
                tokens[j] = StringUtility.cleanToken(htmlTableCells.get(j).text());
            }

            this.rows.add(new HtmlRow(this, tokens, this.lastGroupId));
        }
    }

    private Elements parseOneRow(Element htmlElement) {
        Elements cells = htmlElement.select("th");
        if (cells == null || cells.size() == 0) {
            cells = htmlElement.select("td");
            if (cells == null || cells.size() == 0) {
                return null;
            }
        }
        return cells;
    }

    private ArrayList<HtmlRow> rows;
}
