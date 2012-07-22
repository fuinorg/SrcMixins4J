package a.b.c;

import org.fuin.srcmixins4j.annotations.MixinGenerated;
import org.fuin.srcmixins4j.annotations.MixinProvider;

/**
 * Provider for test.
 */
@MixinProvider(TestMixinIntf.class)
public class TestMixinProvider implements TestIntf {

    @MixinGenerated(TestMixinProvider.class)
    private String xyz;

    @Override
    @MixinGenerated(TestMixinProvider.class)
    public final String getXyz() {
        return xyz;
    }

    public final void sayHello() {
        // This method will NOT be copied into the mixin user
        // class as it has no @MixinGenerated annotation
        System.out.println("Hello!");
    }

}
