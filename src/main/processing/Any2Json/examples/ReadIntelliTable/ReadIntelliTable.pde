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

ISearchBitmap searchBitmap, searchBitmap2, searchBitmap3;
List<SearchPoint[]> searchPoints;

void setup() {
  size(800, 800);
  noSmooth();
  frameRate(1);

  // IDocument document = DocumentFactory.createInstance(dataPath("Продажи 06.19.xls"), "UTF-8");
  // IDocument document = DocumentFactory.createInstance(dataPath("Сервье июнь.xls"), "UTF-8");
  IDocument document = DocumentFactory.createInstance(dataPath("ДжиДиПи Сервье Отчёт по продажам для поставщиков по клиентам июнь 19.xlsx"), "UTF-8");

  NGramNNClassifier classifier1 = new NGramNNClassifier(JSON.loadJSONObject(dataPath("brainColumnClassifier.json")));

  searchBitmap = document.getSheetAt(0).getSearchBitmap(classifier1.getSampleCount(), classifier1.getSampleCount());
  searchBitmap2 = searchBitmap.clone();
  searchBitmap3 = searchBitmap.clone();

  final Filter filter = new Filter(new Template(new int[][] { { 0, 0, 0 }, { 1, 0, 1 }, { 0, 0, 0 } }));
  filter.apply(searchBitmap, 1);
  filter.applyNeg(searchBitmap, 2);

  searchPoints = new RectangleExtractor().extractAll(searchBitmap);

  for (int y = 0; y < searchBitmap2.getHeight(); y++) {
    for (int x = 0; x < searchBitmap2.getWidth(); x++) {
      for (SearchPoint[] table : searchPoints) {
        if (searchBitmap2.get(x, y) > 0 && SearchPoint.isInRange(table, x, y)) {
          searchBitmap2.set(x, y, 0);
        }
      }
    }
  }

  for (int y = 0; y < searchBitmap2.getHeight(); y++) {
    for (int x = 0; x < searchBitmap2.getWidth(); x++) {
      if (searchBitmap2.get(x - 1, y) == 0 && searchBitmap2.get(x, y) > 0 && searchBitmap2.get(x + 1, y) == 0) {
        searchPoints.add(new SearchPoint[] { new SearchPoint(x, y, 0), new SearchPoint(x + 1, y, 0) });
      }
    }
  }

  ITable table = document.getSheetAt(0).findTableWithIntelliTag(classifier1);

  for (IHeader header : table.headers()) {
    println(header.getCleanName(), header.getTag().getValue());
  }

  for (IRow row : table.rows()) {
    for (IHeader header : table.headers()) {
      String value = row.getCellValue(header);
      print(value, "");
    }
    println();
  }

  document.close();
}

void draw() {
  final float dx = width / searchBitmap3.getWidth();
  final float dy = height / searchBitmap3.getHeight();

  background(51);

  stroke(128);
  strokeWeight(1);
  for (int y = 0; y < searchBitmap3.getHeight(); y++) {
    for (int x = 0; x < searchBitmap3.getWidth(); x++) {
      fill(color(255 * searchBitmap3.get(x, y)));
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
