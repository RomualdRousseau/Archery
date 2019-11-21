
ITagClassifier classifier;
List<LayexMatcher> dataLayexes = new ArrayList<LayexMatcher>();
List<LayexMatcher> metaLayexes = new ArrayList<LayexMatcher>();

void loadDocument(String filePath) {
  IDocument document = DocumentFactory.createInstance(filePath, "UTF-8");

  IntelliSheet sheet = (IntelliSheet) document.getSheetAt(0);

  original = new SheetBitmap(sheet, classifier.getSampleCount(), 100);
  rectangles = sheet.findAllRectangles(classifier.getSampleCount(), 100);

  println("Loading tables ... ");
  sheet.getIntelliTable(classifier, metaLayexes, dataLayexes);
  println("ok.");

  document.close();
}
