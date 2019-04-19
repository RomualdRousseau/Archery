class TrainingSet_ {
  HashMap<String, Integer> ngrams = new HashMap<String, Integer>();
  int ngramsCount = 0;

  ArrayList<float[]> data = new ArrayList<float[]>();
  ArrayList<float[]> targets = new ArrayList<float[]>();

  int size() {
    return data.size();
  }

  boolean conflict(Cell cell) {
    float[] entity2vec = TrainingSet.entity2vec(cell, 0.8);
    float[] word2vec = TrainingSet.word2vec(cell.cleanValue);
    float[] neighbor2vec = TrainingSet.neighbor2vec(cell);
    float[] data = concat(concat(entity2vec, word2vec), neighbor2vec);

    float[] target = oneHot(cell.newTag.ordinal(), TAGVEC_LENGTH);

    return this.conflict(data, target);
  }

  boolean conflict(float[] data, float[] target) {
    int i  = 0;
    while (i < this.data.size() && !java.util.Arrays.equals(this.data.get(i), data)) i++;
    if (i == this.data.size()) {
      return false;
    } else {
      return !java.util.Arrays.equals(this.targets.get(i), target);
    }
  }

  void add(Cell cell) {
    float[] entity2vec = TrainingSet.entity2vec(cell, 0.8);
    float[] word2vec = TrainingSet.word2vec(cell.cleanValue);
    float[] neighbor2vec = TrainingSet.neighbor2vec(cell);
    float[] data = concat(concat(entity2vec, word2vec), neighbor2vec);

    float[] target = oneHot(cell.newTag.ordinal(), TAGVEC_LENGTH);

    this.add(data, target);
  }

  void add(float[] data, float[] target) {
    int i  = 0;
    while (i < this.data.size() && !java.util.Arrays.equals(this.data.get(i), data)) i++;
    if (i == this.data.size()) {
      this.data.add(data);
      this.targets.add(target);
    } else {
      this.targets.set(i, target);
    }
  }
  
  void registerWord(String w, int n) {
    for (int i = 0; i < w.length() - n + 1; i++) {
      String s = w.substring(i, i + n).toLowerCase();
      Integer index = this.ngrams.get(s);
      if (index == null) {
        index = this.ngramsCount;
        this.ngrams.put(s, index);
        this.ngramsCount++;
        if (this.ngramsCount >= WORDVEC_LENGTH) {
          println("ooooo");
        }
      }
    }
  }
  
  float[] entity2vec(Cell cell, float p) {
    Sheet sheet = (Sheet) cell.parent;
    float[] result;
    
    if(cell.row == 0) {
      result = new float[ENTITYVEC_LENGTH];
      
      int n = 0;
      for (int i = 1; i < sheet.cells.length; i++) {
        Cell other = sheet.cells[i][cell.col];
        if (other == null) {
          continue;
        }
  
        float[] tmp = NlpHelper.entity2vec(other.types, ENTITYVEC_LENGTH);
        for (int j = 0; j < result.length; j++) {  
          result[j] += tmp[j];
        }
        
        n++;
      }
  
      for (int j = 0; j < result.length; j++) {
        result[j] = (result[j] >= p * float(n)) ? 1 : 0;
      }
    } else {
      result = NlpHelper.entity2vec(cell.types, ENTITYVEC_LENGTH);
    }

    return result;
  }
  
  float[] word2vec(String w) {
    float[] result = new float[WORDVEC_LENGTH];

    for (int i = 0; i < w.length() - NGRAMS + 1; i++) {
      String p = w.substring(i, i + NGRAMS).toLowerCase();
      Integer index = this.ngrams.get(p);
      if (index != null && index < WORDVEC_LENGTH) {
        result[index] = 1;
      }
    }

    return result;
  }
  
  float[] neighbor2vec(Cell cell) {
    //Sheet sheet = (Sheet) cell.parent;
    float[] result = new float[WORDVEC_LENGTH];
    /*
    for (int i = 0; i < headers.length; i++) {
     Cell header = sheet.cells[0][i];
     if (header != this) {
     float[] tmp = NlpHelper.word2vec(header.cleanValue, NGRAMS, WORDVEC_LENGTH);
     for (int j = 0; j < result.length; j++) {
     result[j] = min(1, result[j] + tmp[j]);
     }
     }
     }
     */
    return result;
  }

  JSONObject toJSON() {
    JSONArray jsonNgrams = new JSONArray();
    for (String ngram : this.ngrams.keySet()) {
      int index = this.ngrams.get(ngram);
      jsonNgrams.setString(index, ngram);
    }

    JSONArray jsonData = new JSONArray();
    JSONArray jsonTargets = new JSONArray();
    for (int i = 0; i < this.data.size(); i++) {
      JSONArray jsonOneData = new JSONArray();
      for (int j = 0; j < this.data.get(i).length; j++) {
        jsonOneData.append(this.data.get(i)[j]);
      }
      jsonData.append(jsonOneData);

      JSONArray jsonTarget = new JSONArray();
      for (int j = 0; j < this.targets.get(i).length; j++) {
        jsonTarget.append(this.targets.get(i)[j]);
      }
      jsonTargets.append(jsonTarget);
    }

    JSONObject json = new JSONObject();
    json.setJSONArray("ngrams", jsonNgrams);
    json.setJSONArray("data", jsonData);
    json.setJSONArray("targets", jsonTargets);
    return json;
  }

  void fromJSON(JSONObject json) {
    JSONArray jsonNgrams = json.getJSONArray("ngrams");
    JSONArray jsonData = json.getJSONArray("data");
    JSONArray jsonTargets = json.getJSONArray("targets");

    this.ngrams.clear();
    for (int i = 0; i < jsonNgrams.size(); i++) {
      String p = jsonNgrams.getString(i);
      this.ngrams.put(p, i);
    }
    
    this.data.clear();
    for (int i = 0; i < jsonData.size(); i++) {
      JSONArray jsonTmp = jsonData.getJSONArray(i);
      float[] data = new float[jsonTmp.size()];
      for (int j = 0; j < jsonTmp.size(); j++) {
        data[j] = jsonTmp.getFloat(j);
      }
      this.data.add(data);
    }

    this.targets.clear();
    for (int i = 0; i < jsonTargets.size(); i++) {
      JSONArray jsonTmp = jsonTargets.getJSONArray(i);
      float[] target = new float[jsonTmp.size()];
      for (int j = 0; j < jsonTmp.size(); j++) {
        target[j] = jsonTmp.getFloat(j);
      }
      this.targets.add(target);
    }
  }
}
TrainingSet_ TrainingSet = new TrainingSet_();
