package com.github.romualdrousseau.any2json.v2;

public class HeaderTag
{
    public HeaderTag(Header header, String value) {
		this.header = header;
		this.value = value;
    }

	public Header getHeader() {
		return this.header;
	}

	public HeaderTag setHeader(Header header) {
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

    public boolean isUndefined() {
		return this.value == null || value.equalsIgnoreCase("none");
	}

    public boolean equals(HeaderTag other) {
        return !this.isUndefined() && !other.isUndefined() && this.value.equals(other.value);
    }

	public String toString() {
		if(this.isUndefined()) {
			return String.format("[%s, <undefined>]", this.header);
		}
		else {
			return String.format("[%s, %s]", this.header, this.value);
		}
	}

	private Header header;
	private String value;
}
