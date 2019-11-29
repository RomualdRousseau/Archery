package com.github.romualdrousseau.any2json.v2.loader.excel;

import java.io.File;
import java.util.ArrayList;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Workbook;

import com.github.romualdrousseau.any2json.v2.Document;
import com.github.romualdrousseau.any2json.v2.Sheet;

public class ExcelDocument implements Document {

    public boolean open(File excelFile, String encoding) {
        if (excelFile == null) {
            throw new IllegalArgumentException();
        }

        Workbook workbook = null;
        try {
            this.sheets.clear();

            workbook = WorkbookFactory.create(excelFile);

            // FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                this.sheets.add(new ExcelSheet(workbook.getSheetAt(i)));
            }

            return this.sheets.size() > 0;

        } catch (EncryptedDocumentException | IOException e) {
            e.printStackTrace();
            this.sheets.clear();
            return false;

        } finally {
            if(workbook != null) {
                try {
                    workbook.close();
                } catch (IOException ignore) {
                }
            }
            workbook = null;
        }
    }

    public void close() {
        this.sheets.clear();
    }

    public int getNumberOfSheets() {
        return this.sheets.size();
    }

    public Sheet getSheetAt(int i) {
        return this.sheets.get(i);
    }

    private ArrayList<ExcelSheet> sheets = new ArrayList<ExcelSheet>();
}
