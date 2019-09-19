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

String[] documentFileNames = null;
int currentDocumentIndex = 0;

Viewer viewer = null;
String search = null;
boolean learning = false;
boolean beautify = false;

void setup() {
  size(1600, 800);
  background(51);

  JSON.setFactory(new JSONProcessingFactory());

  Brain = new NGramNNClassifier(
    new NgramList(JSON.loadJSONObject(dataPath("ngrams.json"))),
    new RegexList(JSON.loadJSONObject(dataPath("entities.json"))),
    new StopWordList(JSON.loadJSONArray(dataPath("stopwords.json"))),
    new com.github.romualdrousseau.shuju.nlp.StringList(JSON.loadJSONObject(dataPath("tags.json"))),
    new String[] {});

  if (new File(dataPath("brain.json")).exists()) {
    Brain.getModel().fromJSON(JSON.loadJSONArray(dataPath("brain.json")));
  }

  if (new File(dataPath("trainingset.json")).exists()) {
    TrainingSet = new DataSet(JSON.loadJSONObject(dataPath("trainingset.json")));
  } else {
    TrainingSet = new DataSet();
  }

  documentFileNames = listFileNames(dataPath("samples"));

  parallel("Loading document ...", "loadDocument");
}

void draw() {
  background(51);
  noFill();

  if (!ProgressBar.isRunning() && viewer != null && viewer.currentSheet != null) {
    viewer.currentSheet.beautify = beautify;
    viewer.update(200, 0, width - 200, height);

    if (learning) {
      learn();
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
    loadNextDocument(1);
  }

  if (key == CODED && keyCode == LEFT) {
    loadPreviousDocument(1);
  }

  if (e.isControlDown() && key == CODED && keyCode == RIGHT) {
    loadNextDocument(9);
  }

  if (e.isControlDown() && key == CODED && keyCode == LEFT) {
    loadPreviousDocument(9);
  }

  if (key == CODED && keyCode == UP) {
    changeNextTag();
  }

  if (key == CODED && keyCode == DOWN) {
    changePreviousTag();
  }

  if (key == CODED && keyCode == java.awt.event.KeyEvent.VK_F1) {
    toggleLearn();
  }

  if (key == CODED && keyCode == java.awt.event.KeyEvent.VK_F3) {
    searchForDocument();
  }

  if (key == CODED && keyCode == java.awt.event.KeyEvent.VK_F5) {
    parallel("Loading document ...", "loadDocument");
  }

  if (e.isControlDown() && (keyCode == 'e' || keyCode == 'E')) {
    openExcelOnSelectedDocument();
  }

  if (e.isControlDown() && (keyCode == 't' || keyCode == 'T')) {
    translateSelectedWord();
  }

  if (e.isControlDown() && (keyCode == 'l' || keyCode == 'L')) {
    locateSelectedWord();
  }

  if (e.isControlDown() && (keyCode == 'f' || keyCode == 'F')) {
    copySelectedDocumentName();
  }

  if (e.isControlDown() && (keyCode == 'c' || keyCode == 'C')) {
    copySelectedWord();
  }

  if (e.isControlDown() && (keyCode == 'b' || keyCode == 'B')) {
    toggleBeautyMode();
  }

  if (e.isControlDown() && (keyCode == 's' || keyCode == 'S')) {
    parallel("Saving configuration ...", "saveConfigToDisk");
  }

  if (e.isControlDown() && (keyCode == 'o' || keyCode == 'O')) {
    parallel("Loading configuration ...", "loadConfigfromDisk");
  }

  if (e.isControlDown() && (keyCode == 'x' || keyCode == 'X')) {
    parallel("Deleting file ...", "moveFileToTrash");
  }

  if (e.isControlDown() && (keyCode == 'p' || keyCode == 'P')) {
    cleanupTrainingSet();
  }
}
