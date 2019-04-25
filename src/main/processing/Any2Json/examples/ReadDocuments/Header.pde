class Header extends Cell {
  Tag orgTag;
  Tag newTag;

  Header(Sheet parent, String value, int col) {
    super(parent, value, 0, col);
    this.cleanValue = NlpHelper.stopwords.removeStopWords(value);
    this.types = NlpHelper.entities.find(this.cleanValue, new EntityType[ENTITYVEC_LENGTH]);
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

  boolean checkConflicts() {
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
      return TrainingSet.checkConflict(NlpHelper.buildRow(this, this.getConflicts(true)));
    }
  }
  
  Header[] getConflicts(boolean ignoreSelfConflict) {
    Sheet sheet = (Sheet) this.parent;
    
    if (ignoreSelfConflict && this.newTag.equals(this.orgTag)) {
      return null;
    }
    
    ArrayList<Header> result = new ArrayList<Header>();

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
  
  void update(int x, int y, int w, int h) {
    super.update(x, y, w, h);
    this.changed = this.orgTag != null && !this.orgTag.equals(this.newTag);
    this.error = this.checkConflicts() || ((Sheet) this.parent).invalid;
  }

  void show() {
    fill(255);
    clip(this.x + 4, this.y - CELL_HEIGHT + 1, this.w - 8, this.h - 2);
    text(this.newTag.toString(), this.x + 4, this.y - 6);
    noClip();
    super.show();
  }
}
