package com.github.romualdrousseau.any2json;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.github.romualdrousseau.shuju.Result;
import com.github.romualdrousseau.shuju.DataSet;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.IFeature;
import com.github.romualdrousseau.shuju.features.RegexFeature;
import com.github.romualdrousseau.shuju.features.FuzzyFeature;
import com.github.romualdrousseau.shuju.IClassifier;
import com.github.romualdrousseau.shuju.util.Election;
import com.github.romualdrousseau.shuju.util.Winner;

import com.github.romualdrousseau.any2json.util.StringUtility;

public abstract class ITable
{
	public List<TableHeader> headers() {
		return m_headers;
	}

	public boolean hasHeaders() {
		return m_headers.size() > 0;
	}

	public int getNumberOfHeaders() {
		return m_headers.size();
	}

	public TableHeader getHeaderAt(int colIndex) {
		return m_headers.get(colIndex);
	}

	public TableHeader getHeaderByTag(String tagName) {
		return m_headersByTag.get(tagName);
	}

	public ITable clearHeaders() {
		m_headers.clear();
		return this;
	}

	public abstract int getNumberOfColumns();

	public abstract int getNumberOfRows();

	public abstract IRow getRowAt(int i);

	public void updateTags(IClassifier classifier, int sampleCount) {
		HashMap<String, HeaderTag> tags = new HashMap<String, HeaderTag>();

		for(TableHeader header: m_headers) {
			// predict best match from a sample of a given column
            List<Result> results = new ArrayList<Result>();
			for(int i = 0; i < Math.min(sampleCount, getNumberOfRows()); i++) {
	            String value = getRowAt(i).getCellValue(header);
	            DataRow sample = new DataRow()
                    .addFeature(new RegexFeature(value))
                    .addFeature(new FuzzyFeature(header.getName()));
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
            for(IFeature label: winner.getCandidate().labels()) {
                double p = winner.getCandidate().getProbability() * label.getProbability();
                HeaderTag tag = tags.get(label.getValue().toString());
                if(tag == null) {
                	tag = new HeaderTag(header, label.getValue().toString(), p);
                	tags.put(tag.getValue(), tag);
                }
                else if(p > tag.getProbability()) {
                    tag.setHeader(header);
                    tag.setProbability(p);
                }
            }
		}

		// set the best tags to the header
		for(TableHeader header: m_headers) {
			double maxP = 0.0;
			for(Entry<String, HeaderTag> entry: tags.entrySet()) {
				HeaderTag tag = entry.getValue();
				if(!tag.isUndefined() && tag.getHeader() == header && tag.getProbability() > maxP) {
					maxP = tag.getProbability();
					header.setTag(tag);
					m_headersByTag.put(header.getTag().getValue(), header);
				}
			}
		}
	}

	protected ITable addHeader(TableHeader header) {
		m_headers.add(header);
		header.setTable(this);
		if(header.getTag() != null) {
			m_headersByTag.put(header.getTag().getValue(), header);
		}	
		return this;
	}

	protected ITable setHeader(int colIndex, TableHeader newHeader) {
		m_headers.set(colIndex, newHeader);
		if(newHeader.getTag() != null) {
			m_headersByTag.put(newHeader.getTag().getValue(), newHeader);
		}	
		return this;
	}

	protected String cleanHeaderToken(String token) {
		token = StringUtility.removeWhiteSpaces(token);
		token = token.replaceAll("\\(.*\\)", "");
		token = token.replaceAll("/.*", "");
		token = token.replaceAll("▲", "");
		return token;
	}

	protected String cleanValueToken(String token) {
		token = StringUtility.normalizeWhiteSpaces(token);
		token = StringUtility.trim(token, StringUtility.WHITE_SPACES + "\"");
		return token;
	}

	private ArrayList<TableHeader> m_headers = new ArrayList<TableHeader>();
	private HashMap<String, TableHeader> m_headersByTag = new HashMap<String, TableHeader>();
}