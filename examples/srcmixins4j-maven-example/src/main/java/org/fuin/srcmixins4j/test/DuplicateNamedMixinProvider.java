package org.fuin.srcmixins4j.test;

import org.fuin.srcmixins4j.annotations.MixinGenerated;
import org.fuin.srcmixins4j.annotations.MixinProvider;

/**
 * Mixin-Implementation of the {@link DuplicateNamedMixin} interface.
 */
@MixinProvider(DuplicateNamedMixin.class)
public final class DuplicateNamedMixinProvider implements DuplicateNamed {

	@Override
	@MixinGenerated(DuplicateNamedMixinProvider.class)
	public final String getName() {
		return "Fixed";
	}
	
}
