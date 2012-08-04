package org.fuin.srcmixins4j.test;

import org.fuin.srcmixins4j.annotations.MixinGenerated;

public abstract class MyParent implements PackageableMixin {

	@MixinGenerated(PackageableMixinProvider.class)
	private String pkg;

	@Override
	@MixinGenerated(PackageableMixinProvider.class)
	public final String getPkg() {
		// Standard
		return pkg;
	}

}
