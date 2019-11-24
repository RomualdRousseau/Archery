package com.github.romualdrousseau.any2json.v2;

public class HeaderTag {

    public final static HeaderTag None = new HeaderTag("none");

    public HeaderTag(String value) {
		this.value = value;
    }

	public String getValue() {
		return this.value;
	}

    public boolean isUndefined() {
		return this.value == null || value.equalsIgnoreCase(HeaderTag.None.getValue());
	}

    public boolean equals(HeaderTag other) {
        return !this.isUndefined() && !other.isUndefined() && this.value.equalsIgnoreCase(other.value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof HeaderTag && this.equals((HeaderTag) o);
    }

	private String value;
}
