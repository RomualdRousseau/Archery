package com.github.romualdrousseau.any2json;

public class HeaderTag
{
	public HeaderTag(TableHeader header, String value, double probability) {
		this.header = header;
		this.value = value.equals("none") ? null : value;
		this.probability = probability;
	}

	public boolean isUndefined() {
		return this.value == null;
	}

	public TableHeader getHeader() {
		return this.header;
	}

	public HeaderTag setHeader(TableHeader header) {
		this.header = header;
		return this;
	}

	public String getValue() {
		return this.value;
	}

	public HeaderTag setValue(String value) {
		this.value = value;
		return this;
	}

	public double getProbability() {
		return this.probability;
	}

	public HeaderTag setProbability(double probability) {
		this.probability = probability;
		return this;
	}

	public String toString() {
		if(this.value == null) {
			return String.format("[%s, <undefined>, %.1f]", this.header, this.probability);	
		}
		else {
			return String.format("[%s, %s, %.1f]", this.header, this.value.toString(), this.probability);
		}
	}

	private TableHeader header;
	private String value;
	private double probability;
}
