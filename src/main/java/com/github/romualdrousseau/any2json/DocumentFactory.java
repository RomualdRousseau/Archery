package com.github.romualdrousseau.any2json;

import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UnknownFormatConversionException;

import com.github.romualdrousseau.any2json.document.excel.ExcelDocument;
import com.github.romualdrousseau.any2json.document.html.HtmlDocument;
import com.github.romualdrousseau.any2json.document.text.TextDocument;

public class DocumentFactory
{
	public static IDocument createInstance(String filePath, String encoding, int headerColumns, int headerRows) {
		return DocumentFactory.createInstance(new File(filePath), encoding, headerColumns, headerRows);
	}

	public static IDocument createInstance(File file, String encoding, int headerColumns, int headerRows) {
		IDocument document;

		document = new ExcelDocument(headerColumns, headerRows);
		if(document.open(file, encoding)) {
			return document;
		}

		document = new HtmlDocument();
		if(document.open(file, encoding)) {
			return document;
		}

		document = new TextDocument();
		if(document.open(file, encoding)) {
			return document;
		}

		throw new UnknownFormatConversionException(file.toString());
	}
}