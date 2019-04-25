PApplet applet = this;

class JSONObject_ implements com.github.romualdrousseau.shuju.json.JSONObject {
  JSONObject jo;

  JSONObject_(JSONObject jo) {
    this.jo = jo;
  }

  int getInt(String k) {
    return jo.getInt(k);
  }

  void setInt(String k, int n) {
    jo.setInt(k, n);
  }

  float getFloat(String k) {
    return jo.getFloat(k);
  }

  void setFloat(String k, float f) {
    jo.setFloat(k, f);
  }

  String getString(String k) {
    return jo.getString(k);
  }

  void setString(String k, String s) {
    jo.setString(k, s);
  }

  com.github.romualdrousseau.shuju.json.JSONArray getJSONArray(String k) {
    return new JSONArray_(jo.getJSONArray(k));
  }

  void setJSONArray(String k, com.github.romualdrousseau.shuju.json.JSONArray a) {
    jo.setJSONArray(k, ((JSONArray_) a).ja);
  }

  com.github.romualdrousseau.shuju.json.JSONObject getJSONObject(String k) {
    return new JSONObject_(jo.getJSONObject(k));
  }

  void setJSONObject(String k, com.github.romualdrousseau.shuju.json.JSONObject o) {
    jo.setJSONObject(k, ((JSONObject_) o).jo);
  }
}

class JSONArray_ implements com.github.romualdrousseau.shuju.json.JSONArray {
  JSONArray ja;

  JSONArray_(JSONArray ja) {
    this.ja = ja;
  }
  
  int size() {
    return ja.size();
  }

  int getInt(int i) {
    return ja.getInt(i);
  }

  void setInt(int k, int n) {
    ja.setInt(k, n);
  }

  float getFloat(int k) {
    return ja.getFloat(k);
  }

  void setFloat(int k, float f) {
    ja.setFloat(k, f);
  }

  String getString(int k) {
    return ja.getString(k);
  }

  void setString(int k, String s) {
    ja.setString(k, s);
  }

  com.github.romualdrousseau.shuju.json.JSONArray getJSONArray(int k) {
    return new JSONArray_(ja.getJSONArray(k));
  }

  void setJSONArray(int k, com.github.romualdrousseau.shuju.json.JSONArray a) {
    ja.setJSONArray(k, ((JSONArray_) a).ja);
  }

  com.github.romualdrousseau.shuju.json.JSONObject getJSONObject(int k) {
    return new JSONObject_(ja.getJSONObject(k));
  }

  void setJSONObject(int k, com.github.romualdrousseau.shuju.json.JSONObject o) {
    ja.setJSONObject(k, ((JSONObject_) o).jo);
  }

  void append(int i) {
    ja.append(i);
  }

  void append(float f) {
    ja.append(f);
  }

  void append(String s) {
    ja.append(s);
  }
  
  void append(com.github.romualdrousseau.shuju.json.JSONArray a) {
    ja.append(((JSONArray_) a).ja);
  }

  void append(com.github.romualdrousseau.shuju.json.JSONObject o) {
    ja.append(((JSONObject_) o).jo);
  }
}

class JSONProcessingFactory_ implements com.github.romualdrousseau.shuju.json.JSONFactory {
  com.github.romualdrousseau.shuju.json.JSONArray newJSONArray() {
    return new JSONArray_(new JSONArray());
  }

  com.github.romualdrousseau.shuju.json.JSONObject  newJSONObject() {
    return new JSONObject_(new JSONObject());
  }
  
  void saveJSONArray(String filePath, com.github.romualdrousseau.shuju.json.JSONArray a) {
    applet.saveJSONArray(((JSONArray_) a).ja, filePath);
  }
  
  void saveJSONObject(String filePath, com.github.romualdrousseau.shuju.json.JSONObject o) {
    applet.saveJSONObject(((JSONObject_) o).jo, filePath);
  }
  
  com.github.romualdrousseau.shuju.json.JSONArray loadJSONArray(String filePath) {
    return new JSONArray_(applet.loadJSONArray(filePath));
  }
  
  com.github.romualdrousseau.shuju.json.JSONObject loadJSONObject(String filePath) {
    return new JSONObject_(applet.loadJSONObject(filePath));
  }
}
JSONProcessingFactory_ JSONProcessingFactory = new JSONProcessingFactory_();
