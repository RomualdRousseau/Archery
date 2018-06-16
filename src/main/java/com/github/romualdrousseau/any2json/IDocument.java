package com.github.romualdrousseau.any2json;

import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class IDocument
{
	public abstract boolean open(File excelFile, String encoding);

	public abstract void close();

	public abstract int getNumberOfSheets();

	public abstract ISheet getSheetAt(int i);
}
