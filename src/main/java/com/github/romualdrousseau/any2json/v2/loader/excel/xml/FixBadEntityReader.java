package com.github.romualdrousseau.any2json.v2.loader.excel.xml;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class FixBadEntityReader extends FilterReader {
    public FixBadEntityReader(Reader in) {
        super(in);
    }

    public int read(char[] buf, int from, int len) throws IOException {
        int last = from;
        while((last - from) < len) {
            int c = in.read();
            if(c == -1) {
                break;
            }

            // Add a semicolon if an entity &#xx is missing the semicolon
            if (state == 0) {
                if (c == '&') {
                    state = 1;
                }
            } else if(state == 1) {
                if (c== '#') {
                    state = 2;
                } else {
                    state = 0;
                }
            } else if(state == 2) {
                if (c >= '0' && c <= '9') {
                    state = 2;
                } else if(c != ';') {
                    buf[last++] = ';';
                    state = 0;
                }
            }

            buf[last++] = (char) c;
        }

        return (last == from) ? -1 : last - from;
    }

    public int read() throws IOException {
        char[] buf = new char[1];
        int result = read(buf, 0, 1);
        return (result == -1) ? -1 : (int) buf[0];
    }

    private int state = 0;
}
