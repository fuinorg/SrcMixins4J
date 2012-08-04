package a.b.c;

public class TestMixinUser implements TestMixinIntf, TestAnotherIntf {

    @Override
    public final void sayWow() {
        System.out.println("Wow!");
    }
    
}
