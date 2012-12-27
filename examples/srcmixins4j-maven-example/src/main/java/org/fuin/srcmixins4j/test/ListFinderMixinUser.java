package org.fuin.srcmixins4j.test;

import java.util.List;
import org.fuin.srcmixins4j.annotations.MixinGenerated;

public class ListFinderMixinUser implements ListFinderMixin<MyName, MyClass, List<MyClass>> {

	@Override
	@MixinGenerated(ListFinderMixinProvider.class)
	public MyClass findByName(MyName name,List<MyClass> list) {
		for (final MyClass entry : list) {
			if (entry.getName().equals(name)) {
				return entry;
			}
		}
		return null;
	}

}
