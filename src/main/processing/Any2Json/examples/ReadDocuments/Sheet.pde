class Sheet extends Container {
  Cell[][] cells;
  Cell currentHeader;
  Cell currentCell;

  Sheet(Viewer parent, String value, int index) {
    super(parent, value, 0, index);
  }

  void trainTags() {
    Cell[] headers = this.cells[0];

    for (int j = 0; j < headers.length; j++) {
      Cell header = headers[j];
      if (header != null) {
        TrainingSet.registerWord(header.cleanValue, NGRAMS);
        TrainingSet.add(header);
      }
    }
  }

  void updateTags(boolean reset) {
    Cell[] headers = this.cells[0];

    for (int j = 0; j < headers.length; j++) {
      Cell header = headers[j];
      if (header != null) {
        header.updateTag(reset);
      }
    }
  }

  void update(int x, int y, int w, int h) {
    super.update(x, y, w, h);

    if (this.cells == null) {
      return;
    }

    final int wCell = this.parent.w / this.cells[0].length;
    final int hCell = CELL_HEIGHT;

    for (int i = 0; i < this.cells.length; i++) {
      Cell[] row = this.cells[i];
      for (int j = 0; j < row.length; j++) {
        Cell cell = row[j];
        if (cell == null) {
          continue;
        }

        cell.update(0, -this.parent.h + CELL_HEIGHT * 2, wCell, hCell);

        if (mousePressed && cell.checkMouse()) {
          this.currentHeader = this.cells[0][j];
          this.currentCell = cell;
        }
      }
    }
  }

  void show() {
    super.show();

    if (this.cells == null) {
      return;
    }

    for (int i = 0; i < min(this.cells.length, this.parent.h / CELL_HEIGHT - 2); i++) {
      Cell[] headers = this.cells[0];
      Cell[] row = this.cells[i];

      for (int j = 0; j < row.length; j++) {
        Cell cell = row[j];
        if (cell == null) {
          continue;
        }

        cell.focus = cell == this.currentCell;
        cell.changed = cell.orgTag != null && !cell.orgTag.equals(cell.newTag);
        cell.frozen = headers[j].newTag != null && headers[j].newTag.equals(Tag.NONE); 
        cell.error = false;
        cell.found = search != null && search.equals(cell.cleanValue);

        if (i == 0) {
          cell.showTag();
          cell.error = cell.checkDuplicateTags() || TrainingSet.conflict(cell);
        }

        cell.show();
      }
    }
  }

  void load() {
    Viewer viewer = (Viewer) this.parent;
    IDocument document = DocumentFactory.createInstance(viewer.currentFilename, "CP949");
    ISheet sheet = document.getSheetAt(this.col);

    int numberOfCols = 0;
    int numberOfRows = 0;

    ITable table = sheet.findTable(30, 30);
    if (!com.github.romualdrousseau.any2json.Table.IsEmpty(table)) {
      numberOfCols = table.getNumberOfHeaders();
      numberOfRows = min(50, table.getNumberOfRows());
    }

    if (numberOfCols == 0 || numberOfRows == 0) {
      this.cells = new Cell[1][1];
    } else {
      this.cells = new Cell[numberOfRows][numberOfCols];

      for (int j = 0; j < numberOfCols; j++) {
        TableHeader header = table.getHeaderAt(j);
        this.cells[0][j] = new Cell(this, header.getName(), 0, j);
      }

      for (int i = 1; i < numberOfRows; i++) {
        Row row = (Row) table.getRowAt(i - 1); 
        try {
          if (row.isEmpty(0.5)) {
            continue;
          }

          for (int j = 0; j < numberOfCols; j++) {
            String value = row.getCellValueAt(j);
            if (value != null) {
              this.cells[i][j] = new Cell(this, value, i, j);
            }
          }
        }
        catch(UnsupportedOperationException x) {
        }
      }
    }

    document.close();

    this.updateTags(true);
  }
}
