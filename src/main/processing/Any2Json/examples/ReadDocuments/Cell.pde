class Cell extends Widget {
  TableCell cell;

  Cell(Sheet parent, TableCell cell, int row, int col) {
    super(parent, cell.getValue(), row, col);
    this.cell = cell;
  }

  void update(int x, int y, int w, int h) {
    Header header = (Header) ((Sheet) this.parent).headers[this.col];
    super.update(x, y, w, h);
    this.focus = this == ((Sheet) this.parent).currentCell;
    this.frozen = header.newTag != null && header.newTag.equals("NONE"); 
    this.found = search != null && search.equals(this.cell.getCleanValue());
    this.changed = false;
    this.error = false;
  }
}
