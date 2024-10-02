package com.github.romualdrousseau.archery;

import java.util.List;

import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.base.BaseTable;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.base.MetaTable;

public interface TableParser extends AutoCloseable {

    Model getModel();

    TableParser setModel(final Model model);

    String getParserOptions();

    TableParser setParserOptions(final String options);

    List<DataTable> getDataTables(final BaseSheet sheet, final List<BaseTable> tables);

    List<MetaTable> getMetaTables(final BaseSheet sheet, final List<BaseTable> tables);
}
