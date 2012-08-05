package a.b.c;

public class TestMixinUser2 implements TestMixinIntf {

    // This field is normally provided by the mixin, 
    // but explicitly "overridden" by this class
    private String xyz;
    
    @Override
    public final String getXyz() {
        // This method is normally provided by the mixin, 
        // but explicitly "overridden" by this class
        return xyz;
    }
    
}
