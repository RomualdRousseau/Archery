class Widget {
  int x, y, w, h;
  Container parent;
  int row;
  int col;
  String value;
  boolean noEvent;
  boolean focus;
  boolean error;
  boolean changed;
  boolean frozen;
  boolean found;

  Widget(Container parent, String value, int row, int col) {
    this(parent, value, row, col, false);
  }

  Widget(Container parent, String value, int row, int col, boolean noEvent) {
    this.parent = parent;
    this.row = row;
    this.col = col;
    this.value = value;
    this.noEvent = noEvent;
    this.focus = false;
    this.error = false;
    this.changed = false;
  }

  boolean checkMouse() {
    if (noEvent) {
      return false;
    }

    final int x1 = this.x;
    final int y1 = this.y;
    final int x2 = x1 + this.w;
    final int y2 = y1 + this.h;

    return x1 <= mouseX && mouseX < x2 && y1 <= mouseY && mouseY < y2;
  }

  void update(int x, int y, int w, int h) {
    if (this.parent != null) {
      this.x = this.parent.x + this.col * w + x;
      this.y = this.parent.y + this.row * h + y;
    } else {
      this.x = x;
      this.y = y;
    }
    this.w = w;
    this.h = h;
  }

  void show() {
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

    if (this.value != null) {
      if (this.found) {
        fill(255, 128, 0);
      } else {
        fill(255);
      }
      clip(this.x + 4, this.y, this.w - 8, this.h - 2);
      text(this.value, this.x + 4, this.y + this.h - 6);
      noClip();
    }
  }
}
