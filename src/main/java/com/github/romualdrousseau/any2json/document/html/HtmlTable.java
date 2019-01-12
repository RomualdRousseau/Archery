package com.github.romualdrousseau.any2json.document.html;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.IRow;
import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.any2json.util.StringUtility;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class HtmlTable extends Table {
    public HtmlTable(Elements htmlElements) {
        processOneTable(htmlElements);
    }

    public int getNumberOfColumns() {
		return getNumberOfHeaders();
	}

	public int getNumberOfRows() {
		return this.rows.size();
	}

	public IRow getRowAt(int i) {
		if(i < 0 || i >= getNumberOfRows()) {
			throw new ArrayIndexOutOfBoundsException(i);
		}

		return this.rows.get(i);
	}

	private void processOneTable(Elements htmlElements) {
		if(htmlElements == null || htmlElements.size() == 0) {
			return;
		}

		if(!processHeaders(htmlElements.get(0))) {
			return;
		}

		processRows(htmlElements);
	}

	private boolean processHeaders(Element htmlElement) {
		if(htmlElement == null) {
			return false;
		}

		Elements htmlTableHeaders = parseOneRow(htmlElement);
		for(int i = 0; i < htmlTableHeaders.size(); i++) {
			addHeader(new TableHeader()
				.setColumnIndex(i)
				.setNumberOfCells(1)
				.setName(StringUtility.cleanValueToken(htmlTableHeaders.get(i).text()))
				.setTag(null));
		}

		return true;
	}

	private void processRows(Elements htmlElements) {
		for(int i = 1; i < htmlElements.size(); i++) {
			Elements htmlTableCells = parseOneRow(htmlElements.get(i));

			String[] tokens = new String[htmlTableCells.size()];
			for(int j = 0; j < htmlTableCells.size(); j++) {
				tokens[j] = StringUtility.cleanValueToken(htmlTableCells.get(j).text());
			}

			if(tokens.length == getNumberOfColumns()) {
				this.rows.add(new HtmlRow(tokens));
			}
		}
	}

	private Elements parseOneRow(Element htmlElement) {
		Elements cells = htmlElement.select("th");
		if(cells == null || cells.size() == 0) {
			cells = htmlElement.select("td");
			if(cells == null || cells.size() == 0) {
				return null;
			}
		}
		return cells;
	}

	private ArrayList<IRow> rows = new ArrayList<IRow>();
}
