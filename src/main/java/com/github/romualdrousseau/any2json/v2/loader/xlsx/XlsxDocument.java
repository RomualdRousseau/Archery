package com.github.romualdrousseau.any2json.v2.loader.xlsx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.Document;
import com.github.romualdrousseau.any2json.v2.Sheet;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XlsxDocument implements Document {

    public class MergedRegionLocator extends DefaultHandler {
        private final List<CellRangeAddress> mergedRegions = new ArrayList<>();
        private boolean startValue = false;

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) {
            if ("mergeCell".equals(name) && attributes.getValue("ref") != null) {
                mergedRegions.add(CellRangeAddress.valueOf(attributes.getValue("ref")));
            } else if ("row".equals(name)) {
                // System.out.print("row ");
                for(int i = 0; i < attributes.getLength(); i++) {
                    System.out.print(attributes.getLocalName(i) + " " + attributes.getValue(i) + " ");
                }
                System.out.println();
            }  else if ("c".equals(name)) {
                // System.out.print("cell ");
                for(int i = 0; i < attributes.getLength(); i++) {
                    System.out.print(attributes.getLocalName(i) + " " + attributes.getValue(i) + " ");
                }
                // System.out.println();
            } else if ("v".equals(name)) {
                startValue = true;
                // System.out.print("value ");
            }
        }

        @Override
        public void endElement(String uri, String localName, String name) {
            if(startValue) {
                // System.out.println();
                startValue = false;
            }
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if(startValue) {
                for(int i = 0; i < length; i++) {
                    // System.out.print(ch[start + i]);
                }
            }
        }

        public CellRangeAddress getMergedRegion(int index) {
            return mergedRegions.get(index);
        }

        public List<CellRangeAddress> getMergedRegions() {
            return mergedRegions;
        }
    }

    @Override
    public boolean open(File excelFile, String encoding) {
        try {
            OPCPackage pkg = OPCPackage.open(excelFile.getAbsolutePath(), PackageAccess.READ);
            XSSFReader reader = new XSSFReader(pkg);
            ReadOnlySharedStringsTable sst = new ReadOnlySharedStringsTable(pkg, false);
            for(String t : sst.getItems()) {
                System.out.println(t);
            }
            InputStream sheetData = reader.getSheetsData().next();

            MergedRegionLocator mergedRegionLocator = new MergedRegionLocator();
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(mergedRegionLocator);
            parser.parse(new InputSource(sheetData));

            for(CellRangeAddress r : mergedRegionLocator.getMergedRegions()) {
                System.out.println(r.toString());
            }
            return true;

        } catch (IOException | OpenXML4JException | SAXException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getNumberOfSheets() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Sheet getSheetAt(int i) {
        // TODO Auto-generated method stub
        return null;
    }

}
