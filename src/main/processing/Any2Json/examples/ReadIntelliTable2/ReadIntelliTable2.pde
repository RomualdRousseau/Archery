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

ISearchBitmap original;
List<SearchPoint[]> rectangles;
boolean documentLoaded = false;

void setup() {
  size(800, 800);
  noSmooth();
  frameRate(10);

  classifier = new NGramNNClassifier(JSON.loadJSONObject(dataPath("brainColumnClassifier.json")));

  metaLayexes.add(new Layex("(v{2}$)([v|m|s]{2}[v|m|s]+$)+").compile());

  dataLayexes.add(new Layex("(v{2}v+$)([v|m|s]{2}[v|m|s]+$)+").compile());
  dataLayexes.add(new Layex("(ms*$v+m*$)([v|m|s][v|m|s][v|m|s]+$)+").compile());
  //dataLayexes.add(new Layex("(ms*$v+m*$)([v|m|s]{2}[v|m|s]+$)+([v|m|s]{2}$)?").compile());

  for (LayexMatcher layex : dataLayexes) {
    println(layex.toString());
  }

  selectInput("Select a file to process:", "fileSelected");
}

void fileSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    documentLoaded = false;
    loadDocument(selection.getAbsolutePath());
    documentLoaded = true;
  }
}

void keyPressed() {
  if (key == ' ') {
    selectInput("Select a file to process:", "fileSelected");
  }
}

void draw() {
  if (!documentLoaded) {
    return;
  }

  final float dx = width / original.getWidth();
  final float dy = height / original.getHeight();

  background(51);

  stroke(128);
  strokeWeight(1);
  for (int y = 0; y < original.getHeight(); y++) {
    for (int x = 0; x < original.getWidth(); x++) {
      fill(color(255 * (float) original.get(x, y)));
      rect(x * dx, y * dy, dx, dy);
    }
  }

  stroke(0, 0, 255);
  strokeWeight(2);
  for (SearchPoint[] table : rectangles) {
    noFill();
    rect(table[0].getX() * dx, table[0].getY() * dy, (table[1].getX() - table[0].getX() + 1) * dx, (table[1].getY() - table[0].getY() + 1) * dy);
  }
}
