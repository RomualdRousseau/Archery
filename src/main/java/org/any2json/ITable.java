package org.any2json;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

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

	protected ITable addHeader(TableHeader header) {
		m_headers.add(header);
		if(header.tag != null) {
			m_headersByTag.put(header.tag, header);
		}	
		return this;
	}

	protected ITable setHeader(int colIndex, TableHeader newHeader) {
		m_headers.set(colIndex, newHeader);
		if(newHeader.tag != null) {
			m_headersByTag.put(newHeader.tag, newHeader);
		}	
		return this;
	}

	

	private ArrayList<TableHeader> m_headers = new ArrayList<TableHeader>();
	private HashMap<String, TableHeader> m_headersByTag = new HashMap<String, TableHeader>();
}