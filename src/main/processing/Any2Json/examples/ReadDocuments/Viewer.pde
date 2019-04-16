import com.github.romualdrousseau.shuju.*;
import com.github.romualdrousseau.any2json.*;

static final int CELL_HEIGHT = 21;

class Viewer {
  int x, y, w, h;
  int estimatedRows;
  String currentFilename;

  Sheet[] sheets;
  Cell[][] cells;

  Sheet currentSheet;
  Cell currentHeader;
  Cell currentCell;

  Viewer(int x, int y, int w, int h, String filename) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.estimatedRows = h / CELL_HEIGHT;
    this.currentFilename = filename;

    this.loadSheets();
    this.currentSheet = this.loadSheet(0);
  }

  void update() {
    int wCell = this.w / this.cells[0].length;
    int hCell = CELL_HEIGHT;

    for (int i = 0; i < this.cells.length; i++) {
      Cell[] row = this.cells[i];

      for (int j = 0; j < row.length; j++) {
        Cell cell = row[j];
        if (cell == null) {
          continue;
        }

        if (cell.checkMouse(this.x, this.y, wCell, hCell)) {
          this.currentHeader = this.cells[0][j];
          this.currentCell = cell;
        }
      }
    }

    for (int k = 0; k < this.sheets.length; k++) {
      Sheet sheet = this.sheets[k];

      if (sheet.checkMouse(this.x, this.y + this.h - hCell, this.w / this.sheets.length, hCell)) {
        this.currentSheet = this.loadSheet(k);
      }
    }
  }

  void show() {
    int wCell = this.w / this.cells[0].length;
    int hCell = CELL_HEIGHT;

    for (int i = 0; i < this.cells.length; i++) {
      Cell[] row = this.cells[i];

      for (int j = 0; j < row.length; j++) {
        Cell cell = row[j];
        if (cell != null && cell != this.currentCell) {
          cell.show(this.x, this.y, wCell, hCell, false);
        }
      }
    }

    if (this.currentCell != null) {
      this.currentCell.show(this.x, this.y, wCell, hCell, true);
    }

    for (int k = 0; k < this.sheets.length; k++) {
      Sheet sheet = this.sheets[k];
      if (sheet != this.currentSheet) {
        sheet.show(this.x, this.y + this.h - hCell, this.w / this.sheets.length, hCell, false);
      }
    }

    this.currentSheet.show(this.x, this.y + this.h - hCell, this.w / this.sheets.length, hCell, true);
  }

  void loadSheets() {
    IDocument document = DocumentFactory.createInstance(this.currentFilename, "CP949");

    this.sheets = new Sheet[document.getNumberOfSheets()];

    for (int k = 0; k < document.getNumberOfSheets(); k++) {
      this.sheets[k] = new Sheet(document.getSheetAt(k).getName(), k);
    }

    document.close();
  }

  Sheet loadSheet(int sheetIndex) {
    IDocument document = DocumentFactory.createInstance(this.currentFilename, "CP949");

    ISheet sheet = document.getSheetAt(sheetIndex);

    int numberOfCols = 0;
    int numberOfRows = 0;

    ITable table = sheet.findTable(30, 30);
    if (!com.github.romualdrousseau.any2json.Table.IsEmpty(table)) {
      numberOfCols = table.getNumberOfHeaders();
      numberOfRows = min(estimatedRows - 1, table.getNumberOfRows());
    }

    if (numberOfCols == 0 || numberOfRows == 0) {
      this.cells = new Cell[1][1];
    } else {
      this.cells = new Cell[numberOfRows][numberOfCols];

      for (int j = 0; j < numberOfCols; j++) {
        TableHeader header = table.getHeaderAt(j);
        this.cells[0][j] = new Cell(header.getName(), 0, j);
        this.cells[0][j].updateNgrams(2);
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
              this.cells[i][j] = new Cell(value, i, j);
            }
          }
        }
        catch(UnsupportedOperationException x) {
        }
      }
    }
    
    for (int j = 0; j < numberOfCols; j++) {
      this.cells[0][j].updateTag();
    }

    document.close();

    return this.sheets[sheetIndex];
  }
}
