package com.github.romualdrousseau.any2json.document.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.github.romualdrousseau.any2json.IDocument;
import com.github.romualdrousseau.any2json.ISheet;
import com.github.romualdrousseau.any2json.util.StringUtility;

public class TextDocument implements IDocument
{
    public boolean open(File txtFile, String encoding) {
        if (openWithEncoding(txtFile, null)) {
            return true;
        } else {
            return openWithEncoding(txtFile, encoding);
        }
	}

	public void close() {
		this.sheet = null;
	}

	public int getNumberOfSheets() {
		return (this.sheet == null) ? 0 : 1;
	}

	public ISheet getSheetAt(int i) {
		return this.sheet;
	}

	private boolean openWithEncoding(File txtFile, String encoding) {
		if(encoding == null) {
			encoding = "UTF-8";
        }

		close();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), encoding))) {
			TextTable table = new TextTable(reader);
			if(table.hasHeaders() && table.getNumberOfRows() > 0 && checkIfGoodEncoding(table)) {
				String sheetName = txtFile.getName().replaceFirst("[.][^.]+$", "");
				this.sheet = new TextSheet(sheetName, table);
			}
		}
		catch(IOException x) {
			close();
		}

		return this.sheet != null;
    }

    private boolean checkIfGoodEncoding(TextTable table) {
        boolean result = true;
        for(int i = 0; i < table.getNumberOfHeaders(); i++) {
            result &= StringUtility.checkIfGoodEncoding(table.getHeaderAt(i).getName());
        }
        return result;
    }

	private TextSheet sheet = null;
}
