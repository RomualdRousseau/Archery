package com.github.romualdrousseau.any2json;

import java.io.File;

public interface Document extends AutoCloseable
{
	boolean open(File file, String encoding, final String password, final boolean wellFormed);

	void close();

	int getNumberOfSheets();

	Sheet getSheetAt(int i);
}
