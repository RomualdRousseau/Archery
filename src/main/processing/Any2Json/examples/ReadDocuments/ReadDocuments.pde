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

void setup() {
  size(1600, 800);

  Brain.init();

  NlpHelper.loadStopWords();
  NlpHelper.loadEntities();

  documentFileNames = listFileNames(dataPath("1612"));

  viewer = new Viewer(dataPath("1612/" + documentFileNames[currentDocumentIndex]));
}

void draw() {
  viewer.update(200, 0, width - 200, height);

  background(51);
  noFill();

  fill(255);
  text(String.format("%d/%d", currentDocumentIndex + 1, documentFileNames.length), 0, 16);
  text(documentFileNames[currentDocumentIndex], 0, 32);

  if (viewer.currentSheet.currentCell != null) {
    text(viewer.currentSheet.currentHeader.value, 0, 48);
    text(viewer.currentSheet.currentHeader.cleanValue, 0, 64);
    text(viewer.currentSheet.currentHeader.orgTag.toString(), 0, 80);
    text(viewer.currentSheet.currentHeader.newTag.toString(), 0, 96);
    
    float[] entityVec1 = TrainingSet.entity2vec(viewer.currentSheet.currentHeader, 0.8);
    float[] entityVec2 = TrainingSet.entity2vec(viewer.currentSheet.currentCell, 0.8);
    EntityType[] entityTypes = EntityType.values();
    for (int i = 1; i < entityTypes.length; i++) {
      text(String.format("%.0f %.0f %s", entityVec1[i - 1], entityVec2[i - 1], entityTypes[i]), 0, 112 + i * 16);
    }
  }

  if (learning) {
    fill(255, 0, 0);
    text("Learning ...", 0, height - 32);
    text(Brain.accuracy, 0, height - 16);
    text(Brain.mean, 0, height);
    Brain.fit();
    if (Brain.mean <= 0.05) {
      learning = false;
    }
    viewer.currentSheet.updateTags(false);
  }

  viewer.show();
}

void keyPressed(KeyEvent e) {
  if (key == CODED && keyCode == RIGHT) {
    currentDocumentIndex++;
    if (currentDocumentIndex >= documentFileNames.length) {
      currentDocumentIndex = 0;
    }
    viewer = new Viewer(dataPath("1612/" + documentFileNames[currentDocumentIndex]));
  }

  if (key == CODED && keyCode == LEFT) {
    currentDocumentIndex--;
    if (currentDocumentIndex < 0) {
      currentDocumentIndex = documentFileNames.length - 1;
    }
    viewer = new Viewer(dataPath("1612/" + documentFileNames[currentDocumentIndex]));
  }

  if (key == CODED && keyCode == UP && viewer.currentSheet.currentCell != null) {
    viewer.currentSheet.currentHeader.nextTag();
  }

  if (key == CODED && keyCode == DOWN && viewer.currentSheet.currentCell != null) {
    viewer.currentSheet.currentHeader.prevTag();
  }

  if (key==CODED && keyCode == java.awt.event.KeyEvent.VK_F1) {
    viewer.currentSheet.trainTags();
    learning = !learning;
  }

  if (e.isControlDown() && (keyCode == 'c' || keyCode == 'C')) {
    if (viewer.currentSheet.currentCell != null) {
      ClipHelper.copyString(viewer.currentSheet.currentCell.value);
      search = viewer.currentSheet.currentCell.cleanValue;
    } else {
      search = null;
    }
  }

  if (e.isControlDown() && (keyCode == 't' || keyCode == 'T')) {
    translateWord(viewer.currentSheet.currentCell.value);
  }

  if (e.isControlDown() && (keyCode == 's' || keyCode == 'S')) {
    print("Writing ...");
    saveJSONObject(TrainingSet.toJSON(), dataPath("trainingset.json"));
    saveJSONArray(Brain.model.toJSON(), dataPath("brain.json"));
    println(" Writed");
  }

  if (e.isControlDown() && (keyCode == 'o' || keyCode == 'O')) {
    print("Reading ...");
    TrainingSet.fromJSON(loadJSONObject(dataPath("trainingset.json")));
    Brain.model.fromJSON(loadJSONArray(dataPath("brain.json")));
    viewer.currentSheet.updateTags(true);
    println(" Read");
  }

  if (e.isControlDown() && (keyCode == 'x' || keyCode == 'X')) {
    print("Moving ...");
    removeFileName(dataPath("1612/" + documentFileNames[currentDocumentIndex]), dataPath("1612.x/" + documentFileNames[currentDocumentIndex]));
    documentFileNames = subset(documentFileNames, currentDocumentIndex, 1);
    if (currentDocumentIndex >= documentFileNames.length) {
      currentDocumentIndex = 0;
    }
    println(" Moved");
  }
}
