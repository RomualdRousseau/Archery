package com.github.romualdrousseau.any2json;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Map.Entry;

import com.github.romualdrousseau.shuju.Result;
//import com.github.romualdrousseau.shuju.DataSet;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.IFeature;
import com.github.romualdrousseau.shuju.features.RegexFeature;
import com.github.romualdrousseau.shuju.features.FuzzyFeature;
import com.github.romualdrousseau.shuju.IClassifier;
import com.github.romualdrousseau.shuju.util.Election;
import com.github.romualdrousseau.shuju.util.Winner;

import com.github.romualdrousseau.any2json.util.StringUtility;

public abstract class Table implements ITable
{
	public static boolean IsEmpty(ITable table) {
		return table == null || table.getNumberOfHeaders() == 0;
	}

	public Iterable<TableHeader> headers() {
		return this.headers;
	}

	public int getNumberOfHeaders() {
		return this.headers.size();
	}

	public TableHeader getHeaderAt(int colIndex) {
		return this.headers.get(colIndex);
	}

	public TableHeader getHeaderByTag(String tagName) {
		if(tagName == null) {
			throw new IllegalArgumentException();
		}

		return this.headersByTag.get(tagName);
	}

	public boolean hasHeaders() {
		return this.headers.size() > 0;
	}

	public void clearHeaders() {
		this.headers.clear();
	}

	public Iterable<IRow> rows() {
		return new RowIterable(this);
	}

	public void updateHeaderTags(IClassifier classifier, int sampleCount) {
		if(classifier == null) {
			throw new IllegalArgumentException();
		}

		HashMap<String, HeaderTag> tags = new HashMap<String, HeaderTag>();

		for(TableHeader header: this.headers) {
			// predict best match from a sample of a given column
            ArrayList<Result> results = new ArrayList<Result>();
			for(int i = 0; i < Math.min(sampleCount, getNumberOfRows()); i++) {

	            DataRow sample = new DataRow()
                    .addFeature(new RegexFeature(getRowAt(i).getCellValue(header)))
                    .addFeature(new FuzzyFeature(StringUtility.cleanHeaderToken(header.getName())));

	            Result result = classifier.predict(sample);
	            if(!result.isUndefined()) {
	                results.add(result);
	            }
	        }

	        // Select the best result for a column
	        Winner<Result> winner = new Election<Result>().vote(results);
	        if(winner == null) {
	        	continue;
	        }

	        // Update the tags list if the result is better
            for(IFeature<?> label: winner.getCandidate().labels()) {
            	String k = label.getValue().toString();
                double p = winner.getCandidate().getProbability() * label.getProbability();
                HeaderTag tag = tags.get(k);
                if(tag == null) {
                	tag = new HeaderTag(header, k, p);
                	tags.put(k, tag);
                }
                else if(p > tag.getProbability()) {
                    tag.setHeader(header);
                    tag.setProbability(p);
                }
            }
		}

		// set the best tags to the header
		for(TableHeader header: this.headers) {
			double maxP = 0.0;
			for(HeaderTag tag: tags.values()) if(tag.getHeader() == header) {
				if(!tag.isUndefined() && tag.getProbability() > maxP) {
					maxP = tag.getProbability();
					header.setTag(tag);
					this.headersByTag.put(header.getTag().getValue(), header);
				}
			}
		}

		// for(TableHeader header: this.headers) if(header.hasTag()) {
		//  String buffer = "";
		//  for(TableHeader other: this.headers) if(other.hasTag() && other != header) {
		//  	buffer += other.getName();
		//  }
		//  buffer = String.format("%s, %s, %s", word2vec(header.getName()), word2vec(buffer), header.getTag().getValue());
		//  System.out.println(buffer);
		// }

		// System.out.println(vectorSpace);
	}

	// private static String word2vec(String s) {
	// 	char[] result = new char[100];

	// 	for(int i = 0; i < result.length; i++) {
	// 		result[i] = '0';
	// 	}

	// 	for(char c: s.toCharArray()) if(Character.isLetter(c)) {
	// 		if(!vectorSpace.contains(c)) {
	// 			vectorSpace.add(c);
	// 		}

	// 		int i = vectorSpace.indexOf(c);
	// 		result[i] = '1';
	// 	}

	// 	return String.valueOf(result);
	// }

	// private static ArrayList<Character> vectorSpace = new ArrayList<Character>();

	protected ITable addHeader(TableHeader header) {
		this.headers.add(header);
		header.setTable(this);
		if(header.getTag() != null) {
			this.headersByTag.put(header.getTag().getValue(), header);
		}
		return this;
	}

	private ArrayList<TableHeader> headers = new ArrayList<TableHeader>();
	private HashMap<String, TableHeader> headersByTag = new HashMap<String, TableHeader>();
}
