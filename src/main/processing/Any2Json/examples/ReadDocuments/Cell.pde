class Cell {
  int row;
  int col;
  String rawValue;
  String value;
  EntityType[] types;
  Tag rawTag;
  Tag tag;
  
  Cell(String value, int row, int col) {
    this.row = row;
    this.col = col;
    this.rawValue = value;
    this.value = regexExtractor.removeStopWords(value);
    this.types = regexExtractor.findEntityTypes(this.value);
  }

  public void updateNgrams(int n) {
    for (int i = 0; i < this.value.length() - n + 1; i++) {
      String p = this.value.substring(i, i + n);
      Integer index = ngrams.get(p);
      if (index == null) {
        index = ngramsCount;
        ngrams.put(p, index);
        ngramsCount++;
      }
    }
  }

  public void updateTag() {
    switch(floor(random(3))) {
    case 0:
      this.rawTag = Tag.NONE;
      break;
    case 1:
      this.rawTag = Tag.AMOUNT;
      break;
    case 2:
      this.rawTag = Tag.QUANTITY;
      break;
    }
    this.tag = this.rawTag;
  }
  
  public void prevTag() {
    switch(this.tag) {
    case NONE:
      this.tag = Tag.QUANTITY;
      break;
    case AMOUNT:
      this.tag = Tag.NONE;
      break;
    case QUANTITY:
      this.tag = Tag.AMOUNT;
      break;
    }
  }
  
  public void nextTag() {
    switch(this.tag) {
    case NONE:
      this.tag = Tag.AMOUNT;
      break;
    case AMOUNT:
      this.tag = Tag.QUANTITY;
      break;
    case QUANTITY:
      this.tag = Tag.NONE;
      break;
    }
  }

  public float[] entity2vec(Cell[][] cells, float p) {
    float[] result = new float[this.types.length];

    for (int i = 1; i < cells.length; i++) {
      Cell cell = cells[i][this.col];
      if (cell == null) {
        continue;
      }

      for (int j = 0; j < this.types.length; j++) {  
        result[j] += (cell.types[j] == EntityType.NONE) ? 0 : 1;
      }
    }

    for (int j = 0; j < this.types.length; j++) {
      result[j] = (result[j] >= p * float(cells.length - 1)) ? 1 : 0;
    }

    return result;
  }

  public float[] word2vec(int n) {
    float[] result = new float[ngramsCount];

    for (int i = 0; i < this.value.length() - n + 1; i++) {
      String p = this.value.substring(i, i + n);
      Integer index = ngrams.get(p);
      if (index != null) {
        result[index] = 1;
      }
    }

    return result;
  }

  public float[] neighbor2vec(Cell[] headers, int n) {
    float[] result = new float[ngramsCount];

    for (int i = 0; i < headers.length; i++) {
      Cell header = headers[i];
      if (header != this) {
        float[] tmp = header.word2vec(n);
        for (int j = 0; j < result.length; j++) {
          result[j] = min(1, result[j] + tmp[j]);
        }
      }
    }

    return result;
  }

  boolean checkMouse(int x, int y, int w, int h) {
    int x1 = x + this.col * w;
    int y1 = y + this.row * h;
    int x2 = x1 + w;
    int y2 = y1 + h;
    return x1 <= mouseX && mouseX < x2 && y1 <= mouseY && mouseY < y2;
  }

  void show(int x, int y, int w, int h, boolean focus) {
    stroke(64);
    rect(x + this.col * w, y + this.row * h, w, h);

    if (focus) {
      stroke(255, 128, 0);
      rect(x + this.col * w + 1, y + this.row * h + 1, w - 2, h - 2);
    }

    if (this.rawValue != null) {
      clip(x + this.col * w + 4, y + this.row * h, w - 8, h - 2);
      text(this.rawValue, x + this.col * w + 4, y + (this.row + 1) * h - 6);
      noClip();
    }
  }
}
