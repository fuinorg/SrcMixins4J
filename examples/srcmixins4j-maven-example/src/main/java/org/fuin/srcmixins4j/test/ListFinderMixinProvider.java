package org.fuin.srcmixins4j.test;

import java.util.List;

import org.fuin.srcmixins4j.annotations.MixinGenerated;
import org.fuin.srcmixins4j.annotations.MixinProvider;

@MixinProvider(ListFinderMixin.class)
public class ListFinderMixinProvider<NAME, TYPE extends Named<NAME>, LIST extends List<TYPE>>
		implements ListFinder<NAME, TYPE, LIST> {

	@Override
	@MixinGenerated(ListFinderMixinProvider.class)
	public TYPE findByName(NAME name, LIST list) {
		for (final TYPE entry : list) {
			if (entry.getName().equals(name)) {
				return entry;
			}
		}
		return null;
	}

}
