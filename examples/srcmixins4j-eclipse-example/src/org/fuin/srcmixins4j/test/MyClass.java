package org.fuin.srcmixins4j.test;

import org.fuin.srcmixins4j.annotations.MixinGenerated;

public class MyClass extends MyParent implements NamedMixin<MyName> {

	@MixinGenerated(NamedMixinProvider.class)
	private MyName name;

	/**
	 * Sets the name to a new value.
	 * 
	 * @param name
	 *            Value to set.
	 */
	@MixinGenerated(NamedMixinProvider.class)
	public final void setName(final MyName name) {
		this.name = name;
	}

	@Override
	@MixinGenerated(NamedMixinProvider.class)
	public final MyName getName() {
		return name;
	}

}
