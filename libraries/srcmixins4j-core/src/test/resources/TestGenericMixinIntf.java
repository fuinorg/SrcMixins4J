package a.b.c;

import java.util.List;

import org.fuin.srcmixins4j.annotations.MixinIntf;

/**
 * Generic mixin interface for test.
 */
@MixinIntf
public interface TestGenericMixinIntf<A, B extends List<A>> extends TestGenericIntf<A, B> {

}
