import com.github.romualdrousseau.shuju.*;
import com.github.romualdrousseau.any2json.*;

class Viewer extends Container {
  String currentFilename;
  Sheet[] sheets;
  Sheet currentSheet;

  Viewer(String filename) {
    super();

    this.currentFilename = filename;

    this.loadAllSheets();

    this.currentSheet = this.sheets[0];
    this.currentSheet.load();
  }

  void update(int x, int y, int w, int h) {
    super.update(x, y, w, h);
    
    final int wTab = this.w / this.sheets.length;
    
    for (int k = 0; k < this.sheets.length; k++) {
      Sheet sheet = this.sheets[k];
      sheet.update(k * wTab, this.h - CELL_HEIGHT, wTab, CELL_HEIGHT);
    }

    for (int k = 0; k < this.sheets.length; k++) {
      Sheet sheet = this.sheets[k];
      if (mousePressed && sheet.checkMouse() && this.currentSheet != sheet) {
        this.currentSheet.unload();
        this.currentSheet = sheet;
        this.currentSheet.load();
      }
    }
  }

  void show() {
    for (int k = 0; k < this.sheets.length; k++) {
      Sheet sheet = this.sheets[k];
      sheet.focus = sheet == this.currentSheet;
      sheet.show();
    }
  }

  void loadAllSheets() {
    IDocument document = DocumentFactory.createInstance(this.currentFilename, "CP949");

    this.sheets = new Sheet[document.getNumberOfSheets()];

    for (int k = 0; k < document.getNumberOfSheets(); k++) {
      this.sheets[k] = new Sheet(this, document.getSheetAt(k).getName(), k);
    }

    document.close();
  }
}
