package com.github.romualdrousseau.any2json.document.html;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.github.romualdrousseau.any2json.IDocument;
import com.github.romualdrousseau.any2json.ISheet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HtmlDocument implements IDocument
{
	public boolean open(File htmlFile, String encoding) {
        if (openWithEncoding(htmlFile, null)) {
            return true;
        } else {
			return openWithEncoding(htmlFile, encoding);
		}
	}

	public void close() {
		this.sheets.clear();
	}

	public int getNumberOfSheets() {
		return this.sheets.size();
	}

	public ISheet getSheetAt(int i) {
		return this.sheets.get(i);
	}

	private boolean openWithEncoding(File htmlFile, String encoding) {
		close();

		try {
			Document html = Jsoup.parse(htmlFile, encoding);
			Elements htmlTables = html.select("table");
			if(htmlTables != null) {
				for(int i = 0; i < htmlTables.size(); i++) {
					HtmlTable table = new HtmlTable(htmlTables.get(i).select("tr"));
					if(table.hasHeaders()) {
						String sheetName = (i == 0) ? htmlFile.getName().replaceFirst("[.][^.]+$", "") : "Sheet" + (i + 1);
						this.sheets.add(new HtmlSheet(sheetName, table));
					}
				}
			}
		}
		catch(IOException x) {
			close();
		}

		return this.sheets.size() > 0;
	}

	private ArrayList<HtmlSheet> sheets = new ArrayList<HtmlSheet>();
}
