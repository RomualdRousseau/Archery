package org.any2json;

import java.util.HashMap;

public abstract class IFilter
{
	public abstract void filter(HashMap<String, TableHeader> in, HashMap<String, TableHeader> out);
}