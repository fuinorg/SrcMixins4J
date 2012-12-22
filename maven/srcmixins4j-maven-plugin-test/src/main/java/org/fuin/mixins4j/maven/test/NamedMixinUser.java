package org.fuin.mixins4j.maven.test;

import org.fuin.srcmixins4j.annotations.MixinGenerated;


public class NamedMixinUser implements NamedMixin<String>, DuplicateNamedMixin {

	@MixinGenerated(NamedMixinProvider.class)
	private String name;

	/**
	 * Sets the name to a new value.
	 * 
	 * @param name
	 *            Value to set.
	 */
	@MixinGenerated(NamedMixinProvider.class)
	public final void setName(final String name) {
		this.name = name;
	}

	@Override
	@MixinGenerated(NamedMixinProvider.class)
	public final String getName() {
		return name;
	}

}
