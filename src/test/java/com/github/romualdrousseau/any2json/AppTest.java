package com.github.romualdrousseau.any2json;

import java.nio.file.Paths;
import java.util.Iterator;

import com.github.romualdrousseau.any2json.classifiers.LayexAndNetClassifierBuilder;
import com.github.romualdrousseau.any2json.classifiers.SimpleClassifierBuilder;
import com.github.romualdrousseau.shuju.json.JSON;

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
                        table = sheet.getTable(classifierFactor);
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

    /**
     * Test to read various documents, tgas the headers and check the first line
     */
    @Test
    public void testTagsVariousDocuments() {
        Document document = null;
        Sheet sheet = null;
        Table table = null;
        Header header = null;
        Row firstRow = null;
        int fileNo = 0;

        ClassifierFactory classifierFactor = new LayexAndNetClassifierBuilder()
            .setModel(JSON.loadJSONObject(getResourcePath("/data/model.json").toString()))
            .build();

        for (String[] expectedValues : scenarios2) {
            int state = 0;
            for (String expectedValue : expectedValues) {
                switch (state) {
                    case 0:
                        document = loadDocument(expectedValue, "CP949");
                        sheet = document.getSheetAt(0);
                        table = sheet.getTable(classifierFactor);
                        assert table != null;
                        firstRow = table.rows().iterator().next();
                        state = 1;
                        break;
                    case 1:
                        header = null;
                        for(Header h: table.headerTags()) {
                            if(h.getTag().getValue().equals("DATE")) {
                                header = h;
                            }
                        }
                        if (header != null) {
                            assertEquals(fileNo + ": Value of <" + header.getTag() + ">", expectedValue,
                                    header.getCellAtRow(firstRow, true).getValue());
                        } else {
                            assertEquals(fileNo + ": Value of <date>", expectedValue, null);
                        }
                        state = 2;
                        break;
                    case 2:
                        header = null;
                        for(Header h: table.headerTags()) {
                            if(h.getTag().getValue().equals("PRODUCT_NAME")) {
                                header = h;
                            }
                        }
                        if (header != null) {
                            assertEquals(fileNo + ": Value of <" + header.getTag() + ">", expectedValue,
                                    header.getCellAtRow(firstRow, true).getValue());
                        } else {
                            assertEquals(fileNo + ": Value of <product-name>", expectedValue, null);
                        }
                        state = 3;
                        break;
                    case 3:
                        header = null;
                        for(Header h: table.headerTags()) {
                            if(h.getTag().getValue().equals("PRODUCT_PACKAGE")) {
                                header = h;
                            }
                        }
                        if (header != null) {
                            assertEquals(fileNo + ": Value of <" + header.getTag() + ">", expectedValue,
                                    header.getCellAtRow(firstRow, true).getValue());
                        } else {
                            assertEquals(fileNo + ": Value of <product-package>", expectedValue, null);
                        }
                        state = 4;
                        break;
                    case 4:
                        header = null;
                        for(Header h: table.headerTags()) {
                            if(h.getTag().getValue().equals("QUANTITY")) {
                                header = h;
                            }
                        }
                        if (header != null) {
                            assertEquals(fileNo + ": Value of <" + header.getTag() + ">", expectedValue,
                                    header.getCellAtRow(firstRow, true).getValue());
                        } else {
                            assertEquals(fileNo + ": Value of <quantity>", expectedValue, null);
                        }
                        state = 5;
                        break;
                    case 5:
                        header = null;
                        for(Header h: table.headerTags()) {
                            if(h.getTag().getValue().equals("unit-price")) {
                                header = h;
                            }
                        }
                        if (header != null) {
                            assertEquals(fileNo + ": Value of <" + header.getTag() + ">", expectedValue,
                                    header.getCellAtRow(firstRow, true).getValue());
                        } else {
                            assertEquals(fileNo + ": Value of <unit-price>", expectedValue, null);
                        }
                        state = 6;
                        break;
                    case 6:
                        header = null;
                        for(Header h: table.headerTags()) {
                            if(h.getTag().getValue().equals("AMOUNT")) {
                                header = h;
                            }
                        }
                        if (header != null) {
                            assertEquals(fileNo + ": Value of <" + header.getTag() + ">", expectedValue,
                                    header.getCellAtRow(firstRow, true).getValue());
                        } else {
                            assertEquals(fileNo + ": Value of <amount>", expectedValue, null);
                        }
                        state = 7;
                        break;
                    case 7:
                        header = null;
                        for(Header h: table.headerTags()) {
                            if(h.getTag().getValue().equals("CUSTOMER_NAME")) {
                                header = h;
                            }
                        }
                        if (header != null) {
                            assertEquals(fileNo + ": Value of <" + header.getTag() + ">", expectedValue,
                                    header.getCellAtRow(firstRow, true).getValue());
                        } else {
                            assertEquals(fileNo + ": Value of <customer-name>", expectedValue, null);
                        }
                        state = 8;
                        break;
                    case 8:
                        header = null;
                        for(Header h: table.headerTags()) {
                            if(h.getTag().getValue().equals("CUSTOMER_TYPE")) {
                                header = h;
                            }
                        }
                        if (header != null) {
                            assertEquals(fileNo + ": Value of <" + header.getTag() + ">", expectedValue,
                                    header.getCellAtRow(firstRow, true).getValue());
                        } else {
                            assertEquals(fileNo + ": Value of <customer-type>", expectedValue, null);
                        }
                        state = 9;
                        break;
                    case 9:
                        header = null;
                        for(Header h: table.headerTags()) {
                            if(h.getTag().getValue().equals("POSTAL_CODE")) {
                                header = h;
                            }
                        }
                        if (header != null) {
                            assertEquals(fileNo + ": Value of <" + header.getTag() + ">", expectedValue,
                                    header.getCellAtRow(firstRow, true).getValue());
                        } else {
                            assertEquals(fileNo + ": Value of <postal-code>", expectedValue, null);
                        }
                        state = 10;
                        break;
                    case 10:
                        header = null;
                        for(Header h: table.headerTags()) {
                            if(h.getTag().getValue().equals("ADDRESS")) {
                                header = h;
                            }
                        }
                        if (header != null) {
                            assertEquals(fileNo + ": Value of <" + header.getTag() + ">", expectedValue,
                                    header.getCellAtRow(firstRow, true).getValue());
                        } else {
                            assertEquals(fileNo + ": Value of <address>", expectedValue, null);
                        }
                        state = 11;
                        break;
                    case 11:
                        header = null;
                        for(Header h: table.headerTags()) {
                            if(h.getTag().getValue().equals("CUSTOMER_NAME")) {
                                header = h;
                            }
                        }
                        if (header != null) {
                            assertEquals(fileNo + ": Value of <" + header.getTag() + ">", expectedValue,
                                    header.getCellAtRow(firstRow, true).getValue());
                        } else {
                            assertEquals(fileNo + ": Value of <customer>", expectedValue, null);
                        }
                        state = 12;
                        break;
                }
            }
            document.close();
            fileNo++;
        }
    }

    private Document loadDocument(String resourceName, String encoding) {
        return DocumentFactory.createInstance(getResourcePath(resourceName).toString(), encoding, null);
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
