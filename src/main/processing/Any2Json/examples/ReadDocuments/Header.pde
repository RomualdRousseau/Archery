class Header extends Cell {
  Tag orgTag;
  Tag newTag;

  Header(Sheet parent, String value, int col) {
    super(parent, value, 0, col);
    this.cleanValue = NlpHelper.removeStopWords(value);
    this.types = NlpHelper.findEntityTypes(this.cleanValue);
  }

  void resetTag() {
    this.newTag = this.orgTag;
  }

  void updateTag(boolean reset, boolean checkForConflicts) {
    this.orgTag = Brain.predict(this, checkForConflicts ? this.getConflicts(false) : null);
    if (reset) {
      this.resetTag();
    }
  }

  void prevTag() {
    Tag[] tags = Tag.values();
    int prevIndex = this.newTag.ordinal() - 1;
    if (prevIndex < 0) {
      prevIndex = tags.length - 1;
    }
    this.newTag = tags[prevIndex];
  }

  void nextTag() {
    Tag[] tags = Tag.values();
    int nextIndex = this.newTag.ordinal() + 1;
    if (nextIndex >= tags.length) {
      nextIndex = 0;
    }
    this.newTag = tags[nextIndex];
  }

  boolean checkPossibleConflicts() {
    Sheet sheet = (Sheet) this.parent;

    for (int j = 0; j < sheet.headers.length; j++) {
      Header header = (Header) sheet.headers[j];
      if (header != null && header != this && !header.newTag.equals(Tag.NONE) && header.newTag.equals(this.newTag)) {
        return true;
      }
    }
    
    if(this.newTag.equals(this.orgTag)) {
      return false;
    } else {
      return TrainingSet.checkConflict(TrainingSet.buildInput(this, this.getConflicts(true)), TrainingSet.buildTarget(this));
    }
  }
  
  Header[] getConflicts(boolean includeOnlyRealConflicts) {
    ArrayList<Header> result = new ArrayList<Header>();
    Sheet sheet = (Sheet) this.parent;
    
    if (includeOnlyRealConflicts && this.newTag.equals(this.orgTag)) {
      return null;
    }

    for (int j = 0; j < sheet.headers.length; j++) {
      Header header = (Header) sheet.headers[j];
      if (header != null && header != this && !header.orgTag.equals(Tag.NONE) && header.orgTag.equals(this.orgTag)) { 
        result.add(header);
      }
    }

    if (result.size() == 0) {
      return null;
    } else {
      return result.toArray(new Header[result.size()]);
    }
  }

  void showTag() {
    //if (this.frozen) {
    //  fill(128, 128, 128);
    //  stroke(64);
    //  rect(this.x, this.y, this.w, this.h);
    //  noFill();
    //} else {
    //  noFill();
    //  stroke(64);
    //  rect(this.x, this.y, this.w, this.h);
    //}

    //if (this.changed) {
    //  stroke(128, 255, 128, 192);
    //  rect(this.x + 1, this.y + 1, this.w - 2, this.h - 2);
    //}

    //if (this.error) {
    //  stroke(255, 128, 128, 192);
    //  rect(this.x + 1, this.y + 1, this.w - 2, this.h - 2);
    //}

    //if (this.focus) {
    //  stroke(255, 128, 0, 192);
    //  rect(this.x + 1, this.y + 1, this.w - 2, this.h - 2);
    //}
    fill(255);
    clip(this.x + 4, this.y - CELL_HEIGHT + 1, this.w - 8, this.h - 2);
    text(this.newTag.toString(), this.x + 4, this.y - 6);
    noClip();
  }
}
