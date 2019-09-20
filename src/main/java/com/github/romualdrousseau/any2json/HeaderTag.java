package com.github.romualdrousseau.any2json;

public class HeaderTag
{
    public HeaderTag(IHeader header, String value) {
		this.header = header;
		this.value = value;
		this.probability = 0.0f;
    }

	public HeaderTag(IHeader header, String value, float probability) {
		this.header = header;
		this.value = value;
		this.probability = probability;
	}

	public boolean isUndefined() {
		return this.value == null || value.equalsIgnoreCase("none");
	}

	public IHeader getHeader() {
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

	public float getProbability() {
		return this.probability;
	}

	public HeaderTag setProbability(float probability) {
		this.probability = probability;
		return this;
    }

    public boolean equals(HeaderTag other) {
        return !this.isUndefined() && !other.isUndefined() && this.value.equals(other.value);
    }

	public String toString() {
		if(this.isUndefined()) {
			return String.format("[%s, <undefined>, %.1f]", this.header, this.probability);
		}
		else {
			return String.format("[%s, %s, %.1f]", this.header, this.value.toString(), this.probability);
		}
	}

	private IHeader header;
	private String value;
	private float probability;
}
