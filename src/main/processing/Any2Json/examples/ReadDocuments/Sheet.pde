class Sheet extends Container { //<>//
  ITable table;
  Header[] headers;
  Header currentHeader;
  Cell[][] cells;
  Cell currentCell;
  boolean beautify;
  boolean invalid;

  Sheet(Viewer parent, String value, int index) {
    super(parent, value, 0, index);
    this.beautify = false;
    this.invalid = false;
  }
  
  boolean isAllValid() {
    if (this.headers == null) {
      return true;
    }

    boolean valid = true;
    for (Header header : this.headers) {
      valid &= header.newTag.equals(header.header.getTag().getValue());
    }
    
    return valid;
  }

  void buildTrainingSet() {
    if (this.headers == null) {
      return;
    }

    for (Header header : this.headers) {
      if (!header.newTag.equals(header.header.getTag().getValue())) {
        TrainingSet.addRow(header.header.buildRow(header.newTag, true, true), true);
      }
    }
  }

  void updateTags(boolean reset) {
    if (this.headers == null) {
      return;
    }

    this.table.updateHeaderTags(Brain);

    if (reset) {
      for (Header header : this.headers) {
        header.newTag = header.header.getTag().getValue();
      }
    }
  }

  void update(int x, int y, int w, int h) {
    this.invalid = !this.checkValidity();
    this.focus = this == ((Viewer) this.parent).currentSheet;
    super.update(x, y, w, h);

    if (this.headers == null || this.cells == null) {
      return;
    }

    if (this.beautify) {
      this.updateBeautify();
    } else {
      this.updateNormal();
    }

    if (mousePressed) {
      this.currentHeader = null;
      for(Header header: this.headers) {
        if (header.checkMouse()) {
          this.currentHeader = header;
        }
      }

      this.currentCell = null;   
      for(Cell[] row: this.cells) {
        for (int j = 0; j < row.length; j++) {
          Cell cell = row[j];
          if (cell != null && cell.checkMouse()) {
            this.currentHeader = this.headers[j];
            this.currentCell = cell;
          }
        }
      }
    }
  }

  void updateBeautify() {
    final int xOffset = -(this.x - this.parent.x);
    final int yOffset = -this.parent.h + CELL_HEIGHT * 2;
    final int wCell = this.parent.w / Brain.getTagList().size();
    final int hCell = CELL_HEIGHT;

    for(Header header: this.headers) {
      if (header.header.getTag().getValue().equals("NONE")) {
        header.update(0, 0, 0, 0);
      } else {
        header.update((Brain.getTagList().ordinal(header.header.getTag().getValue()) - 1) * wCell, yOffset, wCell, hCell);
      }
    }

    for (int i = 0; i < this.cells.length; i++) {
      Cell[] row = this.cells[i];
      for (int j = 0; j < row.length; j++) {
        Header header = this.headers[j];
        Cell cell = row[j];
        if (cell == null) {
          continue;
        }
        if (header.header.getTag().getValue().equals("NONE")) {
          cell.update(0, 0, 0, 0);
        } else {
          cell.update(xOffset + (Brain.getTagList().ordinal(header.header.getTag().getValue()) - 1) * wCell, yOffset + (i + 1) * hCell, wCell, hCell);
        }
      }
    }
  }

  void updateNormal() {
    final int xOffset = -(this.x - this.parent.x);
    final int yOffset = -this.parent.h + CELL_HEIGHT * 2;
    final int wCell = this.parent.w / this.headers.length;
    final int hCell = CELL_HEIGHT;

    for (int i = 0; i < this.headers.length; i++) {
      Header header = this.headers[i];
      header.update(xOffset + i * wCell, yOffset, wCell, hCell);
    }

    for (int i = 0; i < this.cells.length; i++) {
      Cell[] row = this.cells[i];
      for (int j = 0; j < row.length; j++) {
        Cell cell = row[j];
        if (cell != null) {
          cell.update(xOffset + j * wCell, yOffset + (i + 1) * hCell, wCell, hCell);
        }
      }
    }
  }

  void show() {
    if (this.headers != null) {
      for(Header header: this.headers) {
        header.show();
      }
    }

    if (this.cells != null) {
      for (int i = 0; i < min(this.cells.length, this.parent.h / CELL_HEIGHT - 3); i++) {
        Cell[] row = this.cells[i];
        for (int j = 0; j < row.length; j++) {
          Cell cell = row[j];
          if (cell != null) {
            cell.show();
          }
        }
      }
    }

    super.show();
  }

  void load() {
    this.unload();

    Viewer viewer = (Viewer) this.parent;
    IDocument document = DocumentFactory.createInstance(viewer.filename, "CP949");
    ISheet sheet = document.getSheetAt(this.col);
    this.table = sheet.findTableWithItelliTag(Brain, new String[] { "QUANTITY", "PRODUCT_NAME" }); 
    if (this.table == null) {
      document.close();
      return;
    }

    int numberOfCols = table.getNumberOfHeaders();
    int numberOfRows = min(50, table.getNumberOfRows());

    this.cells = new Cell[numberOfRows][numberOfCols];
    this.headers = new Header[numberOfCols];

    for (int j = 0; j < numberOfCols; j++) {
      TableHeader header = this.table.getHeaderAt(j);
      this.headers[j] = new Header(this, header, j);
    }

    int k = 0;
    for (int i = 0; i < numberOfRows; i++) {
      IRow row = this.table.getRowAt(i); 
      if (row == null || row.isEmpty(0.5)) {
        continue;
      }

      for (int j = 0; j < numberOfCols; j++) {
        TableCell cell = row.getCell(table.getHeaderAt(j));
        if (cell.hasValue()) {
          this.cells[k][j] = new Cell(this, cell, k, j);
        }
      }

      k++;
    }

    document.close();
  }

  void unload() {
    this.table = null;
    this.cells = null;
    this.headers = null;
    this.currentHeader = null;
    this.currentCell = null;
  }

  boolean checkValidity() {
    if (this.headers == null) {
      return false;
    }

    int mask = 0;
    for(Header header: this.headers) {
      if (header.newTag.equals("QUANTITY")) {
        mask += 1;
      }
      if (header.newTag.equals("PRODUCT_NAME")) {
        mask += 2;
      }
    }
    return (mask == 3);
  }
}
