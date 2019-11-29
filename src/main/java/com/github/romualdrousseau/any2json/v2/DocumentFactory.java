package com.github.romualdrousseau.any2json.v2;

import java.io.File;
import java.util.UnknownFormatConversionException;

import com.github.romualdrousseau.any2json.v2.loader.excel.ExcelDocument;
import com.github.romualdrousseau.any2json.v2.loader.xml.XmlDocument;
import com.github.romualdrousseau.any2json.v2.loader.text.TextDocument;
// import com.github.romualdrousseau.any2json.v2.loader.html.HtmlDocument;

public class DocumentFactory {

    public final static int DEFAULT_SAMPLE_COUNT = 50;
    public final static float DEFAULT_RATIO_SCARSITY = 0.5f;
    public final static float DEFAULT_RATIO_DENSITY = 0.5f;
    public final static int SEPARATOR_ROW_THRESHOLD = 10;
    public static final String PIVOT_SUFFIX = "#PIVOT?";
    public static final float DEFAULT_ENTITY_PROBABILITY = 0.5f;
    public static final int MAX_STORE_ROWS = 1000;

	public static Document createInstance(String filePath, String encoding) {
		if(filePath == null) {
            throw new IllegalArgumentException();
        }

		return DocumentFactory.createInstance(new File(filePath), encoding);
	}

	public static Document createInstance(File file, String encoding) {
		if(file == null) {
            throw new IllegalArgumentException();
        }

		Document document = new ExcelDocument();
		if(document.open(file, encoding)) {
			return document;
        }

        document = new XmlDocument();
		if(document.open(file, encoding)) {
			return document;
		}

		// document = new HtmlDocument();
		// if(document.open(file, encoding)) {
		// 	return document;
		// }

		document = new TextDocument();
		if(document.open(file, encoding)) {
			return document;
		}

		throw new UnknownFormatConversionException(file.toString());
	}
}
