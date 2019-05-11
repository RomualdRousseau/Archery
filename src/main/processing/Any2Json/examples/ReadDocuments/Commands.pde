void loadNextDocument(int step) {
  currentDocumentIndex += step;
  if (currentDocumentIndex >= documentFileNames.length) {
    currentDocumentIndex = documentFileNames.length - 1;
  }
  parallel("Loading document ...", "loadDocument");
}

void loadPreviousDocument(int step) {
  currentDocumentIndex -= step;
  if (currentDocumentIndex < 0) {
    currentDocumentIndex = 0;
  }
  parallel("Loading document ...", "loadDocument");
}

void changeNextTag() {
  if (viewer != null && viewer.currentSheet != null && viewer.currentSheet.currentHeader != null) {
    viewer.currentSheet.currentHeader.nextTag();
  }
}

void changePreviousTag() {
  if (viewer != null && viewer.currentSheet != null && viewer.currentSheet.currentHeader != null) {
    viewer.currentSheet.currentHeader.prevTag();
  }
}

void toggleLearn() {
  if (!learning) {
    startLearn();
  } else {
    stopLearn();
  }
}

void startLearn() {
  learning = true;
  ProgressBar.start("Learning ...", false);
  ProgressBar.show();
  viewer.currentSheet.buildTrainingSet();
}

void stopLearn() {
  learning = false;
  ProgressBar.stop();
}

void learn() {
  Brain.fit(TrainingSet);
  if (Brain.getAccuracy() == 1.0f) {
    if (!viewer.currentSheet.isAllValid()) {
      viewer.currentSheet.buildTrainingSet();
    } else {
      stopLearn();
    }
  }
  viewer.currentSheet.updateTags(false);
}

void toggleBeautyMode() {
  beautify = !beautify;
}

void searchForDocument() {
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

void openExcelOnSelectedDocument() {
  if (documentFileNames.length > 0) {
    launch(dataPath("samples/" + documentFileNames[currentDocumentIndex]));
  }
}

void translateSelectedWord() {
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

void locateSelectedWord() {
  if (viewer != null && viewer.currentSheet != null) {
    if (viewer.currentSheet.currentCell != null) {
      locateGeo(viewer.currentSheet.currentCell.text);
    } else if (viewer.currentSheet.currentHeader != null) {
      locateGeo(viewer.currentSheet.currentHeader.text);
    } else {
      locateGeo(viewer.currentSheet.text);
    }
  }
}

void copySelectedDocumentName() {
  if (documentFileNames.length > 0) {
    ClipHelper.copyString(documentFileNames[currentDocumentIndex]);
  }
}

void copySelectedWord() {
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

void loadDocument() {
  if (documentFileNames.length > 0) {
    viewer = new Viewer(dataPath("samples/" + documentFileNames[currentDocumentIndex]));
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

  if (new File(dataPath("brain.json")).exists()) {
    Brain.getModel().fromJSON(JSON.loadJSONArray(dataPath("brain.json")));
  }

  if (new File(dataPath("trainingset.json")).exists()) {
    TrainingSet = new DataSet(JSON.loadJSONObject(dataPath("trainingset.json")));
  }

  viewer.currentSheet.updateTags(true);
  ProgressBar.stop();
}

void moveFileToTrash() {
  removeFileName(dataPath("samples/" + documentFileNames[currentDocumentIndex]), dataPath("samples.trash/" + documentFileNames[currentDocumentIndex]));
  documentFileNames = concat(subset(documentFileNames, 0, currentDocumentIndex), subset(documentFileNames, currentDocumentIndex + 1, documentFileNames.length - currentDocumentIndex - 1));
  if (currentDocumentIndex >= documentFileNames.length) {
    currentDocumentIndex = 0;
  }

  if (documentFileNames.length > 0) {
    ProgressBar.start("Loading document ...", true);
    viewer = new Viewer(dataPath("samples/" + documentFileNames[currentDocumentIndex]));
  }

  ProgressBar.stop();
}

void cleanupTrainingSet() {
  TrainingSet = TrainingSet.purgeConflicts();
}
