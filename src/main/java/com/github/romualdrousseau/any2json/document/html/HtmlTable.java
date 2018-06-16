package com.github.romualdrousseau.any2json.document.html;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.romualdrousseau.any2json.ITable;
import com.github.romualdrousseau.any2json.IRow;
import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.any2json.util.StringUtility;

class HtmlTable extends ITable
{
	public HtmlTable(Elements htmlElements) {
		processOneTable(htmlElements);
	}

	public int getNumberOfColumns() {
		return getNumberOfHeaders();	
	}

	public int getNumberOfRows() {
		return m_rows.size();	
	}

	public IRow getRowAt(int i) {
		return m_rows.get(i);
	}

	public ITable addRow(IRow row) {
		m_rows.add(row);
		return this;
	}

	private void processOneTable(Elements htmlElements) {
		if(htmlElements == null || htmlElements.size() == 0) {
			return;
		}

		if(processHeaders(htmlElements.get(0))) {
			processRows(htmlElements);
		}
	}

	private boolean processHeaders(Element htmlElement) {
		if(htmlElement == null) {
			return false;
		}

		Elements htmlTableHeaders = parseOneRow(htmlElement);
		for(int i = 0; i < htmlTableHeaders.size(); i++) {
			TableHeader header = new TableHeader()
				.setColumnIndex(i)
				.setNumberOfCells(1)
				.setOriginalName(cleanValueToken(htmlTableHeaders.get(i).text()))
				.setName(cleanHeaderToken(htmlTableHeaders.get(i).text()))
				.setTag(null);
			addHeader(header);
		}

		return true;
	}

	private void processRows(Elements htmlElements) {
		if(htmlElements == null || htmlElements.size() == 0) {
			return;
		}

		for(int i = 1; i < htmlElements.size(); i++) {
			Elements htmlTableCells = parseOneRow(htmlElements.get(i));

			String[] tokens = new String[htmlTableCells.size()];
			for(int j = 0; j < htmlTableCells.size(); j++) {
				tokens[j] = cleanValueToken(htmlTableCells.get(j).text());
			}

			if(tokens.length == getNumberOfColumns()) {
				m_rows.add(new HtmlRow(tokens));
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

	private ArrayList<IRow> m_rows = new ArrayList<IRow>();
}