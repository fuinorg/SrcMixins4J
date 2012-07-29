package org.fuin.srcmixins4j.test;

import org.fuin.srcmixins4j.annotations.MixinGenerated;
import org.fuin.srcmixins4j.annotations.MixinProvider;

/**
 * Mixin-Implementation of the {@link Named} interface.
 */
@MixinProvider(NamedMixin.class)
public final class NamedMixinProvider<T> implements Named<T> {

	@MixinGenerated(NamedMixinProvider.class)
	private T name;

	/**
	 * Sets the name to a new value.
	 * 
	 * @param name
	 *            Value to set.
	 */
	@MixinGenerated(NamedMixinProvider.class)
	public final void setName(final T name) {
		this.name = name;
	}

	@Override
	@MixinGenerated(NamedMixinProvider.class)
	public final T getName() {
		return name;
	}
	
}
