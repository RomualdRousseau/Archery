import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.shuju.nlp.Ngrams;
import com.github.romualdrousseau.shuju.nlp.EntityTypes;
import com.github.romualdrousseau.shuju.nlp.StopWords;

enum EntityType { 
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

class NlpHelper_ {
  Ngrams ngrams = new Ngrams(NGRAMS, WORDVEC_LENGTH);
  EntityTypes<EntityType> entities = new EntityTypes<EntityType>();
  StopWords stopwords = new StopWords();

  float[] entity2vec(Cell cell, float p) {
    Sheet sheet = (Sheet) cell.parent;
    float[] result;

    if (cell.row == 0) {
      result = new float[ENTITYVEC_LENGTH];
      int n = 0;

      for (int i = 1; i < sheet.cells.length; i++) {
        Cell other = sheet.cells[i][cell.col];
        if (other != null) {
          float[] tmp = Vector.oneHot(other.types, ENTITYVEC_LENGTH);
          Vector.add(result, tmp);
          n++;
        }
      }

      if (n > 0) {
        Vector.filter(result, p * float(n), 0, 1);
      }
    } else {
      result = Vector.oneHot(cell.types, ENTITYVEC_LENGTH);
    }

    return result;
  }

  float[] words2vec(Header[] headers) {
    float[] result = new float[WORDVEC_LENGTH];

    if (headers == null) {
      return result;
    }

    for (int i = 0; i < headers.length; i++) {
      Header header = headers[i];
      if (header != null) {
        float[] tmp = this.ngrams.word2vec(header.cleanValue);
        Vector.add(result, tmp);
      }
    }

    return Vector.constrain(result, 0, 1);
  }
  
  DataRow buildRow(Header header, Header[] conflicts) {
    float[] entity2vec = NlpHelper.entity2vec(header, 0.8);
    float[] word2vec = NlpHelper.ngrams.word2vec(header.cleanValue);
    float[] neighbor2vec = NlpHelper.words2vec(conflicts);
    
    VectorFeature feature = new VectorFeature(concat(concat(entity2vec, word2vec), neighbor2vec));
    VectorFeature label = new VectorFeature(Vector.oneHot(header.newTag.ordinal(), TAGVEC_LENGTH));
    
    DataRow row = new DataRow();
    row.addFeature(feature);
    row.setLabel(label);
    return row;
  }
}
NlpHelper_ NlpHelper = new NlpHelper_();
