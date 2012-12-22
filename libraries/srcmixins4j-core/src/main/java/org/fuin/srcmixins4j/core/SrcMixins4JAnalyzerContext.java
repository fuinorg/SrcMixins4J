/**
 * Copyright (C) 2012 Future Invent Informationsmanagement GmbH. All rights
 * reserved. <http://www.fuin.org/>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fuin.srcmixins4j.core;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.Interface;

/**
 * Context used when analyzing source files to apply or remove mixin code.
 */
public interface SrcMixins4JAnalyzerContext extends SrcMixins4JAnalyzerLog {

    /**
     * Returns the current resource set.
     * 
     * @return Resource set.
     */
    public ResourceSet getResourceSet();

    /**
     * Returns the information if there are more resources.
     * 
     * @return If the iterator has more elements TRUE else FALSE.
     */
    public boolean hasNextResource();

    /**
     * Returns the next resource. Throws an {@link NoSuchElementException} if
     * the iteration has no more elements.
     * 
     * @return The next element in the iteration - Never NULL.
     */
    public Resource nextResource();

    /**
     * Tags the last resource returned by the {@link #nextResource()} method as
     * changed. This method can be called only once per call to
     * {@link #nextResource()}. Throws an {@link IllegalStateException} if the
     * {@link #nextResource()} method has not yet been called.
     */
    public void markResourceAsChanged();

    /**
     * Returns the mixin provider for a given mixin interface. If more then one
     * provider class exists (should never be the case) the first one is
     * returned.
     * 
     * @param mixinIntf
     *            Interface to find the provider for.
     * 
     * @return Provider or NULL if none was found.
     */
    public Class findMixinProvider(Interface mixinIntf);

    /**
     * Returns a list of mixin users for a given mixin interface.
     * 
     * @param mixinIntf
     *            Interface to find user classes for.
     * 
     * @return List of mixin users. Never NULL but collection may be empty.
     */
    public List<Class> findMixinUsers(Interface mixinIntf);

}
