package org.any2json.document.text;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;

import org.any2json.IDocument;
import org.any2json.ISheet;
import org.any2json.ITable;
import org.any2json.TableHeader;
import org.any2json.utility.StringUtility;

public class TextDocument extends IDocument
{
	public boolean open(File txtFile, String encoding) {
		if(openWithEncoding(txtFile, null)) {
			return true;
		}
		else {
			return openWithEncoding(txtFile, encoding);
		}
	}
	
	public void close() {
		m_sheet = null;
	}

	public int getNumberOfSheets() {
		return (m_sheet == null) ? 0 : 1;
	}

	public ISheet getSheetAt(int i) {
		return m_sheet;
	}

	private boolean openWithEncoding(File txtFile, String encoding) {
		BufferedReader br = null;
		boolean forceClose = false;

		if(encoding == null) {
			encoding = "UTF-8";
		}

		m_sheet = null;

		try {
			InputStream inputStream = new FileInputStream(txtFile);
			br = new BufferedReader(new InputStreamReader(inputStream, encoding));
			
			TextTable table = new TextTable(br);
			if(table.hasHeaders()) {
				String sheetName = txtFile.getName().replaceFirst("[.][^.]+$", "");
				m_sheet = new TextSheet(sheetName, table);
			}

			br.close();
			br = null;

			return m_sheet != null;
		}
		catch(Exception x) {
			forceClose = true;
			return false;
		}
		finally {
			if(br != null) {
				try {
					br.close();
				}
				catch(IOException x) {
					// ignore exception
				}
			}
			if(forceClose) {
				close();
			}
		}
	}

	private TextSheet m_sheet = null;
}
