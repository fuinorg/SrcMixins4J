package a.b.c;

import org.fuin.srcmixins4j.annotations.MixinIntf;

/**
 * Mixin interface that has the same method as {@link TestIntf}.
 */
@MixinIntf
public interface DuplicateMixinIntf extends DuplicateIntf {

}
