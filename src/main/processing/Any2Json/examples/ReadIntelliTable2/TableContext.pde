class Node {
  int col;
  Cell cell;

  Node(int col, Cell cell) {
    this.col = col;
    this.cell = cell;
  }
}

class TableContext extends Context<Cell> {
  public boolean hasHeader = false;
  public boolean firstRow = true;

  ArrayList<Cell> metas = new ArrayList<Cell>();
  ArrayList<Node> pivots = new ArrayList<Node>();
  ArrayList<Node> headers = new ArrayList<Node>();
  ArrayList<Cell> values = new ArrayList<Cell>();

  public void func(Cell cell) {

    int g = getGroup();

    if (g == 0) {
      if (cell.getSymbol().equals("m")) {
        if (hasHeader) {
          pivots.add(new Node(getColumn(), cell));
        } else {
          metas.add(cell);
        }
      } else if (!cell.getSymbol().equals("$")) {
        headers.add(new Node(getColumn(), cell));
        hasHeader = true;
      }
    } else if (g == 1) {
      if (cell.getSymbol().equals("$")) {
        if (firstRow) {
          for (Cell m : metas) {
            print(classifier.getEntityList().get(m.getEntityVector().argmax()), " ");
          }

          for (Node h : headers) {
            print(h.cell.getCleanValue(), " ");
          }

          if (pivots.size() > 0) {
            print(classifier.getEntityList().get(pivots.get(0).cell.getEntityVector().argmax()), " ");
            print("QUANTITY");
          }

          println();

          firstRow = false;
        }

        if (pivots.size() > 0) {
          for (Node p : pivots) {
            for (Cell m : metas) {
              print(m.getCleanValue(), " ");
            }

            for (Node h : headers) {
              if(h.col < values.size()) {
                print(values.get(h.col).getCleanValue(), " ");
              }
            }

            print(p.cell.getCleanValue(), " ");
            if(p.col < values.size()) {
              print(values.get(p.col).getCleanValue());
            }

            println();
          }
        } else {
          for (Cell m : metas) {
            print(m.getCleanValue(), " ");
          }

          for (Node h : headers) {
            if(h.col < values.size()) {
              print(values.get(h.col).getCleanValue(), " ");
            }
          }

          println();
        }

        values.clear();
      } else {
        values.add(cell);
      }
    } else if (g == 2) {
      metas.clear();
      pivots.clear();
      headers.clear();
      values.clear();
    }
  }
}
