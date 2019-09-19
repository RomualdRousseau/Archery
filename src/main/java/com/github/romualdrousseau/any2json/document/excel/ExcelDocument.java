package com.github.romualdrousseau.any2json.document.excel;

import java.io.File;
import java.util.ArrayList;
import java.io.IOException;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.github.romualdrousseau.any2json.IDocument;
import com.github.romualdrousseau.any2json.ISheet;

public class ExcelDocument implements IDocument
{
	public boolean open(File excelFile, String encoding) {
		if(excelFile == null) {
            throw new IllegalArgumentException();
        }

        close();

		try {
			this.workbook = WorkbookFactory.create(excelFile);

			FormulaEvaluator evaluator = this.workbook.getCreationHelper().createFormulaEvaluator();

			for(int i = 0; i < this.workbook.getNumberOfSheets(); i++) {
				this.sheets.add(new ExcelSheet(this.workbook.getSheetAt(i), evaluator));
			}
		}
		catch(NotOLE2FileException x) {
			close();
		}
		catch(InvalidFormatException x) {
			close();
		}
		catch(IOException x) {
			close();
		}

		return this.sheets.size() > 0;
	}

	public void close() {
		this.sheets.clear();
		if(this.workbook == null) {
			return;
		}
		try {
			this.workbook.close();
		}
		catch(IOException x) {
			// ignore exception
		}
		finally {
			this.workbook = null;
		}
	}

	public int getNumberOfSheets() {
		return this.sheets.size();
	}

	public ISheet getSheetAt(int i) {
		return this.sheets.get(i);
	}

	private Workbook workbook = null;
	private ArrayList<ExcelSheet> sheets = new ArrayList<ExcelSheet>();
}
