package a.b.c;

import java.util.ArrayList;
import java.util.List;
import org.fuin.srcmixins4j.annotations.MixinGenerated;

public class TestGenericMixinUser<A> implements TestGenericMixinIntf<A, ArrayList<A>> {

    @Override
    @MixinGenerated(TestGenericMixinProvider.class)
    public A getA() {
        // Always return null
        return null;
    }

    @Override
    @MixinGenerated(TestGenericMixinProvider.class)
    public void add(ArrayList<A> b) {
        // Whatever
    }

}
