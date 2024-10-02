package com.github.romualdrousseau.archery;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface TableGraph {

    Table getTable();

    boolean isRoot();

    TableGraph getParent();

    List<? extends TableGraph> children();

    void parse(Consumer<TableGraph> func);

    void parseIf(Consumer<TableGraph> func, Predicate<TableGraph> pred);
}
