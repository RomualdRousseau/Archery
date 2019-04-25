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
	public final static int ROWS_IN_MEMORY = 10000;

	public TextTable(BufferedReader reader) throws IOException {
		this.reader = reader;
		processOneTable();
	}

	public int getNumberOfColumns() {
		return getNumberOfHeaders();
	}

	public int getNumberOfRows() {
		return this.processedCount;
	}

	public IRow getRowAt(int i) {

		ensureRowsInMemory(i);

		if(i < 0 || i >= this.processedCount) {
			throw new ArrayIndexOutOfBoundsException(i);
		}

		return this.rows.get(i % ROWS_IN_MEMORY);
	}

	private void processOneTable() throws IOException {

		if(this.reader == null) {
			return;
		}

		if(!processHeaders(this.reader.readLine())) {
			return;
		}

		processRows(this.reader);
	}

	private boolean processHeaders(String textLine) {

		if(textLine == null) {
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

		this.rows.clear();

		for(String textRow; (textRow = reader.readLine()) != null;) {

			String[] tokens = parseOneRow(textRow);

            String[] cells =  new String[getNumberOfColumns()];
			for(int j = 0; j < Math.min(tokens.length, cells.length); j++) {
                cells[j] = StringUtility.cleanValueToken(tokens[j]);
			}

			this.rows.add(new TextRow(cells));
			processedCount++;

			if(this.rows.size() >= ROWS_IN_MEMORY) {
				return;
			}
		}
	}

	private String[] parseOneRow(String data) {
		return data.split("\t"); // DIRTY: but hey! It is working until now
	}

	private void ensureRowsInMemory(int i) {
		try {
			if(i >= this.processedCount) {
				processRows(this.reader);
			}
		}
		catch(IOException x) {
			throw new ArrayIndexOutOfBoundsException(i);
		}
	}

	private BufferedReader reader;
	private ArrayList<IRow> rows = new ArrayList<IRow>();
	private int processedCount = 0;
}
