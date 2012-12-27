package org.fuin.srcmixins4j.test;

public final class MyName {

	private final String name;
	
	public MyName(final String name) {
		super();
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}
		if (name.trim().length() == 0) {
			throw new IllegalArgumentException("name is empty");
		}
		this.name = name;
	}
	
	@Override
	public final String toString() {
		return name;
	}
	
}
