class Sheet extends Container { //<>//
  Cell[][] cells;
  Cell[] headers;
  Header currentHeader;
  Cell currentCell;
  boolean beautify;
  boolean invalid;

  Sheet(Viewer parent, String value, int index) {
    super(parent, value, 0, index);
    this.beautify = false;
    this.invalid = false;
  }

  void buildTrainingSet() {
    if (this.cells == null) {
      return;
    }

    for (int j = 0; j < this.headers.length; j++) {
      Header header = (Header) this.headers[j];
      if (header == null) {
        continue;
      }

      NlpHelper.ngrams.registerWord(header.cleanValue);
      float[] input = TrainingSet.buildInput(header, header.getConflicts(true));  
      float[] target = TrainingSet.buildTarget(header);
      TrainingSet.add(input, target);
    }
  }

  void updateTags(boolean reset) {
    if (this.cells == null) {
      return;
    }

    for (int j = 0; j < this.headers.length; j++) {
      Header header = (Header) this.headers[j];
      if (header != null) {
        header.updateTag(reset, false);
      }
    }

    for (int j = 0; j < this.headers.length; j++) {
      Header header = (Header) this.headers[j];
      if (header != null) {
        header.updateTag(reset, true);
      }
    }
  }

  void update(int x, int y, int w, int h) {
    this.invalid = !this.checkValidity();
    this.focus = this == ((Viewer) this.parent).currentSheet;
    super.update(x, y, w, h);

    if (this.cells == null) {
      return;
    }

    if (this.beautify) {
      this.updateBeautify();
    } else {
      this.updateNormal();
    }

    if (mousePressed) {
      this.currentHeader = null;
      this.currentCell = null;   
      for (int i = 0; i < this.cells.length; i++) {
        Cell[] row = this.cells[i];
        for (int j = 0; j < row.length; j++) {
          Cell cell = row[j];
          if (cell != null && cell.checkMouse()) {
            this.currentHeader = (Header) this.headers[j];
            this.currentCell = cell;
          }
        }
      }
    }
  }

  void updateBeautify() {
    Tag[] tags = Tag.values();
    final int xOffset = -(this.x - this.parent.x);
    final int yOffset = -this.parent.h + CELL_HEIGHT * 2;
    final int wCell = this.parent.w / tags.length;
    final int hCell = CELL_HEIGHT;

    for (int j = 0; j < headers.length; j++) { // skip NONE
      Header header = (Header) this.headers[j];
      if (header == null) {
        continue;
      }

      if (header.orgTag.equals(Tag.NONE)) {
        header.update(0, 0, 0, 0);
      } else {
        header.update((header.orgTag.ordinal() - 1) * wCell, yOffset, wCell, hCell);
      }
    }

    for (int i = 1; i < this.cells.length; i++) {
      Cell[] row = this.cells[i];
      for (int j = 0; j < row.length; j++) {
        Header header = (Header) this.headers[j];
        Cell cell = row[j];
        if (cell == null) {
          continue;
        }

        if (header == null || header.orgTag.equals(Tag.NONE)) {
          cell.update(0, 0, 0, 0);
        } else {
          cell.update(xOffset + (header.orgTag.ordinal() - 1) * wCell, yOffset + i * hCell, wCell, hCell);
        }
      }
    }
  }

  void updateNormal() {
    final int xOffset = -(this.x - this.parent.x);
    final int yOffset = -this.parent.h + CELL_HEIGHT * 2;
    final int wCell = this.parent.w / this.headers.length;
    final int hCell = CELL_HEIGHT;

    for (int i = 0; i < this.cells.length; i++) {
      Cell[] row = this.cells[i];
      for (int j = 0; j < row.length; j++) {
        Cell cell = row[j];
        if (cell != null) {
          cell.update(xOffset + j * wCell, yOffset + i * hCell, wCell, hCell);
        }
      }
    }
  }

  void show() {
    if (this.cells != null) {
      for (int i = 0; i < min(this.cells.length, this.parent.h / CELL_HEIGHT - 2); i++) {
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
    Viewer viewer = (Viewer) this.parent;
    IDocument document = DocumentFactory.createInstance(viewer.filename, "CP949");
    ISheet sheet = document.getSheetAt(this.col);

    this.unload();

    ITable bestTable = sheet.findTable(30, 30);
    if(bestTable == null) {
      document.close();
      return;
    }

    java.util.List<ITable> tables = sheet.findTables(30, 30);
    for (ITable table : tables) {
      if (table.getNumberOfHeaders() < bestTable.getNumberOfHeaders()) { //<>//
        continue;
      }

      int numberOfCols = 0;
      int numberOfRows = 0;
      if (!com.github.romualdrousseau.any2json.Table.IsEmpty(table)) {
        numberOfCols = table.getNumberOfHeaders();
        numberOfRows = min(50, table.getNumberOfRows() + 1);
      }
      if (numberOfCols == 0 || numberOfRows == 0) {
        continue;
      }
      
      this.cells = new Cell[numberOfRows][numberOfCols];
      this.headers = this.cells[0];
 //<>//
      for (int j = 0; j < numberOfCols; j++) {
        TableHeader header = table.getHeaderAt(j);
        this.headers[j] = new Header(this, header.getName(), j);
      }

      int k = 1; //<>//
      for (int i = 0; i < numberOfRows - 1; i++) {
        Row row = (Row) table.getRowAt(i); 
        if (row == null || row.isEmpty(0.5)) {
          continue;
        }

        for (int j = 0; j < numberOfCols; j++) {
          String value = row.getCellValue(table.getHeaderAt(j));
          if (value != null) {
            this.cells[k][j] = new Cell(this, value, k, j);
          }
        }

        k++;
      }

      this.updateTags(true); //<>//

      if(this.checkValidity()) { //<>//
        break;
      }
    }

    document.close();
  }

  void unload() {
    this.cells = null;
    this.headers = null;
    this.currentHeader = null;
    this.currentCell = null;
  }
  
  boolean checkValidity() {
    if(this.headers == null) {
      return false;
    }
    
    int mask = 0;
    for (int j = 0; j < this.headers.length; j++) {
      Header header = (Header) this.headers[j];
      if (header == null) {
        continue;
      }
      
      if (header.newTag.equals(Tag.QUANTITY)) {
        mask += 1;
      }
      if (header.newTag.equals(Tag.PRODUCT_NAME)) {
        mask += 2;
      }
    }
    return (mask == 3);
  }
}
