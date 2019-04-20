enum EntityType { 
  NONE, 
    DATE, 
    POSTAL_CODE, 
    REFERENCE, 
    PACKAGE, 
    SMALL, 
    NUMBER
}

enum Tag {
  NONE, 
    DATE, 
    AMOUNT, 
    QUANTITY, 
    CUSTOMER_CODE, 
    CUSTOMER_NAME, 
    CUSTOMER_TYPE, 
    PRODUCT_CODE, 
    PRODUCT_NAME, 
    PRODUCT_PACKAGE, 
    POSTAL_CODE, 
    ADMIN_AREA, 
    LOCALITY, 
    ADDRESS
}

class Entity {
  String pattern;
  EntityType type;

  Entity(String pattern, EntityType type) {
    this.pattern = pattern;
    this.type = type;
  }
}

class NlpHelper_ {
  String[] stopwords; 
  Entity[] entities;

  void loadStopWords() {
    processing.data.Table table = loadTable(dataPath("stopwords.txt"), "csv");

    this.stopwords = new String[table.getRowCount()];

    int i = 0;
    for (TableRow row : table.rows()) {
      this.stopwords[i] = row.getString(0).trim();
      i++;
    }
  }

  void loadEntities() {
    processing.data.Table table = loadTable(dataPath("entities.txt"), "csv");

    this.entities = new Entity[table.getRowCount()];

    int i = 0;
    for (TableRow row : table.rows()) {
      this.entities[i] = new Entity(row.getString(0).trim(), EntityType.valueOf(row.getString(1).trim()));
      i++;
    }
  }

  String removeStopWords(String s) {
    for (int i  = 0; i < this.stopwords.length; i++) {
      s = s.replaceAll(this.stopwords[i], "");
    } 
    return s;
  }

  EntityType[] findEntityTypes(String s) {
    EntityType[] result = new EntityType[this.entities.length];

    for (int i  = 0; i < this.entities.length; i++) {
      String[] m = match(s, this.entities[i].pattern);
      if (m != null) {
        result[i] = this.entities[i].type;
      } else {
        result[i] = EntityType.NONE;
      }
    }

    return result;
  }
  
  float[] entity2vec(Cell cell, float p) {
    Sheet sheet = (Sheet) cell.parent;
    float[] result;

    if (cell.row == 0) {
      result = new float[ENTITYVEC_LENGTH];
      int n = 0;

      for (int i = 1; i < sheet.cells.length; i++) {
        Cell other = sheet.cells[i][cell.col];
        if (other != null) {
          float[] tmp = this.entity2vec(other.types, ENTITYVEC_LENGTH);
          addvf(result, tmp);
          n++;
        }
      }

      if (n > 0) {
        filtervf(result, p * float(n), 0, 1);
      }
    } else {
      result = this.entity2vec(cell.types, ENTITYVEC_LENGTH);
    }

    return result;
  }

  public float[] entity2vec(EntityType[] types, int s) {
    float[] result = new float[s];

    for (int j = 0; j < types.length; j++) {
      EntityType type = types[j];
      if(!type.equals(EntityType.NONE)) {
        result[type.ordinal() - 1] = 1;
      }
    }

    return result;
  }
}
NlpHelper_ NlpHelper = new NlpHelper_();
