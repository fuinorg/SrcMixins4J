package org.fuin.srcmixins4j.test;

import java.util.List;

import org.fuin.srcmixins4j.annotations.MixinIntf;

@MixinIntf
public interface ListFinderMixin<NAME, TYPE extends Named<NAME>, LIST extends List<TYPE>> extends ListFinder<NAME, TYPE, LIST> {

}
