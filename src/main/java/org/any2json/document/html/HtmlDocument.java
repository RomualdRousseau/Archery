package org.any2json.document.html;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.any2json.IDocument;
import org.any2json.ISheet;
import org.any2json.ITable;
import org.any2json.TableHeader;
import org.any2json.utility.StringUtility;

public class HtmlDocument extends IDocument
{
	public boolean open(File htmlFile, String encoding) {
		if(openWithEncoding(htmlFile, null)) {
			return true;
		}
		else {
			return openWithEncoding(htmlFile, encoding);
		}
	}
	
	public void close() {
		m_sheets.clear();
	}

	public int getNumberOfSheets() {
		return m_sheets.size();
	}

	public ISheet getSheetAt(int i) {
		return m_sheets.get(i);
	}

	private boolean openWithEncoding(File htmlFile, String encoding) {
		boolean forceClose = false;

		m_sheets.clear();

		try {
			Document html = Jsoup.parse(htmlFile, encoding);
			Elements htmlTables = html.select("table");

			for(int i = 0; i < htmlTables.size(); i++) {
				HtmlTable table = new HtmlTable(htmlTables.get(i).select("tr"));
				if(table.hasHeaders()) {
					String sheetName = (i == 0) ? htmlFile.getName().replaceFirst("[.][^.]+$", "") : "Sheet" + (i + 1);
					m_sheets.add(new HtmlSheet(sheetName, table));
				}
			}

			return m_sheets.size() > 0;
		}
		catch(Exception x) {
			forceClose = true;
			return false;
		}
		finally {
			if(forceClose) {
				close();
			}
		}
	}

	private ArrayList<HtmlSheet> m_sheets = new ArrayList<HtmlSheet>();
}
