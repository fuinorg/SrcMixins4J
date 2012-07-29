package org.fuin.srcmixins4j.test;

import org.fuin.srcmixins4j.annotations.MixinGenerated;
import org.fuin.srcmixins4j.annotations.MixinProvider;

@MixinProvider(PackageableMixin.class)
public class PackageableMixinProvider implements Packageable {

	@MixinGenerated(PackageableMixinProvider.class)
	private String pkg;

	@Override
	@MixinGenerated(PackageableMixinProvider.class)
	public final String getPkg() {
		// Standard
		return pkg;
	}
	
}
