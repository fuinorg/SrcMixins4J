package a.b.c;

import java.util.List;

/**
 * Interface with generics for test.
 */
public interface TestGenericIntf<A, B extends List<A>> {

    /**
     * Returns A.
     * 
     * @return A.
     */
    public A getA();

    /**
     * Adds B.
     * 
     * @param b Add B.
     */
    public void add(B b);
    
}
