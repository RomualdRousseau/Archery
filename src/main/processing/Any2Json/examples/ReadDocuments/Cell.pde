class Cell extends Widget {
  String cleanValue;
  EntityType[] types;
  Tag orgTag;
  Tag newTag;

  Cell(Sheet parent, String value, int row, int col) {
    super(parent, value, row, col);
    this.cleanValue = NlpHelper.removeStopWords(value);
    this.types = NlpHelper.findEntityTypes(this.cleanValue);
  }

  public void updateTag(boolean reset) {
    this.orgTag = Brain.predict(this);
    if (reset) {
      this.newTag = this.orgTag;
    }
  }

  public void prevTag() {
    Tag[] tags = Tag.values();
    int prevIndex = this.newTag.ordinal() - 1;
    if (prevIndex < 0) {
      prevIndex = tags.length - 1;
    }
    this.newTag = tags[prevIndex];
  }

  public void nextTag() {
    Tag[] tags = Tag.values();
    int nextIndex = this.newTag.ordinal() + 1;
    if (nextIndex >= tags.length) {
      nextIndex = 0;
    }
    this.newTag = tags[nextIndex];
  }

  boolean checkDuplicateTags() {
    Sheet sheet = (Sheet) this.parent;
    Cell[] headers = sheet.cells[0];

    for (int j = 0; j < headers.length; j++) {
      Cell header = headers[j];
      if (header != this && !header.newTag.equals(Tag.NONE) && header.newTag.equals(this.newTag)) {
        return true;
      }
    }

    return false;
  }

  void showTag() {
    /*
    if (this.frozen) {
     fill(128, 128, 128);
     stroke(64);
     rect(this.x, this.y, this.w, this.h);
     noFill();
     } else {
     noFill();
     stroke(64);
     rect(this.x, this.y, this.w, this.h);
     }
     
     if (this.changed) {
     stroke(128, 255, 128, 192);
     rect(this.x + 1, this.y + 1, this.w - 2, this.h - 2);
     }
     
     if (this.error) {
     stroke(255, 128, 128, 192);
     rect(this.x + 1, this.y + 1, this.w - 2, this.h - 2);
     }
     
     if (this.focus) {
     stroke(255, 128, 0, 192);
     rect(this.x + 1, this.y + 1, this.w - 2, this.h - 2);
     }
     */
    fill(255);
    clip(this.x + 4, this.y - CELL_HEIGHT + 1, this.w - 8, this.h - 2);
    text(this.newTag.toString(), this.x + 4, this.y - 6);
    noClip();
  }
}
