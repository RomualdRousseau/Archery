import com.github.romualdrousseau.any2json.*;
import com.github.romualdrousseau.any2json.document.xml.*;
import com.github.romualdrousseau.any2json.classifiers.*;
import com.github.romualdrousseau.any2json.document.excel.*;
import com.github.romualdrousseau.any2json.document.html.*;
import com.github.romualdrousseau.any2json.document.text.*;

import com.github.romualdrousseau.shuju.transforms.*;
import com.github.romualdrousseau.shuju.nlp.*;
import com.github.romualdrousseau.shuju.columns.*;
import com.github.romualdrousseau.shuju.util.*;
import com.github.romualdrousseau.shuju.*;
import com.github.romualdrousseau.shuju.cv.*;
import com.github.romualdrousseau.shuju.cv.templatematching.*;
import com.github.romualdrousseau.shuju.math.*;
import com.github.romualdrousseau.shuju.json.*;
import com.github.romualdrousseau.shuju.json.processing.*;
import com.github.romualdrousseau.shuju.json.jackson.*;
import com.github.romualdrousseau.shuju.nlp.impl.*;
import com.github.romualdrousseau.shuju.ml.nn.loss.*;
import com.github.romualdrousseau.shuju.ml.nn.normalizer.*;
import com.github.romualdrousseau.shuju.ml.nn.*;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.*;
import com.github.romualdrousseau.shuju.genetic.*;
import com.github.romualdrousseau.shuju.ml.kmean.*;
import com.github.romualdrousseau.shuju.ml.qlearner.*;
import com.github.romualdrousseau.shuju.ml.slr.*;
import com.github.romualdrousseau.shuju.ml.nn.initializer.*;
import com.github.romualdrousseau.shuju.ml.nn.optimizer.*;
import com.github.romualdrousseau.shuju.ml.nn.optimizer.builder.*;
import com.github.romualdrousseau.shuju.ml.nn.scheduler.*;
import com.github.romualdrousseau.shuju.ml.nn.activation.*;
import com.github.romualdrousseau.shuju.ml.knn.*;

void setup() {
  size(800, 800);
  noSmooth();
  frameRate(1);

  IDocument document = DocumentFactory.createInstance(dataPath("data.xlsx"), "UTF-8");

  ITable table = document.getSheetAt(0).getTable();

  for (IHeader header : table.headers()) {
    println(header.getCleanName());
  }

  int rowNum = 0;
  for (IRow row : table.rows()) {
    for (IHeader header : table.headers()) {
      String value = row.getCellValue(header);
      print(value, "");
    }
    println();
    rowNum++;
  }
  
  println(rowNum, table.getNumberOfRows());
  
  if(rowNum == table.getNumberOfRows()) {
    println("success");
  }

  document.close();
}

void draw() {
}
