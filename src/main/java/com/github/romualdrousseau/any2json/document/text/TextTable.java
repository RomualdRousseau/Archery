package com.github.romualdrousseau.any2json.document.text;

import java.util.ArrayList;
import java.io.IOException;
import java.io.BufferedReader;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.any2json.IRow;
import com.github.romualdrousseau.any2json.util.StringUtility;

class TextTable extends Table
{
	public TextTable(BufferedReader reader) throws IOException {
		processOneTable(reader);
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

	private void processOneTable(BufferedReader reader) throws IOException {
		if(reader == null) {
			return;
		}

		if(!processHeaders(reader.readLine())) {
			return;
		}
		
		processRows(reader);
	}

	private boolean processHeaders(String textLine) {
		if(textLine == null || !StringUtility.checkIfGoodEncoding(textLine)) {
			return false;
		}

		String[] textHeaders = parseOneRow(textLine);
		for(int i = 0; i < textHeaders.length; i++) {
			addHeader(new TableHeader()
				.setColumnIndex(i)
				.setNumberOfCells(1)
				.setName(StringUtility.cleanValueToken(textHeaders[i]))
				.setTag(null));
		}

		return true;
	}

	private void processRows(BufferedReader reader) throws IOException {
		for(String textRow; (textRow = reader.readLine()) != null;) {
			String[] tokens = parseOneRow(textRow);

			if(tokens.length != getNumberOfColumns()) {
				continue;
			}

			for(int j = 0; j < tokens.length; j++) {
				tokens[j] = StringUtility.cleanValueToken(tokens[j]);
			}

			this.rows.add(new TextRow(tokens));
		}
		
	}

	private String[] parseOneRow(String data) {
		return data.split("\t"); // DIRTY: but hey! It is working until now
	}

	private ArrayList<IRow> rows = new ArrayList<IRow>();
}