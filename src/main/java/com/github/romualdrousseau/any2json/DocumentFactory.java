package com.github.romualdrousseau.any2json;

import java.io.File;
import java.util.UnknownFormatConversionException;

import com.github.romualdrousseau.any2json.loader.excel.xlsx.XlsxDocument;
import com.github.romualdrousseau.any2json.loader.excel.xls.XlsDocument;
import com.github.romualdrousseau.any2json.loader.excel.xml.XmlDocument;
import com.github.romualdrousseau.any2json.loader.text.TextDocument;

public class DocumentFactory {

    public final static int DEFAULT_SAMPLE_COUNT = 200;
    public final static float DEFAULT_RATIO_SIMILARITY = 0.35f;
    public static final String PIVOT_KEY_SUFFIX = "#PIVOT?";
    public static final String PIVOT_VALUE_SUFFIX = "#VALUE?";
    public static final float DEFAULT_ENTITY_PROBABILITY = 0.6f;
    public static final int MAX_STORE_ROWS = 10000;

	public static Document createInstance(final String filePath, final String encoding) {
		return DocumentFactory.createInstance(new File(filePath), encoding, null, true);
	}

	public static Document createInstance(final String filePath, final String encoding, final String password) {
		return DocumentFactory.createInstance(new File(filePath), encoding, password, true);
	}

	public static Document createInstance(final String filePath, final String encoding, final String password, final boolean wellFormed) {
		if(filePath == null) {
            throw new IllegalArgumentException();
        }
		return DocumentFactory.createInstance(new File(filePath), encoding, password, wellFormed);
	}

	public static Document createInstance(final File file, final String encoding) {
		return DocumentFactory.createInstance(file, encoding, null, true);
	}

	public static Document createInstance(final File file, final String encoding, final String password) {
		return DocumentFactory.createInstance(file, encoding, password, true);
	}

	public static Document createInstance(final File file, final String encoding, final String password, final boolean wellFormed) {
		if(file == null) {
            throw new IllegalArgumentException();
        }

        Document document = new XlsxDocument();
		if(document.open(file, encoding, password, false)) {
			return document;
        }

        document = new XlsDocument();
		if(document.open(file, encoding, password, false)) {
			return document;
        }

        document = new XmlDocument();
		if(document.open(file, encoding, password, false)) {
			return document;
		}

		// document = new HtmlDocument();
		// if(document.open(file, encoding, password, false)) {
		// 	return document;
		// }

        document = new TextDocument();
		if(document.open(file, encoding, password, wellFormed)) {
			return document;
		}

		throw new UnknownFormatConversionException(file.toString());
	}
}
