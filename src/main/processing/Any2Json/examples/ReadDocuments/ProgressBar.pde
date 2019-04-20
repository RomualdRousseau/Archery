class ProgressBar_ {
  boolean active = false;
  boolean exclusive = false;
  String text;
  int percent;
  
  ProgressBar_() {
    this.active = false;
    this.text = "";
    this.percent = 0;
  }
  
  synchronized boolean isRunning() {
    return this.active && this.exclusive;
  }
  
  synchronized void start(String text, boolean exclusive) {
    this.active = true;
    this.exclusive = exclusive;
    this.text = text;
    this.percent = 0;
  }
  
  synchronized void stop() {
    this.active = false;
    this.exclusive = false;
    this.text = "";
    this.percent = 0;
  }
  
  synchronized void show() {
    if(!this.active) {
      return;
    }

    if(!this.exclusive) {
      noStroke();
      fill(0, 192);
      rect(width * 0.25 - 10, height / 2 - 7 - 2 - 16 - 10, width * 0.5 + 4 + 20, 32 + 20);
    }
    
    fill(255);
    text(this.text, width * 0.25, height / 2 - 7 - 6);
    
    stroke(255);
    fill(0);
    rect(width * 0.25 - 2, height / 2 - 7, width * 0.5 + 4, 14);

    stroke(0);
    fill(255);
    float w = map(this.percent, 0, 100, 0, width * 0.5);
    rect(width * 0.25, height / 2 - 5, w, 10);

    this.percent = (this.percent + 1) % 101;
  }
}
ProgressBar_ ProgressBar = new ProgressBar_();
