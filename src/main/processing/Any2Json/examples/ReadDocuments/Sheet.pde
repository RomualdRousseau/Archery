class Sheet extends Container {
  Cell[][] cells;
  Cell[] headers;
  Header currentHeader;
  Cell currentCell;
  boolean beautify;

  Sheet(Viewer parent, String value, int index) {
    super(parent, value, 0, index);
    this.beautify = false;
  }

  void buildTrainingSet() {
    if (this.cells == null) {
      return;
    }

    for (int j = 0; j < this.headers.length; j++) {
      Header header = (Header) this.headers[j];
      if (header != null) {
        TrainingSet.registerWord(header.cleanValue, NGRAMS);
        float[] input = TrainingSet.buildInput(header, header.getConflicts(true));  
        float[] target = TrainingSet.buildTarget(header);
        TrainingSet.add(input, target);
      }
    }

    viewer.currentSheet.currentHeader = null;
    viewer.currentSheet.currentCell = null;
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
    super.update(x, y, w, h);

    if (this.cells == null) {
      return;
    }

    int countVisibleCells = 0;
    if (this.beautify) {
      for (int i = 0; i < this.headers.length; i++) {
        Header header = (Header) this.headers[i];
        if (header != null && !header.orgTag.equals(Tag.NONE)) {
          countVisibleCells++;
        }
      }
    } else {
      countVisibleCells = this.headers.length;
    }
    if (countVisibleCells == 0) {
      return;
    }

    final int wCell = this.parent.w / countVisibleCells;
    final int hCell = CELL_HEIGHT;

    if (mousePressed) {
      viewer.currentSheet.currentHeader = null;
      viewer.currentSheet.currentCell = null;
    }

    for (int i = 0; i < this.cells.length; i++) {
      Cell[] row = this.cells[i];
      int k = 0;
      for (int j = 0; j < row.length; j++) {
        Cell cell = row[j];
        
        if (this.beautify) {
          Header header = (Header) this.headers[j];
          if (header == null || header.orgTag.equals(Tag.NONE)) {
            if (cell != null) {
              cell.update(0, 0, 0, 0);
            }
            continue;
          }
        }
          
        if (cell != null) {
          cell.update(k * wCell, i * hCell - this.parent.h + CELL_HEIGHT * 2, wCell, hCell);
  
          if (mousePressed && cell.checkMouse() && this.currentCell != cell) {
            this.currentHeader = (Header) this.headers[j];
            this.currentCell = cell;
          }
        }
        
        k++;
      }
    }
  }

  void show() {
    super.show();

    if (this.cells == null) {
      return;
    }

    for (int i = 0; i < min(this.cells.length, this.parent.h / CELL_HEIGHT - 2); i++) {
      Cell[] row = this.cells[i];

      for (int j = 0; j < row.length; j++) {
        Header header = (Header) this.headers[j];
        Cell cell = row[j];
        if (cell == null) {
          continue;
        }

        cell.focus = cell == this.currentCell;
        cell.frozen = header.newTag != null && header.newTag.equals(Tag.NONE); 
        cell.found = search != null && search.equals(cell.cleanValue);

        if (i == 0) {
          cell.changed = header.orgTag != null && !header.orgTag.equals(header.newTag);
          cell.error = header.checkPossibleConflicts();
          header.showTag();
        } else {
          cell.changed = false;
          cell.error = false;
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

    this.unload();

    if (numberOfCols > 0 && numberOfRows > 0) {
      this.cells = new Cell[numberOfRows][numberOfCols];
      this.headers = this.cells[0];

      for (int j = 0; j < numberOfCols; j++) {
        TableHeader header = table.getHeaderAt(j);
        this.headers[j] = new Header(this, header.getName(), j);
      }

      int k = 1;
      for (int i = 1; i < numberOfRows; i++) {
        Row row = (Row) table.getRowAt(i - 1); 
        try {
          if (row.isEmpty(0.5)) {
            continue;
          }

          for (int j = 0; j < numberOfCols; j++) {
            String value = row.getCellValueAt(j);
            if (value != null) {
              this.cells[k][j] = new Cell(this, value, k, j);
            }
          }
          
          k++;
        }
        catch(UnsupportedOperationException x) {
        }
      }
    }

    document.close();

    this.updateTags(true);
  }

  void unload() {
    this.cells = null;
    this.headers = null;
    this.currentHeader = null;
    this.currentCell = null;
  }
}
