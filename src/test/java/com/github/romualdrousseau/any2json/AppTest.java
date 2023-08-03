package com.github.romualdrousseau.any2json;

import java.nio.file.Paths;
import java.util.Iterator;

import com.github.romualdrousseau.any2json.classifier.SimpleClassifierBuilder;

import java.nio.file.Path;
import java.net.URL;
import java.net.URISyntaxException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testReadVariousDocuments() {
        Document document = null;
        Sheet sheet = null;
        Table table = null;
        Header header = null;
        Row firstRow = null;
        int fileNo = 0;

        ClassifierFactory classifierFactor = new SimpleClassifierBuilder()
            .build();

        for (String[] expectedValues : scenarios1) {
            int state = 0;
            Iterator<Header> itHeader = null;
            for (String expectedValue : expectedValues) {
                switch (state) {
                    case 0:
                        document = loadDocument(expectedValue, "UTF-8");
                        sheet = document.getSheetAt(0);
                        sheet.setClassifierFactory(classifierFactor);
                        table = sheet.getTable();
                        assert table != null;
                        itHeader = table.headers().iterator();
                        header = itHeader.next();
                        firstRow = table.rows().iterator().next();
                        state = 1;
                        break;
                    case 1:
                        assertEquals(fileNo + ": Sheet name", expectedValue, sheet.getName());
                        state = 2;
                        break;
                    case 2:
                        assertEquals(fileNo + ": Number of Rows", expectedValue, "" + table.getNumberOfRows());
                        state = 3;
                        break;
                    case 3:
                        assertEquals(fileNo + ": Header name", expectedValue, header.getName());
                        state = 4;
                        break;
                    case 4:
                        assertEquals(fileNo + ": Value of <" + header.getName() + ">", expectedValue,
                        header.getCellAtRow(firstRow).getValue());
                        header = itHeader.next();
                        state = 3;
                        break;
                }
            }
            document.close();
            fileNo++;
        }
    }

    private Document loadDocument(String resourceName, String encoding) {
        return DocumentFactory.createInstance(getResourcePath(resourceName).toFile(), encoding);
    }

    private Path getResourcePath(String resourceName) {
        try {
            URL resourceUrl = getClass().getResource(resourceName);
            assert resourceUrl != null;
            return Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException x) {
            assert false : x.getMessage();
            return null;
        }
    }

    private String[][] scenarios1 = {
        //{ "/data/Singapore - ZUELLIG - Sales - 202101", "Aggregated", "6", ""}
    };

    private String[][] scenarios2 = {
        //{ "/data/HongKong - ZUELLIG - Sales - 20220305.xlsx", "Aggregated", "6", ""}
    };
}
