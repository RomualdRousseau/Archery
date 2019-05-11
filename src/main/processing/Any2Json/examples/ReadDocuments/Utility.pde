import com.github.romualdrousseau.shuju.columns.*;
import com.github.romualdrousseau.shuju.cv.*;
import com.github.romualdrousseau.shuju.json.*;
import com.github.romualdrousseau.shuju.math.*;
import com.github.romualdrousseau.shuju.ml.ann.*;
import com.github.romualdrousseau.shuju.ml.nn.activation.*;
import com.github.romualdrousseau.shuju.ml.nn.*;
import com.github.romualdrousseau.shuju.ml.nn.loss.*;
import com.github.romualdrousseau.shuju.ml.nn.optimizer.builder.*;
import com.github.romualdrousseau.shuju.ml.qlearner.*;
import com.github.romualdrousseau.shuju.nlp.*;
import com.github.romualdrousseau.shuju.*;
import com.github.romualdrousseau.shuju.json.processing.*;
import com.github.romualdrousseau.shuju.ml.nn.optimizer.*;
import com.github.romualdrousseau.shuju.ml.nn.scheduler.*;
import com.github.romualdrousseau.shuju.ml.slr.*;
import com.github.romualdrousseau.shuju.transforms.*;
import com.github.romualdrousseau.shuju.ml.nn.initializer.*;
import com.github.romualdrousseau.shuju.cv.templatematching.*;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.*;
import com.github.romualdrousseau.shuju.ml.nn.normalizer.*;
import com.github.romualdrousseau.shuju.util.*;
import com.github.romualdrousseau.shuju.ml.knn.*;

import com.github.romualdrousseau.any2json.classifiers.*;
import com.github.romualdrousseau.any2json.document.text.*;
import com.github.romualdrousseau.any2json.*;
import com.github.romualdrousseau.any2json.document.excel.*;
import com.github.romualdrousseau.any2json.document.html.*;
import com.github.romualdrousseau.any2json.util.*;

String[] listFileNames(String dir) {
  File file = new File(dir);
  if (file.isDirectory()) {
    String names[] = file.list();
    return names;
  } else {
    // If it's not a directory
    return null;
  }
}

void removeFileName(String filename1, String filename2) {
  File file1 = new File(filename1);
  File file2 = new File(filename2);
  file1.renameTo(file2);
}

void translateWord(String word) {
  try {
    link("https://translate.google.com/#view=home&op=translate&sl=auto&tl=en&text=" + java.net.URLEncoder.encode(word, "UTF-8"));
  } 
  catch (java.io.UnsupportedEncodingException e) {
    println(e);
  }
}

void locateGeo(String word) {
  try {
    link("https://www.google.com/maps/place/" + java.net.URLEncoder.encode(word, "UTF-8") + ",+South+Korea");
  } 
  catch (java.io.UnsupportedEncodingException e) {
    println(e);
  }
}

void parallel(String text, String command) {
  ProgressBar.start(text, true);
  thread(command);
}
