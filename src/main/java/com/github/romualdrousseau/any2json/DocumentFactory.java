package com.github.romualdrousseau.any2json;

import java.io.File;
import java.util.UnknownFormatConversionException;

import com.github.romualdrousseau.any2json.document.excel.ExcelDocument;
import com.github.romualdrousseau.any2json.document.html.HtmlDocument;
import com.github.romualdrousseau.any2json.document.text.TextDocument;

public class DocumentFactory
{
	public static IDocument createInstance(String filePath, String encoding) {
		if(filePath == null) {
            throw new IllegalArgumentException();
        }

		return DocumentFactory.createInstance(new File(filePath), encoding);
	}

	public static IDocument createInstance(File file, String encoding) {
		if(file == null) {
            throw new IllegalArgumentException();
        }

		IDocument document = new ExcelDocument();
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
