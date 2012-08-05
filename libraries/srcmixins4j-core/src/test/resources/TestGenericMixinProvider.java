package a.b.c;

import java.util.List;

import org.fuin.srcmixins4j.annotations.MixinGenerated;
import org.fuin.srcmixins4j.annotations.MixinProvider;

@MixinProvider(TestGenericMixinIntf.class)
public class TestGenericMixinProvider<A, B extends List<A>> implements TestGenericIntf<A, B> {

    @Override
    @MixinGenerated(TestGenericMixinProvider.class)
    public A getA() {
        // Always return null
        return null;
    }

    @Override
    @MixinGenerated(TestGenericMixinProvider.class)
    public void add(B b) {
        // Whatever
    }

}
