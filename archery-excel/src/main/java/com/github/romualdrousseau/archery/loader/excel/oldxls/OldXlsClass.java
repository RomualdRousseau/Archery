package com.github.romualdrousseau.archery.loader.excel.oldxls;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.DocumentClass;

public class OldXlsClass implements DocumentClass
{
    public Priority getPriority() {
        return Priority.HIGH;
    }

    public Document newInstance() {
        return new OldXlsDocument();
    }
}
