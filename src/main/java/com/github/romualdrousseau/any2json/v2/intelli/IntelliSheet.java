package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.IHeader;
import com.github.romualdrousseau.any2json.v2.ITable;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.SheetBitmap;
import com.github.romualdrousseau.any2json.v2.TableStream;
import com.github.romualdrousseau.any2json.v2.base.Row;
import com.github.romualdrousseau.any2json.v2.base.Sheet;
import com.github.romualdrousseau.any2json.v2.base.Table;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.shuju.cv.Filter;
import com.github.romualdrousseau.shuju.cv.ISearchBitmap;
import com.github.romualdrousseau.shuju.cv.SearchPoint;
import com.github.romualdrousseau.shuju.cv.Template;
import com.github.romualdrousseau.shuju.cv.templatematching.TemplateMatcher;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.RectangleExtractor;

public abstract class IntelliSheet extends Sheet {

    class Graph {
        Table value;
        Graph parent;
        ArrayList<Graph> children = new ArrayList<Graph>();
    }

    @Override
    public ITable getTable(ITagClassifier classifier) {
        return super.getTable(classifier);
    }

    public ITable getIntelliTable(ITagClassifier classifier, List<LayexMatcher> metaLayexes,
            List<LayexMatcher> dataLayexes) {
        List<Table> tables = this.findAllTables(classifier);
        List<DataTable> dataTables = this.getDataTables(tables, dataLayexes);
        List<MetaTable> metaTables = this.getMetaTables(tables, metaLayexes);
        Graph root = this.buildGraph(metaTables, dataTables);

        this.walkThroughGraph(root, 0);

        // return new IntelliTable(root);
        return null;
    }

    private void walkThroughGraph(Graph root, int indent) {
        if (root.value != null) {
            for(int i = 0; i < indent; i++) {
                System.out.print("|- ");
            }
            for (IHeader header : root.value.headers()) {
                System.out.print(header.getName() + " ");
            }
            System.out.println();
        }

        for (Graph child : root.children) {
            walkThroughGraph(child, indent + 1);
        }
    }

    private Graph buildGraph(List<MetaTable> metaTables, List<DataTable> dataTables) {
        Graph root = new Graph();

        // First attach all not snapped metaTables to the root nodes
        for (MetaTable metaTable : metaTables) {
            if (!isSnapped(metaTable, dataTables)) {
                Graph child = new Graph();
                child.parent = root;
                child.value = metaTable;
                root.children.add(child);
                metaTable.setVisited(true);
            }
        }

        // Second attach all snapped metaTables to the closest nodes
        for (MetaTable metaTable : metaTables) {
            if (metaTable.isVisited()) {
                continue;
            }
            Graph parent = findClosestParent(root, metaTable, false);
            Graph child = new Graph();
            child.value = metaTable;
            child.parent = parent;
            parent.children.add(child);
        }

        // Third attach datatables to the closest metadatas
        for (DataTable dataTable : dataTables) {
            Graph parent = findClosestParent(root, dataTable, true);
            Graph child = new Graph();
            child.value = dataTable;
            child.parent = parent;
            parent.children.add(child);
        }

        return root;
    }

    private List<DataTable> getDataTables(List<Table> tables, List<LayexMatcher> dataLayexes) {
        ArrayList<DataTable> result = new ArrayList<DataTable>();
        for (Table table : tables) {
            for (LayexMatcher dataLayex : dataLayexes) {
                if (dataLayex.match(new TableStream(table), null)) {
                    result.add(new DataTable(table, dataLayex));
                    table.setVisited(true);
                }
            }
        }
        return result;
    }

    private List<MetaTable> getMetaTables(List<Table> tables, List<LayexMatcher> metaLayexes) {
        ArrayList<MetaTable> result = new ArrayList<MetaTable>();
        for (Table table : tables) {
            if (table.isVisited()) {
                continue;
            }
            for (LayexMatcher metaLayex : metaLayexes) {
                if (metaLayex.match(new TableStream(table), null)) {
                    result.add(new MetaTable(table, metaLayex));
                } else {
                    result.add(new MetaTable(table));
                }
            }
        }
        return result;
    }

    private List<Table> findAllTables(ITagClassifier classifier) {
        ArrayList<Table> result = new ArrayList<Table>();

        List<SearchPoint[]> rectangles = findAllRectangles(classifier.getSampleCount(), this.getLastRowNum());

        for (SearchPoint[] rectangle : rectangles) {
            int firstColumnNum = rectangle[0].getX();
            int firstRowNum = rectangle[0].getY();
            int lastColumnNum = Math.max(rectangle[1].getX(), this.getLastColumnNum(firstColumnNum, firstRowNum));
            int lastRowNum = rectangle[1].getY();

            Table table = new Table(this, firstColumnNum, firstRowNum, lastColumnNum, lastRowNum, classifier);

            boolean isSplitted = false;
            for (int i = 0; i < table.getNumberOfRows(); i++) {
                Row row = table.getRowAt(i);
                if (row.sparsity() >= 0.5) {
                    int currRowNum = table.getFirstRow() + i;
                    result.add(new Table(table, firstRowNum, currRowNum - 1));
                    result.add(new Table(table, currRowNum, currRowNum));
                    firstRowNum = currRowNum + 1;
                    isSplitted |= true;
                }
            }

            if (!isSplitted) {
                result.add(table);
            } else {
                result.add(new Table(table, firstRowNum, lastRowNum));
            }
        }

        return result;
    }

    public List<SearchPoint[]> findAllRectangles(int columns, int rows) {
        ISearchBitmap original = new SheetBitmap(this, columns, rows);
        ISearchBitmap filtered = original.clone();

        final Filter filter = new Filter(new Template(new float[][] { { 0, 0, 0 }, { 1, 1, 0 }, { 0, 0, 0 } }));
        filter.apply(original, filtered, 0.5);
        List<SearchPoint[]> rectangles = new RectangleExtractor().extractAll(filtered);

        for (SearchPoint[] rectangle : rectangles) {
            rectangle[0] = new SearchPoint(Math.max(0, rectangle[0].getX() - 1), rectangle[0].getY(),
                    rectangle[0].getSAD());
        }

        final TemplateMatcher pointTemplate = new TemplateMatcher(
                new Template(new float[][] { { 0, 0, 0 }, { 0, 1, 1 }, { 0, 0, 0 } }));
        List<SearchPoint> points = pointTemplate.matchAll(filtered, 0, 0, filtered.getWidth(), filtered.getHeight(),
                0.9);
        for (SearchPoint point : points) {
            rectangles
                    .add(new SearchPoint[] { point, new SearchPoint(point.getX() + 1, point.getY(), point.getSAD()) });
        }

        rectangles = SearchPoint.TrimInX(SearchPoint.MergeInX(SearchPoint.RemoveOverlaps(rectangles)), original);

        return rectangles;
    }

    private boolean isSnapped(MetaTable metaTable, List<DataTable> dataTables) {
        for (DataTable dataTable : dataTables) {
            if (distanceBetweenTables(metaTable, dataTable) == 0) {
                return true;
            }
        }
        return false;
    }

    private Graph findClosestParent(Graph root, Table table, boolean rec) {
        Graph result = root;
        double minDist = Double.MAX_VALUE;
        for (Graph child : root.children) {
            if (!(child.value instanceof MetaTable)) {
                continue;
            }

            double dist2 = distanceBetweenTables(child.value, table);

            Graph grandChild = null;
            double dist1 = dist2;
            if(rec) {
                grandChild = findClosestParent(child, table, false);
                dist1 = distanceBetweenTables(grandChild.value, table);
            }

            if (dist1 < dist2) {
                if (dist1 < minDist) {
                    minDist = dist1;
                    result = grandChild;
                }
            } else {
                if (dist2 < minDist) {
                    minDist = dist2;
                    result = child;
                }
            }
        }
        return result;
    }

    private double distanceBetweenTables(Table table1, Table table2) {
        int vx = table2.getFirstColumn() - table1.getFirstColumn();
        int vy = table2.getFirstRow() - table1.getLastRow() - 1;
        if (vy >= 0) {
            return Math.sqrt(vx * vx + vy * vy);
        } else {
            return Double.MAX_VALUE;
        }
    }
}
