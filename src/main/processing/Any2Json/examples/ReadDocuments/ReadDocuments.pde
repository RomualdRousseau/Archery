import com.github.romualdrousseau.shuju.*;
import com.github.romualdrousseau.any2json.*;

float cellWidth = 100;
float cellHeight = 21;
int numberOfVisibleRows;
String[] documentFileNames;
int currentDocumentIndex = 0;

void setup() {
  size(1600, 800);
  
  documentFileNames = listFileNames(dataPath("1612"));
}

void draw() {
  background(51);
  noFill();
  stroke(64);

  println("============================================================");
  println("FileName: " + documentFileNames[currentDocumentIndex]);
  
  IDocument document = DocumentFactory.createInstance(dataPath("1612/" + documentFileNames[currentDocumentIndex]), "CP949");
  ISheet sheet = document.getSheetAt(0);
  ITable table = sheet.findTable(30, 30);
  assert !com.github.romualdrousseau.any2json.Table.IsEmpty(table);

  cellWidth = width / float(table.getNumberOfHeaders());
  numberOfVisibleRows = height / int(cellHeight);
  
  for (int k = 0; k < document.getNumberOfSheets(); k++) {
    print(document.getSheetAt(k).getName());
    print("    ");
  }
  println();
  
  for (int j = 0; j < table.getNumberOfHeaders(); j++) {
    TableHeader header = table.getHeaderAt(j);
    drawCell(header.getName(), j, 0);
  }

  for (int i = 0; i < min(numberOfVisibleRows - 1, table.getNumberOfRows()); i++) {
    Row row = (Row) table.getRowAt(i); 
    try {
    if (!row.isEmpty(0.5)) {
      for (int j = 0; j < row.getNumberOfCells(); j++) {
        String value = row.getCellValueAt(j);
        drawCell(value, j, i + 1);
      }
    }
    }
    catch(UnsupportedOperationException x) {
    }
  }
  
  document.close();
  
  delay(1000);
  currentDocumentIndex++;
  if(currentDocumentIndex >= documentFileNames.length) {
    currentDocumentIndex = 0;
  }
}

void drawCell(String value, int col, int row) {
  rect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
  if (value != null) {
    clip(col * cellWidth + 4, row * cellHeight, cellWidth - 8, cellHeight);
    text(value, col * cellWidth + 4, (row + 1) * cellHeight - 6);
    noClip();
  }
}

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
