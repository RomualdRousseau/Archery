class Cell extends Widget {
  String cleanValue;
  EntityType[] types;

  Cell(Sheet parent, String value, int row, int col) {
    super(parent, value, row, col);
    this.cleanValue = value.replaceAll("\\s+", "");
    this.types = NlpHelper.entities.find(this.cleanValue, new EntityType[ENTITYVEC_LENGTH]);
  }

  void update(int x, int y, int w, int h) {
    Header header = (Header) ((Sheet) this.parent).headers[this.col];
    super.update(x, y, w, h);
    this.focus = this == ((Sheet) this.parent).currentCell;
    this.frozen = header.newTag != null && header.newTag.equals(Tag.NONE); 
    this.found = search != null && search.equals(this.cleanValue);
    this.changed = false;
    this.error = false;
  }
}
