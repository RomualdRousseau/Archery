package com.github.romualdrousseau.any2json.document.excel;

import java.io.File;
import java.util.ArrayList;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;

import com.github.romualdrousseau.any2json.IDocument;
import com.github.romualdrousseau.any2json.ISheet;
import com.github.romualdrousseau.any2json.TableHeader;

public class ExcelDocument extends IDocument
{
	public ExcelDocument(int headerColumns, int headerRows) {
		m_headerColumns = headerColumns;
		m_headerRows = headerRows;
	}

	public boolean open(File excelFile, String encoding) {
		InputStream inputStream = null;
		boolean forceClose = false;

		m_sheets.clear();

		try {
			inputStream = new FileInputStream(excelFile);
			m_workbook = WorkbookFactory.create(inputStream);

			for(int i = 0; i < m_workbook.getNumberOfSheets(); i++) {
				m_sheets.add(new ExcelSheet(m_workbook.getSheetAt(i), m_headerColumns, m_headerRows));
			}

			return true;
		}
		catch(Exception x) {
			forceClose = true;
			return false;
		}
		finally {
			if(forceClose) {
				close();
				if(inputStream != null) {
					try {
						inputStream.close();
					}
					catch(IOException x) {
						// ignore exception
					}
				}
			}
		}
	}
	
	public void close() {
		m_sheets.clear();
		if(m_workbook == null) {
			return;
		}
		try {
			m_workbook.close();
		}
		catch(IOException x) {
			// ignore exception
		}
		finally {
			m_workbook = null;
		}
	}

	public int getNumberOfSheets() {
		return m_sheets.size();
	}

	public ISheet getSheetAt(int i) {
		return m_sheets.get(i);
	}

	private Workbook m_workbook = null;	
	private int m_headerColumns = 0;
	private int m_headerRows = 0;
	private ArrayList<ExcelSheet> m_sheets = new ArrayList<ExcelSheet>();
}
