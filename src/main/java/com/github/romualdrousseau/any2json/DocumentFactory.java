package com.github.romualdrousseau.any2json;

import java.io.File;
import java.util.UnknownFormatConversionException;

import com.github.romualdrousseau.any2json.document.excel.ExcelDocument;
import com.github.romualdrousseau.any2json.document.html.HtmlDocument;
import com.github.romualdrousseau.any2json.document.text.TextDocument;
import com.github.romualdrousseau.any2json.document.xml.XmlDocument;

public class DocumentFactory
{
    public final static int DEFAULT_SAMPLE_COUNT = 50;
    public final static float DEFAULT_RATIO_EMPTINESS = 0.5f;
    public final static float DEFAULT_ENTITY_PROBABILITY = 0.8f;
    public final static int MAX_META_COUNT = 100;

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

        document = new XmlDocument();
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
