package a.b.c;

import org.fuin.srcmixins4j.annotations.MixinGenerated;

public class TestMixinUser3 implements TestMixinIntf {

    @MixinGenerated(TestMixinProvider.class)
    private String xyz;

    @Override
    @MixinGenerated(TestMixinProvider.class)
    public final String getXyz() {
        return xyz;
    }

}
