package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.shuju.IClassifier;

public interface ITable
{
	public Iterable<TableHeader> headers();

	public int getNumberOfHeaders();

	public TableHeader getHeaderAt(int colIndex);

	public TableHeader getHeaderByTag(String tagName);

	public boolean hasHeaders();

	public void clearHeaders();

	public int getNumberOfColumns();

	public Iterable<IRow> rows();

	public int getNumberOfRows();

	public IRow getRowAt(int i);

	public void updateHeaderTags(IClassifier classifier, int sampleCount);
}