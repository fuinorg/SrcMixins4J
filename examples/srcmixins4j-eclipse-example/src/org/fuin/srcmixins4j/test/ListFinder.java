package org.fuin.srcmixins4j.test;

import java.util.List;

public interface ListFinder<NAME, TYPE extends Named<NAME>, LIST extends List<TYPE>> {

	public TYPE findByName(NAME name, LIST list);	
	
}
