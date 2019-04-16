String[] documentFileNames;
int currentDocumentIndex = 0;

Viewer viewer;

Extractor regexExtractor = new Extractor();

void setup() {
  size(1600, 800);

  regexExtractor.loadStopWords();
  regexExtractor.loadEntities();

  documentFileNames = listFileNames(dataPath("1612"));

  viewer = new Viewer(200, 0, width - 200, height, dataPath("1612/" + documentFileNames[currentDocumentIndex]));
}

void draw() {
  background(51);
  noFill();

  text(documentFileNames[currentDocumentIndex], 0, 16);

  if (viewer.currentCell != null) {
    text(viewer.currentHeader.value, 0, 32);
    text(viewer.currentHeader.rawTag.toString(), 0, 48);
    text(viewer.currentHeader.tag.toString(), 0, 64);
    
    int line = 6;
    float[] entity2vec = viewer.currentHeader.entity2vec(viewer.cells, 0.8);
    for (int i = 0; i < entity2vec.length; i++) {
      text(i + ": " + entity2vec[i], 0, line * 16);
      line++;
    }
    
    line++;
    float[] word2vec = viewer.currentHeader.word2vec(2);
    for (int i = 0; i < word2vec.length; i++) {
      text(i + ": " + word2vec[i], 0, line * 16);
      line++;
    }
    
    line++;
    float[] neighbor2vec = viewer.currentHeader.neighbor2vec(viewer.cells[0], 2);
    for (int i = 0; i < neighbor2vec.length; i++) {
      text(i + ": " + neighbor2vec[i], 0, line * 16);
      line++;
    }
  }

  viewer.show();
}

void keyPressed() {
  if (key == CODED && keyCode == RIGHT) {
    currentDocumentIndex++;
    if (currentDocumentIndex >= documentFileNames.length) {
      currentDocumentIndex = 0;
    }
    viewer = new Viewer(200, 0, width - 200, height, dataPath("1612/" + documentFileNames[currentDocumentIndex]));
  }
  if (key == CODED && keyCode == LEFT) {
    currentDocumentIndex--;
    if (currentDocumentIndex < 0) {
      currentDocumentIndex = documentFileNames.length - 1;
    }
    viewer = new Viewer(200, 0, width - 200, height, dataPath("1612/" + documentFileNames[currentDocumentIndex]));
  }
  if(key == CODED && keyCode == UP && viewer.currentCell != null) {
     viewer.currentHeader.nextTag();
  }
  if(key == CODED && keyCode == DOWN && viewer.currentCell != null) {
     viewer.currentHeader.prevTag();
  }
  if(key == 't' || key == 'T') {
     // add to training and train
  }
}

void mousePressed() {
  viewer.update();
}
