import com.github.romualdrousseau.shuju.json.*;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.classifiers.*;

import com.github.romualdrousseau.any2json.v2.*;
import com.github.romualdrousseau.any2json.v2.base.*;
import com.github.romualdrousseau.any2json.v2.intelli.*;
import com.github.romualdrousseau.any2json.v2.intelli.event.*;
import com.github.romualdrousseau.any2json.v2.intelli.header.*;
import com.github.romualdrousseau.any2json.v2.layex.*;
import com.github.romualdrousseau.any2json.v2.loader.xlsx.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.List;
import java.awt.event.KeyEvent;

final String[] metaLayexes = { "(v[v|e|s]$)+" };

final String[] dataLayexes = {
  //"(()([v|s]{3}$))([ee[v|s]$][(ve[v|s]$)(ee[v|s]$)+]+)+"
  //"(()(ve{3}v$v{6}$))([vs{4}$][(ev[e|s]{4}$)(v[e|s]{4}$)+]+)+(vs{4}$)?",
  //"(([v|e]$)+([v|s][v|e][v|e|s]+$[v|e|s]+$))(()([v|e|s]{2}[v|e|s]+$))+([v|e|s]{2}$)?",
  "((e[v|e|s]*$)(v[v|e][v|e|s]+$))(()([v|e|s]{2}[v|e|s]+$))+([v|e|s]{2}$)?",
  "((v[v|e|s]*$)(v[v|e][v|e|s]+$))(([v|e|s]{2}$)([v|e|s]{2}[v|e|s]+$)+)+([v|e|s]{2}$)?",
  "(()([v|s][v|e][v|e|s]+$))((s[v|e][v|e|s]+$)([v|e][v|e|s][v|e|s]+$)+)+([v|e|s]{2}$)?",
  "(()([v|s][v|e][v|e|s]+$))(()([v|e|s]{2}[v|e|s]+$))+([v|e|s]{2}$)?"
};

ITagClassifier classifier;
int scrollSpeed;
int gridSize;

volatile boolean documentLoaded = true;
PGraphics documentImage;
int documentTopY;

void configure() {
  classifier = new NGramNNClassifier(JSON.loadJSONObject(dataPath("brainColumnClassifier.json")), metaLayexes, dataLayexes);
  //classifier = new NGramNNClassifier(JSON.loadJSONObject(dataPath("brainColumnClassifier.json")));
  
  scrollSpeed = 100; // 100px per scroll  

  gridSize = 10; // 10px
}

void setup() {
  size(1000, 800);
  noSmooth();
  frameRate(20);

  configure();

  buildEmptyImage();
}

void draw() {
  if (!documentLoaded) {
    return;
  }

  background(51);
  image(documentImage, 0, -documentTopY);
  displayHUD();
}

void fileSelected(File selection) {
  if (selection != null) {
    noLoop();
    documentLoaded = false;
    XlsxDocument doc = new XlsxDocument();
    doc.open(selection, "UTF-8");
    doc.close();
    //loadDocument(selection.getAbsolutePath());
    documentTopY = 0;
    documentLoaded = true;
    loop();
  }
}

void keyPressed() {
  if (keyCode == KeyEvent.VK_F3) {
    selectInput("Select a file to process:", "fileSelected");
  }
  if (keyCode == KeyEvent.VK_HOME) {
    documentTopY = 0;
  }
  if (keyCode == KeyEvent.VK_END) {
    documentTopY = max(0, documentImage.height - height + 17);
  }
  if (keyCode == KeyEvent.VK_PAGE_UP ) {
    documentTopY = (int) constrain(documentTopY - height, -gridSize, max(0, documentImage.height - height + 17) + gridSize);
  }
  if (keyCode == KeyEvent.VK_PAGE_DOWN ) {
    documentTopY = (int) constrain(documentTopY + height, -gridSize, max(0, documentImage.height - height + 17) + gridSize);
  }
}

void mouseWheel(MouseEvent event) {
  documentTopY = (int) constrain(documentTopY + event.getCount() * scrollSpeed, -gridSize, max(0, documentImage.height - height + 17) + gridSize);
}

void loadDocument(String filePath) {
  println("Loading document ... ");

  File tempFile = null;
  try {
    tempFile = File.createTempFile("temp", null);
    tempFile.deleteOnExit();
    Path oldFile = Paths.get(filePath);  
    Path newFile = tempFile.toPath();
    Files.copy(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);
  } 
  catch (IOException e) {
    e.printStackTrace();
  }

  if (tempFile == null) {
    return;
  }

  Document document = DocumentFactory.createInstance(tempFile.getAbsolutePath(), "UTF-8");

  Sheet sheet = document.getSheetAt(0);
  sheet.addSheetListener(new SheetListener() {
    public void stepCompleted(SheetEvent e) {
      buildImage(e);
    }
  }
  );

  com.github.romualdrousseau.any2json.v2.Table table = sheet.getTable(classifier);
  println("Tables loaded.");
  println("done.");
  
  dumpTable(table);

  document.close();
}

void buildEmptyImage() {
  int dx = width / classifier.getSampleCount();
  int dy = gridSize;

  documentImage = createGraphics(width, height);

  documentImage.beginDraw();
  documentImage.stroke(128);
  documentImage.strokeWeight(1);
  for (int y = 0; y < height / dy; y++) {
    for (int x = 0; x < width / dx; x++) {
      documentImage.fill(0);
      documentImage.rect(x * dx, y * dy, dx, dy);
    }
  }
  documentImage.endDraw();
}

void buildImage(SheetEvent e) {
  IntelliSheet sheet = (IntelliSheet) e.getSource();
  int dx = width / classifier.getSampleCount();

  if (e instanceof BitmapGeneratedEvent) {
    SheetBitmap bitmap = ((BitmapGeneratedEvent) e).getBitmap();

    // Max rows set to 5000 to prevent heap overflow
    documentImage = createGraphics(width, Math.min(sheet.getLastRowNum(), 100) * gridSize);

    documentImage.beginDraw();
    documentImage.stroke(128);
    documentImage.strokeWeight(1);
    for (int y = 0; y < bitmap.getHeight(); y++) {
      for (int x = 0; x < bitmap.getWidth(); x++) {
        documentImage.fill(color(255 * bitmap.get(x, y)));
        documentImage.rect(x * dx, y * gridSize, dx, gridSize);
      }
    }
    documentImage.endDraw();

    println("Image generated.");
  }

  if (e instanceof AllTablesExtractedEvent) {
    documentImage.beginDraw();
    documentImage.stroke(0, 0, 255);
    documentImage.strokeWeight(2);
    documentImage.noFill();
    for (AbstractTable table : ((AllTablesExtractedEvent) e).getTables()) {
      documentImage.rect(table.getFirstColumn() * dx, table.getFirstRow() * gridSize, table.getNumberOfColumns() * dx, table.getNumberOfRows() * gridSize);
    }
    documentImage.endDraw();

    println("Tables extracted from image.");
  }

  if (e instanceof DataTableListBuiltEvent) {
    documentImage.beginDraw();
    documentImage.noStroke();

    for (DataTable table : ((DataTableListBuiltEvent) e).getDataTables()) {
      // meta
      documentImage.fill(color(255, 128, 0), 128);
      documentImage.rect(table.getFirstColumn() * dx, table.getFirstRow() * gridSize, table.getNumberOfColumns() * dx, table.getFirstRowOffset() * gridSize);
      // header
      documentImage.fill(color(0, 255, 0), 128);
      documentImage.rect(table.getFirstColumn() * dx, (table.getFirstRow() + table.getHeaderRowOffset()) * gridSize, table.getNumberOfColumns() * dx, gridSize);
      // data
      documentImage.fill(color(0, 255, 0), 64);
      documentImage.rect(table.getFirstColumn() * dx, (table.getFirstRow() + table.getFirstRowOffset()) * gridSize, table.getNumberOfColumns() * dx, table.getNumberOfRows() * gridSize);
      // rowgroups
      documentImage.fill(color(0, 255, 255), 128);
      for(RowGroup rowGroup : table.rowGroups()) {
        documentImage.rect(table.getFirstColumn() * dx, (table.getFirstRow() + table.getFirstRowOffset() + rowGroup.getRow()) * gridSize, table.getNumberOfColumns() * dx, gridSize);
      }
      // footer
      documentImage.fill(color(0, 0, 0), 64);
      documentImage.rect(table.getFirstColumn() * dx, (table.getLastRow() + table.getLastRowOffset() + 1) * gridSize, table.getNumberOfColumns() * dx, -table.getLastRowOffset() * gridSize);
    }
    documentImage.endDraw();

    println("\nDataTable list built.");
  }

  if (e instanceof MetaTableListBuiltEvent) {
    documentImage.beginDraw();
    documentImage.noStroke();
    documentImage.fill(color(255, 128, 0), 128);
    for (MetaTable table : ((MetaTableListBuiltEvent) e).getMetaTables()) {
      documentImage.rect(table.getFirstColumn() * dx, table.getFirstRow() * gridSize, table.getNumberOfColumns() * dx, table.getNumberOfRows() * gridSize);
    }
    documentImage.endDraw();

    println("MetaTable list built.");
  }

  if (e instanceof TableGraphBuiltEvent) {
    println("TableGraph generated.");
    println("============================ DUMP TABLEGRAPH ============================");
    ((TableGraphBuiltEvent) e).dumpTableGraph();
    println("================================== END ==================================");
  }
}

void displayHUD() {
  fill(0);
  stroke(255);
  rect(0, height - 17, width - 1, 16);

  fill(255);
  text("F3: Open a document", 4, height - 4);

  fill(255, 0, 0);
  int x = floor(mouseX * classifier.getSampleCount() / width);
  int y = floor((mouseY + documentTopY) / gridSize);
  String s = String.format("(%d, %d)", x, y);
  text(s, width - textWidth(s) - 4, height - 4);
}

void dumpTable(com.github.romualdrousseau.any2json.v2.Table table) {
  /*
  println();
  for (Header header : table.headers()) {
    AbstractHeader abstractHeader = (AbstractHeader) header;
    print(abstractHeader.getName(), abstractHeader.getTag().getValue(), "| ");
  }
  println();
  */
  /*
  int n = 0;
  for (Row row : table.rows()) {
    for (Cell cell : row.cells()) {
      print(cell.getValue(), "| ");
    }
    println();
    if(n++ >= 50) break;
  }
  */
}
