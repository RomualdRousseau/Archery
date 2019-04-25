/**
 * Document Processing
 *
 * Author: Romuald Rousseau
 * Date: 2019-04-19
 * Processing 3+
 */

static final int BRAIN_CLOCK = 10;
static final int NGRAMS = 2;
static final int ENTITYVEC_LENGTH = 24;
static final int WORDVEC_LENGTH = 500;
static final int TAGVEC_LENGTH = 16;
static final int CELL_HEIGHT = 21;

String[] documentFileNames;
int currentDocumentIndex = 0;

Viewer viewer;
boolean learning = false;
String search = null;
boolean beautify = false;

void setup() {
  size(1600, 800);
  background(51);
  
  NlpHelper.ngrams.fromJSON(JSONProcessingFactory.loadJSONArray(dataPath("ngrams.json")));
  NlpHelper.stopwords.fromJSON(JSONProcessingFactory.loadJSONArray(dataPath("stopwords.json")));
  NlpHelper.entities.fromJSON(JSONProcessingFactory.loadJSONArray(dataPath("entities.json")), EntityType.class);

  Brain.init();

  if (new File(dataPath("brain.json")).exists()) {
    Brain.model.fromJSON(JSONProcessingFactory.loadJSONArray(dataPath("brain.json")));
  }
  if (new File(dataPath("trainingset.json")).exists()) {
    TrainingSet.fromJSON(JSONProcessingFactory.loadJSONObject(dataPath("trainingset.json")));
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
      Brain.fit();
      if (Brain.mean <= 5e-3) {
        learning = false;
        ProgressBar.stop();
      }
      viewer.currentSheet.updateTags(false);
    }

    fill(255);
    text(String.format("%03d / %03d [%s]", currentDocumentIndex + 1, documentFileNames.length, beautify ? "Beautified" : ""), 0, 16);
    text(documentFileNames[currentDocumentIndex], 0, 32);

    if (viewer.currentSheet.currentCell != null) {
      text(viewer.currentSheet.currentHeader.value, 0, 64);
      text(viewer.currentSheet.currentHeader.cleanValue, 0, 80);
      text(viewer.currentSheet.currentHeader.orgTag.toString(), 0, 96);
      text(viewer.currentSheet.currentHeader.newTag.toString(), 0, 112);

      text(viewer.currentSheet.currentCell.value, 0, 144);
      text(viewer.currentSheet.currentCell.cleanValue, 0, 160);

      float[] entityVec1 = NlpHelper.entity2vec(viewer.currentSheet.currentHeader, 0.8);
      float[] entityVec2 = NlpHelper.entity2vec(viewer.currentSheet.currentCell, 0.8);
      EntityType[] entityTypes = EntityType.values();
      for (int i = 1; i < entityTypes.length; i++) {
        text(String.format("%.0f %.0f %s", entityVec1[i - 1], entityVec2[i - 1], entityTypes[i]), 0, 176 + i * 16);
      }
    }

    if (search != null) {
      fill(255, 192, 0);
      text("Search: " + search, 0, height - 2 - 16);
    }

    if (learning) {
      fill(255, 0, 0);
      text(String.format("Accu: %.03f Mean: %.03f", Brain.accuracy, Brain.mean), 0, height - 2);
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

  if (key == CODED && keyCode == UP && viewer.currentSheet.currentCell != null) {
    viewer.currentSheet.currentHeader.nextTag();
  }

  if (key == CODED && keyCode == DOWN && viewer.currentSheet.currentCell != null) {
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
    for(int i = 0; i < documentFileNames.length; i++) {
      if(documentFileNames[i].contains(searchedFilename)) {
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
    if (viewer != null && viewer.currentSheet != null && viewer.currentSheet.currentCell != null) {
      translateWord(viewer.currentSheet.currentCell.value);
    } else {
      translateWord(viewer.currentSheet.value);
    }
  }
  
  if (e.isControlDown() && (keyCode == 'l' || keyCode == 'L')) {
    if (viewer != null && viewer.currentSheet != null && viewer.currentSheet.currentCell != null) {
      locateGeo(viewer.currentSheet.currentCell.value);
    }
  }

  if (e.isControlDown() && (keyCode == 'f' || keyCode == 'F')) {
    if (documentFileNames.length > 0) {
      ClipHelper.copyString(documentFileNames[currentDocumentIndex]);
    }
  }

  if (e.isControlDown() && (keyCode == 'c' || keyCode == 'C')) {
    if (viewer != null && viewer.currentSheet != null && viewer.currentSheet.currentCell != null) {
      ClipHelper.copyString(viewer.currentSheet.currentCell.value);
      search = viewer.currentSheet.currentCell.cleanValue;
    } else if (viewer != null && viewer.currentSheet != null) {
      ClipHelper.copyString(viewer.currentSheet.value);
      search = null;
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
}

void loadDocument() {
  if (documentFileNames.length > 0) {
    viewer = new Viewer(dataPath("1612/" + documentFileNames[currentDocumentIndex]));
  }
  ProgressBar.stop();
}

void saveConfigToDisk() {
  JSONProcessingFactory.saveJSONArray(dataPath("ngrams.json"), NlpHelper.ngrams.toJSON(JSONProcessingFactory));
  JSONProcessingFactory.saveJSONObject(dataPath("trainingset.json"), TrainingSet.toJSON(JSONProcessingFactory));
  JSONProcessingFactory.saveJSONArray(dataPath("brain.json"), Brain.model.toJSON(JSONProcessingFactory));
  ProgressBar.stop();
}

void loadConfigfromDisk() { 
  NlpHelper.ngrams.fromJSON(JSONProcessingFactory.loadJSONArray(dataPath("ngrams.json")));
  NlpHelper.stopwords.fromJSON(JSONProcessingFactory.loadJSONArray(dataPath("stopwords.json")));
  NlpHelper.entities.fromJSON(JSONProcessingFactory.loadJSONArray(dataPath("entities.json")), EntityType.class);
  TrainingSet.fromJSON(JSONProcessingFactory.loadJSONObject(dataPath("trainingset.json")));
  Brain.model.fromJSON(JSONProcessingFactory.loadJSONArray(dataPath("brain.json")));
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
