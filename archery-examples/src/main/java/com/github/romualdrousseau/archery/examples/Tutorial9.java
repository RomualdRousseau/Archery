package com.github.romualdrousseau.archery.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.DocumentFactory;
import com.github.romualdrousseau.archery.TableGraph;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.parser.LayexTableParser;

public class Tutorial9 implements Runnable {

    public static void main(final String[] args) {
        new Tutorial9().run();
    }

    @Override
    public void run() {
        final var builder = Common.loadModelBuilderFromGitHub("sales-english");

        final var model = builder
                .setTableParser(this.customTableParser())
                .build();

        final var file = Common.loadData("AG120-N-074.pdf", this.getClass());
        try (final var doc = DocumentFactory.createInstance(file, "UTF-8")
                .setModel(model)
                .setHints(EnumSet.of(Document.Hint.INTELLI_LAYOUT))) {

            doc.sheets().forEach(s -> Common.addSheetDebugger(s).getTableGraph().ifPresent(this::visitTable));
        }
    }

    private void visitTable(final TableGraph parent) {
        parent.children().forEach(c -> {
            final var table = c.getTable();
            if (table instanceof DataTable) {
                Common.printHeaders(table.headers());
                Common.printRows(table.rows());
            }
            if (c.children().size() > 0) {
                this.visitTable(c);
            }
        });
    }

    private LayexTableParser customTableParser() {
        return new LayexTableParser(
                List.of(""),
                List.of(
                        "((vv$)(v+$v+$))(()(.+$)())+()",
                        "(()(.+$))(()(.+$)())+()"));
    }
}
