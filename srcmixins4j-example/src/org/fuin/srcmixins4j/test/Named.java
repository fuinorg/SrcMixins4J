package org.fuin.srcmixins4j.test;

/**
 * An object with a name.
 */
public interface Named<T> {

	/**
	 * Returns the name.
	 * 
	 * @return Name - Never <code>null</code>.
	 */
	public T getName();

}
