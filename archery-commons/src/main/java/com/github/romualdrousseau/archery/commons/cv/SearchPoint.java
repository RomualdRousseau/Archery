package com.github.romualdrousseau.archery.commons.cv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchPoint {

    public SearchPoint(int x, int y) {
        this.x = x;
        this.y = y;
        this.sad = 0;
    }

    public SearchPoint(int x, int y, float sad) {
        this.x = x;
        this.y = y;
        this.sad = sad;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getSAD() {
        return this.sad;
    }

    public void setSAD(int sad) {
        this.sad = sad;
    }

    public boolean equals(SearchPoint o) {
        return this.x == o.x && this.y == o.y;
    }

    public static boolean isValid(SearchPoint[] s) {
        return s[1].getX() >= s[0].getX() && s[1].getY() >= s[0].getY();
    }

    public static int GetArea(SearchPoint[] s) {
        return (s[1].getX() - s[0].getX()) * (s[1].getY() - s[0].getY());
    }

    public static boolean IsOverlap(SearchPoint[] s1, SearchPoint[] s2) {
        return s1[1].getX() >= s2[0].getX() && s1[0].getX() <= s2[1].getX() && s1[1].getY() >= s2[0].getY()
                && s1[0].getY() <= s2[1].getY();
    }

    public static boolean IsInside(SearchPoint[] points, int x, int y) {
        return points[0].getX() <= x && x <= points[1].getX() && points[0].getY() <= y && y <= points[1].getY();
    }

    public static boolean IsDuplicate(SearchPoint[] shape1, List<SearchPoint[]> shapes) {
        boolean foundDuplicate = false;
        for (SearchPoint[] shape2 : shapes) {
            if (shape1[0].equals(shape2[0]) && shape1[1].equals(shape2[1])) {
                foundDuplicate = true;
            }
        }
        return foundDuplicate;
    }

    public static List<SearchPoint[]> RemoveDuplicates(List<SearchPoint[]> shapes) {
        ArrayList<SearchPoint[]> result = new ArrayList<SearchPoint[]>();
        for (SearchPoint[] shape1 : shapes) {
            if (!SearchPoint.IsDuplicate(shape1, result)) {
                result.add(shape1);
            }
        }
        return result;
    }

    public static List<SearchPoint[]> RemoveOverlaps(List<SearchPoint[]> shapes) {
        if (shapes.size() < 2) {
            return shapes;
        }

        Collections.sort(shapes, new Comparator<SearchPoint[]>() {
            public int compare(SearchPoint[] o1, SearchPoint[] o2) {
                return SearchPoint.GetArea(o1) - SearchPoint.GetArea(o2);
            }
        });

        List<SearchPoint[]> result = new ArrayList<SearchPoint[]>();
        result.addAll(shapes);
        for (SearchPoint[] shape1 : shapes) {
            ArrayList<SearchPoint[]> tmp = new ArrayList<SearchPoint[]>();
            for (SearchPoint[] shape2 : result) {
                tmp.addAll(SearchPoint.Clipping(shape1, shape2));
            }
            result = SearchPoint.RemoveDuplicates(tmp);
        }

        return SearchPoint.RemoveDuplicates(result);
    }

    public static List<SearchPoint[]> MergeInX(List<SearchPoint[]> shapes) {
        if (shapes.size() < 2) {
            return shapes;
        }

        ArrayList<SearchPoint[]> result = new ArrayList<SearchPoint[]>();
        for (SearchPoint[] shape1 : shapes)
            if (shape1[0] != null && shape1[1] != null) {
                for (SearchPoint[] shape2 : shapes)
                    if (shape1 != shape2 && shape2[0] != null && shape2[1] != null) {
                        if (shape1[0].getY() == shape2[0].getY() && shape1[1].getY() == shape2[1].getY()) {
                            shape1[0].setX(Math.min(shape1[0].getX(), shape2[0].getX()));
                            shape1[1].setX(Math.max(shape1[1].getX(), shape2[1].getX()));
                            shape2[0] = null;
                            shape2[1] = null;
                        }
                    }
                result.add(shape1);
            }
        return result;
    }

    public static List<SearchPoint[]> MergeInY(List<SearchPoint[]> shapes) {
        if (shapes.size() < 2) {
            return shapes;
        }

        ArrayList<SearchPoint[]> result = new ArrayList<SearchPoint[]>();
        for (SearchPoint[] shape1 : shapes)
            if (shape1[0] != null && shape1[1] != null) {
                for (SearchPoint[] shape2 : shapes)
                    if (shape1 != shape2 && shape2[0] != null && shape2[1] != null) {
                        if (shape1[0].getX() == shape2[0].getX() && shape1[1].getX() == shape2[1].getX()) {
                            shape1[0].setY(Math.min(shape1[0].getY(), shape2[0].getY()));
                            shape1[1].setY(Math.max(shape1[1].getY(), shape2[1].getY()));
                            shape2[0] = null;
                            shape2[1] = null;
                        }
                    }
                result.add(shape1);
            }
        return result;
    }

    public static List<SearchPoint[]> TrimInX(List<SearchPoint[]> shapes, ISearchBitmap bitmap) {
        for (SearchPoint[] shape : shapes) {
            for (int i = shape[0].getX(); i <= shape[1].getX(); i++) {
                if (SearchPoint.columnIsEmpty(shape, i, bitmap)) {
                    shape[0].setX(i + 1);
                } else {
                    break;
                }
            }

            for (int i = shape[1].getX(); i >= shape[0].getX(); i--) {
                if (SearchPoint.columnIsEmpty(shape, i, bitmap)) {
                    shape[1].setX(i - 1);
                } else {
                    break;
                }
            }
        }

        return shapes;
    }

    public static List<SearchPoint[]> TrimInY(List<SearchPoint[]> shapes, ISearchBitmap bitmap) {
        for (SearchPoint[] shape : shapes) {
            for (int i = shape[0].getY(); i <= shape[1].getY(); i++) {
                if (rowIsEmpty(shape, i, bitmap)) {
                    shape[0].setY(i + 1);
                } else {
                    break;
                }
            }

            for (int i = shape[1].getY(); i >= shape[0].getY(); i--) {
                if (rowIsEmpty(shape, i, bitmap)) {
                    shape[1].setY(i - 1);
                } else {
                    break;
                }
            }
        }

        return shapes;
    }

    public static List<SearchPoint[]> ExpandInX(List<SearchPoint[]> shapes, ISearchBitmap bitmap) {
        for (SearchPoint[] shape : shapes) {
            for (int i = shape[0].getX() - 1; i > 0; i--) {
                if (!SearchPoint.columnIsEmpty(shape, i, bitmap)) {
                    shape[0].setX(i - 1);
                } else {
                    break;
                }
            }

            for (int i = shape[1].getX(); i < bitmap.getWidth(); i++) {
                if (!SearchPoint.columnIsEmpty(shape, i, bitmap)) {
                    shape[1].setX(i + 1);
                } else {
                    break;
                }
            }
        }

        return shapes;
    }

    public static List<SearchPoint[]> ExpandInY(List<SearchPoint[]> shapes, ISearchBitmap bitmap) {
        for (SearchPoint[] shape : shapes) {
            for (int i = shape[0].getY() - 1; i > 0; i--) {
                if (!SearchPoint.rowIsEmpty(shape, i, bitmap)) {
                    shape[0].setY(i - 1);
                } else {
                    break;
                }
            }

            for (int i = shape[1].getY(); i < bitmap.getHeight(); i++) {
                if (!SearchPoint.rowIsEmpty(shape, i, bitmap)) {
                    shape[1].setY(i + 1);
                } else {
                    break;
                }
            }
        }

        return shapes;
    }

    public static List<SearchPoint[]> Clipping(SearchPoint[] master, SearchPoint[] slave) {
        ArrayList<SearchPoint[]> result = new ArrayList<SearchPoint[]>();

        result.add(slave);

        // Top to bottom
        for (int i = 0; i < 2; i++) {
            ArrayList<SearchPoint[]> tmp = new ArrayList<SearchPoint[]>();
            for (SearchPoint[] r : result) {
                if (!SearchPoint.IsOverlap(r, master)) {
                    tmp.add(r);
                } else if (r[0].getY() < master[0].getY()) {
                    int d = master[0].getY() - r[0].getY();
                    tmp.add(new SearchPoint[] { new SearchPoint(r[0].getX(), r[0].getY()),
                            new SearchPoint(r[1].getX(), r[0].getY() + d - 1) });
                    tmp.add(new SearchPoint[] { new SearchPoint(r[0].getX(), r[0].getY() + d),
                            new SearchPoint(r[1].getX(), r[1].getY()) });
                } else if (r[1].getY() > master[1].getY()) {
                    int d = r[1].getY() - master[1].getY();
                    tmp.add(new SearchPoint[] { new SearchPoint(r[0].getX(), r[0].getY()),
                            new SearchPoint(r[1].getX(), r[1].getY() - d) });
                    tmp.add(new SearchPoint[] { new SearchPoint(r[0].getX(), r[1].getY() - d + 1),
                            new SearchPoint(r[1].getX(), r[1].getY()) });
                } else {
                    tmp.add(r);
                }
            }
            result = tmp;
        }

        // Left to right
        for (int i = 0; i < 2; i++) {
            ArrayList<SearchPoint[]> tmp = new ArrayList<SearchPoint[]>();
            for (SearchPoint[] r : result) {
                if (!SearchPoint.IsOverlap(r, master)) {
                    tmp.add(r);
                } else if (r[0].getX() < master[0].getX()) {
                    tmp.add(new SearchPoint[] { new SearchPoint(r[0].getX(), r[0].getY()),
                            new SearchPoint(master[0].getX() - 1, r[1].getY()) });
                    tmp.add(new SearchPoint[] { new SearchPoint(master[0].getX(), r[0].getY()),
                            new SearchPoint(r[1].getX(), r[1].getY()) });
                } else if (r[1].getX() > master[1].getX()) {
                    tmp.add(new SearchPoint[] { new SearchPoint(r[0].getX(), r[0].getY()),
                            new SearchPoint(master[1].getX(), r[1].getY()) });
                    tmp.add(new SearchPoint[] { new SearchPoint(master[1].getX() + 1, r[0].getY()),
                            new SearchPoint(r[1].getX(), r[1].getY()) });
                }
            }
            result = tmp;
        }

        result.add(master);

        return result;
    }

    private static boolean columnIsEmpty(SearchPoint[] table, int colIndex, ISearchBitmap bitmap) {
        boolean isEmpty = true;
        for (int i = table[0].getY(); i <= table[1].getY(); i++) {
            if (bitmap.get(colIndex, i) > 0) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }

    private static boolean rowIsEmpty(SearchPoint[] table, int rowIndex, ISearchBitmap bitmap) {
        boolean isEmpty = true;
        for (int i = table[0].getX(); i <= table[1].getX(); i++) {
            if (bitmap.get(i, rowIndex) > 0) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }

    private int x;
    private int y;
    private float sad;
}
