package com.github.romualdrousseau.any2json.v2;

import java.io.File;

public interface IDocument extends AutoCloseable {

	public boolean open(File excelFile, String encoding);

	public void close();

	public int getNumberOfSheets();

	public ISheet getSheetAt(int i);
}
