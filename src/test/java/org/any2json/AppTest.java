package org.any2json;

import org.any2json.*;

import java.util.List;
import java.util.function.Consumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URL;
import java.net.URISyntaxException;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileDescriptor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        System.setOut(setEncoding(System.out, "UTF-8"));
        System.setErr(setEncoding(System.err, "UTF-8"));
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        System.out.println("대전지오팜");
        System.err.println("대전지오팜");

        IDocument document = null;
        ISheet sheet = null;
        ITable table = null;
        TableHeader header = null;

        /*
        document = loadDocument("/data/대전지오팜.xls", "CP949");
        //document = loadDocument("/data/강원백제.xls", "CP949");
        //document = loadDocument("/data/강원동양.xls", "CP949");
        //document = loadDocument("/data/대구경일.xls", "CP949");
        for(int k = 0; k < 1; k++) {
            sheet = document.getSheetAt(k);
            table = sheet.getTable();
            assert table != null;
            for(int i = 0; i < table.getNumberOfRows(); i++) {
                for(int j = 0; j < table.getNumberOfHeaders(); j++) {
                    header = table.getHeaderAt(j); 
                    String value = table.getRowAt(i).getCellValue(header);
                    System.out.println("{sheet=\"" + sheet.getName() + "\", row=" + i + ", label=\"" + header.name + "\", value=\"" + value + "\"}");
                }
            }
        }
        document.close();
        */

        for(String[] expectedValues: scenarios) {
            int state = 0;
            int headerIndex = 0;
            for(String expectedValue: expectedValues) {
                switch(state) {
                    case 0:
                        document = loadDocument(expectedValue, "CP949");
                        sheet = document.getSheetAt(0);
                        table = sheet.getTable();
                        assert table != null;
                        state = 1;
                        break;
                    case 1:
                        assertEquals("Sheet name", expectedValue, sheet.getName());
                        state = 2;
                        break;
                    case 2:
                        header = table.getHeaderAt(headerIndex);
                        assertEquals("Header name", expectedValue, header.name);
                        state = 3;
                        break;
                    case 3:
                        assertEquals("Value of <" + header.name + ">", expectedValue, table.getRowAt(0).getCellValue(header));
                        headerIndex++;
                        state = 2;
                        break;
                }
            }
            document.close();
        }
    }

    private IDocument loadDocument(String resourceName, String encoding) {
        try {
            URL resourceUrl = getClass().getResource(resourceName);
            assert resourceUrl != null;
            return DocumentFactory.createInstance(Paths.get(resourceUrl.toURI()).toString(), encoding, 20, 10);
        }
        catch(URISyntaxException x) {
            assert false : x.getMessage();
            return null;
        }
    }

    private static PrintStream setEncoding(PrintStream stream, String encoding) {
        try {
            return new PrintStream(stream, true, encoding);
        }
        catch(java.io.UnsupportedEncodingException x) {
            return stream;
        }
    }

    private String[][] scenarios = {
        {"/data/대전지오팜.xls", "대전지오팜", "제품명", "그로리정97.875mg(병)", "규격", "100T", "보험코드", "651601730", "수량", "2", "매출일자", "2016/12/05", "거래처분류", "약국", "우편번호", "331-947", "표준코드", "8806516017325", "주소", "충남 천안시 서북구 쌍용2동"},
        {"/data/대구경일.xls", "매출현황", "년월", "201611", "매입처", "세르비에", "제약사", "세르비에", "상품명", "프로코라란정5mg", "규격/단위", "56T(P)", "수량", "-1", "상대처", "안동성소", "주소", "경상북도안동시서동문로"}
    };
}
