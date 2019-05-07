class Header extends Widget {
  TableHeader header;
  String newTag;

  Header(Sheet parent, TableHeader header, int col) {
    super(parent, header.getName(), 0, col);
    this.header = header;
    this.newTag = header.getTag().getValue();
  }

  void prevTag() {
    int prevIndex = Brain.getTagList().ordinal(this.newTag) - 1;
    if (prevIndex < 0) {
      prevIndex = Brain.getTagList().size() - 1;
    }
    this.newTag = Brain.getTagList().get(prevIndex);
  }

  void nextTag() {
    int nextIndex = Brain.getTagList().ordinal(this.newTag) + 1;
    if (nextIndex >= Brain.getTagList().size()) {
      nextIndex = 0;
    }
    this.newTag = Brain.getTagList().get(nextIndex);
  }

  boolean checkTagConflicts() {
    Sheet sheet = (Sheet) this.parent;

    for (int j = 0; j < sheet.headers.length; j++) {
      Header header = sheet.headers[j];
      if (header != null && header != this && !header.newTag.equals("NONE") && header.newTag.equals(this.newTag)) {
        return true;
      }
    }

    if (this.newTag.equals(this.header.getTag().getValue())) {
      return false;
    } else {
      return TrainingSet.conflicts(this.header.buildRow(this.newTag, false, false));
    }
  }

  void update(int x, int y, int w, int h) {
    super.update(x, y, w, h);
    this.focus = this == ((Sheet) this.parent).currentHeader;
    this.frozen = this.newTag != null && this.newTag.equals("NONE"); 
    this.found = search != null && search.equals(this.header.getCleanName());
    this.changed = this.header.getTag().getValue() != null && !this.header.getTag().getValue().equals(this.newTag);
    this.error = this.checkTagConflicts() || ((Sheet) this.parent).invalid;
  }

  void show() {
    fill(255);
    clip(this.x + 4, this.y - CELL_HEIGHT + 1, this.w - 8, this.h - 2);
    text(this.newTag.toString(), this.x + 4, this.y - 6);
    noClip();    
    super.show();
  }
}
