class TrainingSet_ {
  HashMap<String, Integer> ngrams = new HashMap<String, Integer>();
  int ngramsCount = 0;

  ArrayList<float[]> inputs = new ArrayList<float[]>();
  ArrayList<float[]> targets = new ArrayList<float[]>();

  void healthCheck() {
    if (inputs.size() != targets.size()) {
      throw new UnsupportedOperationException("TRAININGSET UNHEALTHY");
    }
  }

  int size() {
    return inputs.size();
  }

  float[] buildInput(Header header, Header[] conflicts) {
    float[] entity2vec = NlpHelper.entity2vec(header, 0.8);
    float[] word2vec = this.word2vec(header);
    float[] neighbor2vec = this.words2vev(conflicts);
    return concat(concat(entity2vec, word2vec), neighbor2vec);
  }

  float[] buildTarget(Header header) {
    return oneHot(header.newTag.ordinal(), TAGVEC_LENGTH);
  }

  boolean checkConflict(float[] input, float[] target) {
    int i  = 0;
    while (i < this.inputs.size() && !java.util.Arrays.equals(this.inputs.get(i), input)) i++;
    
    return i < this.inputs.size() && !java.util.Arrays.equals(this.targets.get(i), target);
  }

  void add(float[] input, float[] target) {
    this.healthCheck();

    int i  = 0;
    while (i < this.inputs.size() && !java.util.Arrays.equals(this.inputs.get(i), input)) i++;

    if (i == this.inputs.size()) {
      this.inputs.add(input);
      this.targets.add(target);
    } else {
      //if (!java.util.Arrays.equals(this.targets.get(i), target)) {
      //  throw new UnsupportedOperationException("CONFLICTING TARGETS");
      //}
      this.targets.set(i, target);
    }
  }

  void registerWord(String w, int n) {
    for (int i = 0; i < w.length() - n + 1; i++) {
      String s = w.substring(i, i + n).toLowerCase();
      Integer index = this.ngrams.get(s);
      if (index != null) {
        continue;
      }

      index = this.ngramsCount;
      this.ngrams.put(s, index);
      this.ngramsCount++;
      if (this.ngramsCount >= WORDVEC_LENGTH) {
        throw new IndexOutOfBoundsException("WORD2VEC");
      }
    }
  }

  float[] word2vec(Header header) {
    String w = header.cleanValue;
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

  float[] words2vev(Header[] headers) {
    float[] result = new float[WORDVEC_LENGTH];

    if (headers == null) {
      return result;
    }

    for (int i = 0; i < headers.length; i++) {
      Header header = headers[i];
      if (header != null) {
        float[] tmp = TrainingSet.word2vec(header);
        addvf(result, tmp);
      }
    }

    return constrainvf(result, 0, 1);
  }

  JSONObject toJSON() {
    this.healthCheck();

    JSONArray jsonNgrams = new JSONArray();
    for (String ngram : this.ngrams.keySet()) {
      int index = this.ngrams.get(ngram);
      jsonNgrams.setString(index, ngram);
    }

    JSONArray jsonInputs = new JSONArray();
    JSONArray jsonTargets = new JSONArray();
    for (int i = 0; i < this.inputs.size(); i++) {
      JSONArray jsonInput = new JSONArray();
      for (int j = 0; j < this.inputs.get(i).length; j++) {
        jsonInput.append(this.inputs.get(i)[j]);
      }
      jsonInputs.append(jsonInput);

      JSONArray jsonTarget = new JSONArray();
      for (int j = 0; j < this.targets.get(i).length; j++) {
        jsonTarget.append(this.targets.get(i)[j]);
      }
      jsonTargets.append(jsonTarget);
    }

    JSONObject json = new JSONObject();
    json.setJSONArray("ngrams", jsonNgrams);
    json.setJSONArray("inputs", jsonInputs);
    json.setJSONArray("targets", jsonTargets);
    return json;
  }

  void fromJSON(JSONObject json) {
    JSONArray jsonNgrams = json.getJSONArray("ngrams");
    JSONArray jsonInputs = json.getJSONArray("inputs");
    JSONArray jsonTargets = json.getJSONArray("targets");

    this.ngrams.clear();
    for (int i = 0; i < jsonNgrams.size(); i++) {
      String p = jsonNgrams.getString(i);
      this.ngrams.put(p, i);
    }
    this.ngramsCount = jsonNgrams.size();

    this.inputs.clear();
    for (int i = 0; i < jsonInputs.size(); i++) {
      JSONArray jsonInput = jsonInputs.getJSONArray(i);
      float[] input = new float[jsonInput.size()];
      for (int j = 0; j < jsonInput.size(); j++) {
        input[j] = jsonInput.getFloat(j);
      }
      this.inputs.add(input);
    }

    this.targets.clear();
    for (int i = 0; i < jsonTargets.size(); i++) {
      JSONArray jsonTarget = jsonTargets.getJSONArray(i);
      float[] target = new float[jsonTarget.size()];
      for (int j = 0; j < jsonTarget.size(); j++) {
        target[j] = jsonTarget.getFloat(j);
      }
      this.targets.add(target);
    }

    this.healthCheck();
  }
}
TrainingSet_ TrainingSet = new TrainingSet_();
