package a.b.c;

import org.fuin.srcmixins4j.annotations.MixinGenerated;
import org.fuin.srcmixins4j.annotations.MixinProvider;

/**
 * Provider that has the same method as {@link TestMixinProvider}.
 */
@MixinProvider(DuplicateMixinIntf.class)
public class DuplicateMixinProvider implements DuplicateIntf {

    @MixinGenerated(DuplicateMixinProvider.class)
    public final String getXyz() {
        return "ABC";
    }

}
