import com.github.romualdrousseau.shuju.columns.*;
import com.github.romualdrousseau.shuju.cv.*;
import com.github.romualdrousseau.shuju.*;
import com.github.romualdrousseau.shuju.json.jackson.*;
import com.github.romualdrousseau.shuju.json.processing.*;
import com.github.romualdrousseau.shuju.ml.nn.activation.*;
import com.github.romualdrousseau.shuju.ml.nn.*;
import com.github.romualdrousseau.shuju.ml.nn.loss.*;
import com.github.romualdrousseau.shuju.ml.nn.optimizer.*;
import com.github.romualdrousseau.shuju.ml.nn.scheduler.*;
import com.github.romualdrousseau.shuju.ml.slr.*;
import com.github.romualdrousseau.shuju.nlp.*;
import com.github.romualdrousseau.shuju.genetic.*;
import com.github.romualdrousseau.shuju.math.*;
import com.github.romualdrousseau.shuju.ml.nn.initializer.*;
import com.github.romualdrousseau.shuju.ml.nn.normalizer.*;
import com.github.romualdrousseau.shuju.ml.qlearner.*;
import com.github.romualdrousseau.shuju.nlp.impl.*;
import com.github.romualdrousseau.shuju.transforms.*;
import com.github.romualdrousseau.shuju.cv.templatematching.*;
import com.github.romualdrousseau.shuju.json.*;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.*;
import com.github.romualdrousseau.shuju.ml.nn.optimizer.builder.*;
import com.github.romualdrousseau.shuju.ml.kmean.*;
import com.github.romualdrousseau.shuju.util.*;
import com.github.romualdrousseau.shuju.ml.knn.*;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.classifiers.*;

import com.github.romualdrousseau.any2json.v2.*;
import com.github.romualdrousseau.any2json.v2.base.*;
import com.github.romualdrousseau.any2json.v2.intelli.*;
import com.github.romualdrousseau.any2json.v2.layex.*;

import java.util.List;

ITagClassifier classifier;

PGraphics documentImage;
volatile boolean documentLoaded = false;
int dx, dy = 10, y = 0, sy = 5;

void setup() {
  size(800, 800);
  noSmooth();
  frameRate(20);
  classifier = new NGramNNClassifier(JSON.loadJSONObject(dataPath("brainColumnClassifier.json")));
  selectInput("Select a file to process:", "fileSelected");
}

void draw() {
  background(51);

  if (!documentLoaded) {
    return;
  }

  image(documentImage, 0, -y);

  fill(255, 0, 0);
  int xs = floor(mouseX / dx);
  int ys = floor((mouseY + y) / dy);
  text("(" + xs + ",  " + ys + ")", width - 100, height - 10);
}

void fileSelected(File selection) {
  if (selection != null) {
    noLoop();
    documentLoaded = false;
    loadDocument(selection.getAbsolutePath());
    documentLoaded = true;
    loop();
  }
}

void keyPressed() {
  if (key == ' ') {
    selectInput("Select a file to process:", "fileSelected");
  }
}

void mouseWheel(MouseEvent event) {
  y = (int) constrain(y + event.getCount() * dy * sy, -dy, max(0, documentImage.height - height) + dy);
}

void loadDocument(String filePath) {
  IDocument document = DocumentFactory.createInstance(filePath, "UTF-8");

  IntelliSheet sheet = (IntelliSheet) document.getSheetAt(0);

  println("Loading tables ... ");
  sheet.getTable(classifier);
  println("ok.");

  buildImage(sheet);

  document.close();
}

void buildImage(IntelliSheet sheet) {
  dx = width / sheet.debugBitmap.getWidth();
  y = 0;
  
  documentImage = createGraphics(classifier.getSampleCount() * dx, Math.min(5000, sheet.getLastRowNum()) * dy);
  
  documentImage.beginDraw();
  documentImage.stroke(128);
  documentImage.strokeWeight(1);
  for (int y = 0; y < sheet.debugBitmap.getHeight(); y++) {
    for (int x = 0; x < sheet.debugBitmap.getWidth(); x++) {
      documentImage.fill(color(255 * sheet.debugBitmap.get(x, y)));
      documentImage.rect(x * dx, y * dy, dx, dy);
    }
  }

  documentImage.stroke(0, 0, 255);
  documentImage.strokeWeight(2);
  for (com.github.romualdrousseau.any2json.v2.base.Table table : sheet.debugTables) {
    documentImage.noFill();
    documentImage.rect(table.getFirstColumn() * dx, table.getFirstRow() * dy, table.getNumberOfColumns() * dx, table.getNumberOfRows() * dy);
  }

  documentImage.endDraw();
}
