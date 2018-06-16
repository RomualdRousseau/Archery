package com.github.romualdrousseau.any2json.document.text;

import java.util.ArrayList;
import java.io.IOException;
import java.io.BufferedReader;

import com.github.romualdrousseau.any2json.ITable;
import com.github.romualdrousseau.any2json.IRow;
import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.any2json.util.StringUtility;

class TextTable extends ITable
{
	public TextTable(BufferedReader br) throws IOException {
		processOneTable(br);
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

	private void processOneTable(BufferedReader br) throws IOException {
		if(br == null) {
			return;
		}

		if(processHeaders(br.readLine())) {
			processRows(br);
		}
	}

	private boolean processHeaders(String textLine) {
		if(textLine == null || !StringUtility.checkIfGoodEncoding(textLine)) {
			return false;
		}

		String[] textHeaders = parseOneRow(textLine);
		for(int i = 0; i < textHeaders.length; i++) {
			TableHeader header = new TableHeader()
				.setColumnIndex(i)
				.setNumberOfCells(1)
				.setOriginalName(cleanValueToken(textHeaders[i]))
				.setName(cleanHeaderToken(textHeaders[i]))
				.setTag(null);
			addHeader(header);
		}

		return true;
	}

	private void processRows(BufferedReader br) throws IOException {
		for(String textRow; (textRow = br.readLine()) != null;) {
			String[] tokens = parseOneRow(textRow);
			if(tokens.length == getNumberOfColumns()) {
				m_rows.add(new TextRow(tokens));
			}
		}
	}

	private String[] parseOneRow(String data) {
		String[] tokens = data.split("\t");
		for(int i = 0; i < tokens.length; i++){
			tokens[i] = cleanValueToken(tokens[i]);
		}
		return tokens;
	}

	private ArrayList<IRow> m_rows = new ArrayList<IRow>();
}