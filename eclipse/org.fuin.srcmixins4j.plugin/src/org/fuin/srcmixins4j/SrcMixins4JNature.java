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
package org.fuin.srcmixins4j;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Defines a special nature for SrcMixin4J projects.
 */
public final class SrcMixins4JNature implements IProjectNature {
    
    /** Unique nature ID. */
    public static final String NATURE_ID = "org.fuin.srcmixins4.srcMixins4JNature";

    private IProject project;
    
    @Override
    public final void configure() throws CoreException {
        // Nothing to do
    }

    @Override
    public final void deconfigure() throws CoreException {
        // Nothing to do
    }

    @Override
    public final IProject getProject() {
        return project;
    }

    @Override
    public final void setProject(final IProject project) {
        this.project = project;        
    } 

}
