package org.fuin.srcmixins4j.test;

import org.fuin.srcmixins4j.annotations.MixinIntf;

/**
 * An object with a name. Automatically adds an implementation for the extended
 * interface to the class that implements this one.
 */
@MixinIntf
public interface NamedMixin<T> extends Named<T> {

}
