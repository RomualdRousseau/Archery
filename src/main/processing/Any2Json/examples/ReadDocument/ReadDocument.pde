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

import com.github.romualdrousseau.any2json.*;
import com.github.romualdrousseau.any2json.classifiers.*;
import com.github.romualdrousseau.any2json.document.html.*;
import com.github.romualdrousseau.any2json.document.excel.*;
import com.github.romualdrousseau.any2json.document.text.*;

import java.util.List;

ISearchBitmap searchBitmap;
List<SearchPoint[]> searchPoints;
List<ITable> tables;

void setup() {
  size(800, 800);
  noSmooth();
  frameRate(1);

  IDocument document = DocumentFactory.createInstance(dataPath("Book2.xlsx"), "UTF-8");
  
  searchBitmap = document.getSheetAt(0).getSearchBitmap(25, 50);
  final Filter filter = new Filter(new Template(new int[][] { { 0, 0, 0 }, { 1, 0, 1 }, { 0, 0, 0 } }));
  filter.apply(searchBitmap, 1);
  filter.applyNeg(searchBitmap, 2);
  searchPoints = new RectangleExtractor().extractAll(searchBitmap);
  
  tables = document.getSheetAt(0).findTables(25, 30);
  
  for(ITable table: tables) {
    if(!table.isMetaTable()) {
      for(IRow row: table.rows()) {
        if(row == null) {
          continue;
        }
        ITable subtable = table.getMetaTables().get(table.getMetaTables().size() - 1);
        println(subtable.isMetaTable(), subtable.getMetaAt(0).getCellValueAt(0), subtable.getMetaAt(1).getCellValueAt(0), row.getCellValueAt(0));
      }
    }
  }
  
  document.close();
}

void draw() {
  final float dx = width / searchBitmap.getWidth();
  final float dy = height / searchBitmap.getHeight();

  background(51);

  stroke(128);
  strokeWeight(1);
  for (int y = 0; y < searchBitmap.getHeight(); y++) {
    for (int x = 0; x < searchBitmap.getWidth(); x++) {
      fill(color(255 * searchBitmap.get(x, y)));
      rect(x * dx, y * dy, dx, dy);
    }
  }

  stroke(0, 0, 255);
  strokeWeight(2);
  for (SearchPoint[] point : searchPoints) {
    noFill();
    rect(point[0].getX() * dx, point[0].getY() * dy, (point[1].getX() - point[0].getX() + 1) * dx, (point[1].getY() - point[0].getY() + 1) * dy);
  }
}
