/**
 * Document Processing
 *
 * Author: Romuald Rousseau
 * Date: 2019-04-19
 * Processing 3+
 */

static final int CELL_HEIGHT = 21;

NGramNNClassifier Brain;
DataSet TrainingSet;

String[] documentFileNames;
int currentDocumentIndex = 0;

Viewer viewer;
boolean learning = false;
String search = null;
boolean beautify = false;

void setup() {
  JSON.setFactory(new JSONProcessingFactory());
  size(1600, 800);
  background(51);

  Brain = new NGramNNClassifier(
    new NgramList(JSON.loadJSONObject(dataPath("ngrams.json"))), 
    new RegexList(JSON.loadJSONObject(dataPath("entities.json"))), 
    new StopWordList(JSON.loadJSONArray(dataPath("stopwords.json"))), 
    new com.github.romualdrousseau.shuju.nlp.StringList(JSON.loadJSONObject(dataPath("tags.json"))));
  Brain.getModel().fromJSON(JSON.loadJSONArray(dataPath("brain.json")));

  if (new File(dataPath("trainingset.json")).exists()) {
    TrainingSet = new DataSet(JSON.loadJSONObject(dataPath("trainingset.json")));
  } else {
    TrainingSet = new DataSet();
  }

  documentFileNames = listFileNames(dataPath("1612"));

  ProgressBar.start("Loading document ...", true);
  thread("loadDocument");
}

void draw() {
  background(51);
  noFill();

  if (!ProgressBar.isRunning() && viewer != null && viewer.currentSheet != null) {
    viewer.currentSheet.beautify = beautify;
    viewer.update(200, 0, width - 200, height);

    if (learning) {
      Brain.fit(TrainingSet);
      if (Brain.getAccuracy() == 1.0f) {
        learning = false;
        ProgressBar.stop();
      }
      if (viewer.currentSheet != null) {
        viewer.currentSheet.updateTags(false);
      }
    }

    fill(255);
    text(String.format("%03d / %03d [%s]", currentDocumentIndex + 1, documentFileNames.length, beautify ? "Beautified" : ""), 0, 16);
    text(documentFileNames[currentDocumentIndex], 0, 32);

    if (viewer.currentSheet.currentHeader != null) {
      text(viewer.currentSheet.currentHeader.text, 0, 64);
      text(viewer.currentSheet.currentHeader.header.getCleanName(), 0, 80);
      text(viewer.currentSheet.currentHeader.header.getTag().getValue(), 0, 96);
      text(viewer.currentSheet.currentHeader.newTag, 0, 112);
    }

    if (viewer.currentSheet.currentCell != null) {
      text(viewer.currentSheet.currentCell.text, 0, 144);
      text(viewer.currentSheet.currentCell.cell.getCleanValue(), 0, 160);
    }

    if (viewer.currentSheet.currentHeader != null && viewer.currentSheet.currentCell != null) {
      for (int i = 0; i < Brain.getEntityList().size(); i++) {
        text(String.format("%.0f %.0f %s", viewer.currentSheet.currentHeader.header.getEntityVector().get(i), viewer.currentSheet.currentCell.cell.getEntityVector().get(i), Brain.getEntityList().get(i)), 0, 192 + i * 16);
      }
    } else if (viewer.currentSheet.currentHeader != null) {
      for (int i = 0; i < Brain.getEntityList().size(); i++) {
        text(String.format("%.0f 0 %s", viewer.currentSheet.currentHeader.header.getEntityVector().get(i), Brain.getEntityList().get(i)), 0, 192 + i * 16);
      }
    }

    if (search != null) {
      fill(255, 192, 0);
      text("Search: " + search, 0, height - 2 - 16);
    }

    if (learning) {
      fill(255, 0, 0);
      text(String.format("Accu: %.03f Mean: %.03f", Brain.getAccuracy(), Brain.getMean()), 0, height - 2);
    }

    viewer.show();
  }

  ProgressBar.show();
}

void keyPressed(KeyEvent e) {
  if (ProgressBar.isRunning()) {
    return;
  }

  if (key == CODED && keyCode == RIGHT) {
    currentDocumentIndex++;
    if (currentDocumentIndex >= documentFileNames.length) {
      currentDocumentIndex = documentFileNames.length - 1;
    }
    ProgressBar.start("Loading document ...", true);
    thread("loadDocument");
  }

  if (key == CODED && keyCode == LEFT) {
    currentDocumentIndex--;
    if (currentDocumentIndex < 0) {
      currentDocumentIndex = 0;
    }
    ProgressBar.start("Loading document ...", true);
    thread("loadDocument");
  }

  if (e.isControlDown() && key == CODED && keyCode == RIGHT) {
    currentDocumentIndex += 9;
    if (currentDocumentIndex >= documentFileNames.length) {
      currentDocumentIndex = documentFileNames.length - 1;
    }
    ProgressBar.start("Loading document ...", true);
    thread("loadDocument");
  }

  if (e.isControlDown() && key == CODED && keyCode == LEFT) {
    currentDocumentIndex -= 9;
    if (currentDocumentIndex < 0) {
      currentDocumentIndex = 0;
    }
    ProgressBar.start("Loading document ...", true);
    thread("loadDocument");
  }

  if (key == CODED && keyCode == UP && viewer.currentSheet.currentHeader != null) {
    viewer.currentSheet.currentHeader.nextTag();
  }

  if (key == CODED && keyCode == DOWN && viewer.currentSheet.currentHeader != null) {
    viewer.currentSheet.currentHeader.prevTag();
  }

  if (key==CODED && keyCode == java.awt.event.KeyEvent.VK_F1) {
    learning = !learning;
    if (learning) {
      ProgressBar.start("Learning ...", false);
      ProgressBar.show();
      viewer.currentSheet.buildTrainingSet();
    } else {
      ProgressBar.stop();
    }
  }

  if (key==CODED && keyCode == java.awt.event.KeyEvent.VK_F3) {
    String searchedFilename = ClipHelper.pasteString();
    for (int i = 0; i < documentFileNames.length; i++) {
      if (documentFileNames[i].contains(searchedFilename)) {
        currentDocumentIndex = i;
        ProgressBar.start("Loading document ...", true);
        thread("loadDocument");
        return;
      }
    }
  }

  if (key==CODED && keyCode == java.awt.event.KeyEvent.VK_F5) {
    ProgressBar.start("Loading document ...", true);
    thread("loadDocument");
  }

  if (e.isControlDown() && (keyCode == 'e' || keyCode == 'E')) {
    if (documentFileNames.length > 0) {
      launch(dataPath("1612/" + documentFileNames[currentDocumentIndex]));
    }
  }

  if (e.isControlDown() && (keyCode == 't' || keyCode == 'T')) {
    if (viewer != null && viewer.currentSheet != null) {
      if (viewer.currentSheet.currentCell != null) {
        translateWord(viewer.currentSheet.currentCell.text);
      } else if (viewer.currentSheet.currentHeader != null) {
        translateWord(viewer.currentSheet.currentHeader.text);
      } else {
        translateWord(viewer.currentSheet.text);
      }
    }
  }

  if (e.isControlDown() && (keyCode == 'l' || keyCode == 'L')) {
    if (viewer != null && viewer.currentSheet != null && viewer.currentSheet.currentCell != null) {
      locateGeo(viewer.currentSheet.currentCell.text);
    }
  }

  if (e.isControlDown() && (keyCode == 'f' || keyCode == 'F')) {
    if (documentFileNames.length > 0) {
      ClipHelper.copyString(documentFileNames[currentDocumentIndex]);
    }
  }

  if (e.isControlDown() && (keyCode == 'c' || keyCode == 'C')) {
    if (viewer != null && viewer.currentSheet != null) {
      if (viewer.currentSheet.currentCell != null) {
        ClipHelper.copyString(viewer.currentSheet.currentCell.text);
        search = viewer.currentSheet.currentCell.cell.getCleanValue();
      } else if (viewer.currentSheet.currentHeader != null) {
        ClipHelper.copyString(viewer.currentSheet.currentHeader.text);
        search = viewer.currentSheet.currentHeader.header.getCleanName();
      } else {
        ClipHelper.copyString(viewer.currentSheet.text);
        search = null;
      }
    }
  }

  if (e.isControlDown() && (keyCode == 'b' || keyCode == 'B')) {
    beautify = !beautify;
  }

  if (e.isControlDown() && (keyCode == 's' || keyCode == 'S')) {
    ProgressBar.start("Saving configuration ...", true);
    thread("saveConfigToDisk");
  }

  if (e.isControlDown() && (keyCode == 'o' || keyCode == 'O')) {
    ProgressBar.start("Loading configuration ...", true);
    thread("loadConfigfromDisk");
  }

  if (e.isControlDown() && (keyCode == 'x' || keyCode == 'X')) {
    ProgressBar.start("Deleting file ...", true);
    thread("moveFileToTrash");
  }

  if (e.isControlDown() && (keyCode == 'p' || keyCode == 'P')) {
    TrainingSet = TrainingSet.purgeConflicts();
  }
}

void loadDocument() {
  if (documentFileNames.length > 0) {
    viewer = new Viewer(dataPath("1612/" + documentFileNames[currentDocumentIndex]));
  }
  ProgressBar.stop();
}

void saveConfigToDisk() {
  JSON.saveJSONObject(TrainingSet.toJSON(), dataPath("trainingset.json"));
  JSON.saveJSONObject(Brain.getWordList().toJSON(), dataPath("ngrams.json"));
  JSON.saveJSONArray(Brain.getModel().toJSON(), dataPath("brain.json"));
  JSON.saveJSONObject(Brain.toJSON(), dataPath("all.json"));
  ProgressBar.stop();
}

void loadConfigfromDisk() { 
  Brain = new NGramNNClassifier(
    new NgramList(JSON.loadJSONObject(dataPath("ngrams.json"))), 
    new RegexList(JSON.loadJSONObject(dataPath("entities.json"))), 
    new StopWordList(JSON.loadJSONArray(dataPath("stopwords.json"))), 
    new com.github.romualdrousseau.shuju.nlp.StringList(JSON.loadJSONObject(dataPath("tags.json"))));
  Brain.getModel().fromJSON(JSON.loadJSONArray(dataPath("brain.json")));
  TrainingSet = new DataSet(JSON.loadJSONObject(dataPath("trainingset.json")));
  viewer.currentSheet.updateTags(true);
  ProgressBar.stop();
}

void moveFileToTrash() {
  removeFileName(dataPath("1612/" + documentFileNames[currentDocumentIndex]), dataPath("1612.trash/" + documentFileNames[currentDocumentIndex]));
  documentFileNames = concat(subset(documentFileNames, 0, currentDocumentIndex), subset(documentFileNames, currentDocumentIndex + 1, documentFileNames.length - currentDocumentIndex - 1));
  if (currentDocumentIndex >= documentFileNames.length) {
    currentDocumentIndex = 0;
  }

  if (documentFileNames.length > 0) {
    ProgressBar.start("Loading document ...", true);
    viewer = new Viewer(dataPath("1612/" + documentFileNames[currentDocumentIndex]));
  }

  ProgressBar.stop();
}
