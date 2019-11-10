package com.github.romualdrousseau.any2json.document.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.github.romualdrousseau.any2json.IDocument;
import com.github.romualdrousseau.any2json.ISheet;
import com.github.romualdrousseau.shuju.util.StringUtility;

public class TextDocument implements IDocument {
    public boolean open(File txtFile, String encoding) {
        if (openWithEncoding(txtFile, encoding)) {
            return true;
        } else {
            return openWithEncoding(txtFile, null);
        }
    }

    public void close() {
        if(this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.reader = null;
        }
        this.sheet = null;
    }

    public int getNumberOfSheets() {
        return (this.sheet == null) ? 0 : 1;
    }

    public ISheet getSheetAt(int i) {
        return this.sheet;
    }

    private boolean openWithEncoding(File txtFile, String encoding) {
        if (txtFile == null) {
            throw new IllegalArgumentException();
        }

        if (encoding == null) {
            encoding = "UTF-8";
        }

        close();

        int rowCount = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(txtFile), encoding))) {
            while (reader.readLine() != null) {
                rowCount++;
            }
        } catch (IOException x) {
            close();
            return false;
        }

        try {
            this.reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(txtFile), encoding));

            if (encoding.equals("UTF-8")) {
                // skip BOM if present
                this.reader.mark(1);
                if (this.reader.read() != StringUtility.BOM_CHAR) {
                    this.reader.reset();
                }
            }

            TextTable table = new TextTable(this.reader, rowCount);
            if (table.hasHeaders() && table.getNumberOfRows() > 0 && this.checkIfGoodEncoding(table)) {
                String sheetName = StringUtility.removeExtension(txtFile.getName());
                this.sheet = new TextSheet(sheetName, table);
            }
        } catch (IOException x) {
            close();
        }

        return this.sheet != null;
    }

    private boolean checkIfGoodEncoding(TextTable table) {
        boolean result = true;
        for (int i = 0; i < table.getNumberOfHeaders(); i++) {
            result &= StringUtility.checkIfGoodEncoding(table.getHeaderAt(i).getName());
        }
        return result;
    }

    private TextSheet sheet = null;
    private BufferedReader reader = null;
}
