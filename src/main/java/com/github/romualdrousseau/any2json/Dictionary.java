package com.github.romualdrousseau.any2json;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.github.romualdrousseau.shuju.DataSet;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.features.StringFeature;
import com.github.romualdrousseau.shuju.features.FuzzyFeature;
import com.github.romualdrousseau.shuju.features.RegexFeature;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Dictionary
{
    public DataSet getDataSet() {
        return this.dataset;
    }

    public void loadFromFile(String fileName) throws IOException, ParseException {
        if(fileName == null) {
            throw new IllegalArgumentException();
        }

        this.definitions = new ArrayList<Definition>();

        JSONParser parser = new JSONParser();
        JSONArray jsonDefinitions = (JSONArray) parser.parse(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));

        for(int i = 0; i < jsonDefinitions.size(); i++) {
            JSONObject jsonDefinition = (JSONObject) jsonDefinitions.get(i);  

            Definition definition = new Definition();
            definition.tag = (String) jsonDefinition.get("tag");

            JSONArray jsonPatterns = (JSONArray) jsonDefinition.get("patterns");
            for(int j = 0; j < jsonPatterns.size(); j++) {
                JSONObject jsonPattern = (JSONObject) jsonPatterns.get(j); 
                definition.patterns().add(new WeightedString(
                    (String) jsonPattern.get("value"),
                    (Double) jsonPattern.get("weight")));                
            }

            JSONArray jsonWords = (JSONArray) jsonDefinition.get("words");
            for(int j = 0; j < jsonWords.size(); j++) {
                JSONObject jsonWord = (JSONObject) jsonWords.get(j); 
                definition.words().add(new WeightedString(
                    (String) jsonWord.get("value"),
                    (Double) jsonWord.get("weight"),
                    (String) jsonWord.get("option")));            
            }

            this.definitions.add(definition);
        }

        this.dataset = buildDataSet();
    }

    public void saveToFile(String fileName) throws IOException {
        if(fileName == null) {
            throw new IllegalArgumentException();
        }
        
        OutputStreamWriter file = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
        file.write(toJSON().toJSONString());
        file.close();
    }

    @SuppressWarnings("unchecked")
    public JSONArray toJSON() {
        JSONArray arr = new JSONArray();
        for(Definition definition: this.definitions) {
            arr.add(definition.toJSON());
        }
        return arr;
    }

    private DataSet buildDataSet() {
        DataSet dataset = new DataSet();
        String buildNegPattern = "";
        boolean firstNegPattern = true;

        for(Definition definition: this.definitions) {
            for(WeightedString pattern: definition.patterns()) {
                for(WeightedString word: definition.words()) {
                    dataset.addRow(new DataRow()
                        .addFeature(new RegexFeature(pattern.getValue()))
                        .addFeature(new FuzzyFeature(word.getValue()).setTokenizer("tokenize".equals(word.getOption()), " "))
                        .setLabel(new StringFeature(definition.getTag(), word.getWeight())));
                }
            
                // Build negative regex to catch every string without regex defined (i.e. ".*")
                if(!buildNegPattern.contains(pattern.getValue()) && !pattern.getValue().equals(".*")) {
                    if(firstNegPattern) {
                        buildNegPattern = "(" + pattern.getValue() + ")";
                        firstNegPattern = false;
                    }
                    else {
                        buildNegPattern += "|(" + pattern.getValue() + ")";
                    }  
                }
            }
        }

        for(DataRow row: dataset.rows()) {
            RegexFeature regexFeature = (RegexFeature) row.features().get(0);
            if(regexFeature.getValue().equals(".*")) {
                regexFeature.setValue("\\neg(" + buildNegPattern + ")");
            }
        }

        return dataset;      
    }

    class Definition
    {
        public String getTag() {
            return this.tag;
        } 

        public void setTag(String tag) {
            this.tag = tag;
        } 

        public List<WeightedString> patterns() {
            return this.patterns;
        } 

        public List<WeightedString> words() {
            return this.words;
        }

        @SuppressWarnings("unchecked")
        public JSONObject toJSON() {
            JSONArray arr1 = new JSONArray();
            for(WeightedString pattern : this.patterns) {
                arr1.add(pattern.toJSON());
            }
            
            JSONArray arr2 = new JSONArray();
            for(WeightedString word : this.words) {
                arr2.add(word.toJSON());
            }

            JSONObject obj = new JSONObject();
            obj.put("tag", this.tag);
            obj.put("patterns", arr1);
            obj.put("words", arr2);
            return obj;
        }

        private String tag;
        private List<WeightedString> patterns = new ArrayList<WeightedString>();
        private List<WeightedString> words = new ArrayList<WeightedString>();
    }

    class WeightedString
    {
        public WeightedString(String v, double w) {
            this.value = v;
            this.weight = w;
            this.option = null;
        }

        public WeightedString(String v, double w, String o) {
            this.value = v;
            this.weight = w;
            this.option = o;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public double getWeight() {
            return this.weight;
        }

        public String getOption() {
            return this.option;
        }        

        @SuppressWarnings("unchecked")
        public JSONObject toJSON() {
            JSONObject obj = new JSONObject();
            obj.put("value", this.value);
            obj.put("weight", this.weight);
            obj.put("option", this.option);
            return obj;
        }

        private String value;
        private double weight;
        private String option;
    }

    private List<Definition> definitions;
    private DataSet dataset;
}
