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
package org.fuin.srcmixins4j.plugin;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.fuin.srcmixins4j.core.SrcMixins4JAnalyzer;

/**
 * Adds/Removes mixin related code from the compiled classes.
 */
public final class SrcMixins4JCompilationParticipant extends
        CompilationParticipant {

    private IJavaProject project;

    @Override
    public final boolean isActive(final IJavaProject project) {
        return true;
    }

    @Override
    public int aboutToBuild(final IJavaProject project) {
        this.project = project;
        return READY_FOR_BUILD;
    }

    @Override
    public final void buildStarting(final BuildContext[] buildContexts,
            final boolean isBatch) {

        if (buildContexts.length == 0) {
            // Nothing to do...
            return;
        }

        final ResourceSet resourceSet = SrcMixins4JPlugin.getDefault()
                .getResourceSet(project);

        new SrcMixins4JAnalyzer().analyze(new SrcMixins4JAnalyzerEclipseContext(project,
                buildContexts, resourceSet));

    }

}
