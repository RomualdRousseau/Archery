class Cell extends Widget {
  String cleanValue;
  EntityType[] types;
  
  Cell(Sheet parent, String value, int row, int col) {
    super(parent, value, row, col);
    this.cleanValue = value.trim();
    this.types = NlpHelper.findEntityTypes(this.cleanValue);
  }
}
