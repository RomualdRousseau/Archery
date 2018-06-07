package org.any2json.document.text;

import java.util.ArrayList;
import java.io.IOException;
import java.io.BufferedReader;

import org.any2json.ITable;
import org.any2json.IRow;
import org.any2json.TableHeader;
import org.any2json.utility.StringUtility;

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
			TableHeader header = new TableHeader();
			header.columnIndex = i;
			header.numberOfCells = 1;
			header.name = textHeaders[i];
			header.tag = null;
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
			String token = tokens[i];
			token = StringUtility.normalizeWhiteSpaces(token);
			token = StringUtility.trim(token, StringUtility.WHITE_SPACES + "\"");
			tokens[i] = token;
		}
		return tokens;
	}

	private ArrayList<IRow> m_rows = new ArrayList<IRow>();
}