class Container extends Widget {
  Container() {
    super(null, null, 0, 0);
  }
  
  Container(Container parent, String value, int row, int col) {
    super(parent, value, row, col);
  }
}
