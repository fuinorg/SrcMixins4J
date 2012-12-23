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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.emftext.language.java.JavaClasspath;
import org.fuin.srcmixins4j.core.SrcMixins4JAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds/Removes mixin related code from the compiled classes.
 */
public final class SrcMixins4JCompilationParticipant extends
        CompilationParticipant {

    private static final Logger LOG = LoggerFactory
            .getLogger(SrcMixins4JCompilationParticipant.class);

    private IJavaProject project;

    @Override
    public final boolean isActive(final IJavaProject project) {
        return true;
    }

    @Override
    public int aboutToBuild(final IJavaProject project) {
        LOG.info("aboutToBuild: " + project.getElementName());
        this.project = project;
        return READY_FOR_BUILD;
    }

    @Override
    public final void buildStarting(final BuildContext[] buildContexts,
            final boolean isBatch) {

        LOG.info("buildStarting: batch=" + isBatch + ", annotationProcessor="
                + isAnnotationProcessor());

        if (buildContexts.length == 0) {
            // Nothing to do...
            return;
        }

        final ResourceSet resourceSet = SrcMixins4JPlugin.getDefault()
                .getResourceSet(project);

        new SrcMixins4JAnalyzer()
                .analyze(new SrcMixins4JAnalyzerEclipseContext(project,
                        buildContexts, resourceSet));

    }

    @Override
    public final void buildFinished(final IJavaProject project) {
        if (LOG.isDebugEnabled()) {
            final ResourceSet resourceSet = SrcMixins4JPlugin.getDefault()
                    .getResourceSet(project);
            dumpClassPath(project, resourceSet);
        }
        LOG.info("buildFinished: " + project.getElementName());
    }

    // @formatter:off
    private static void dumpClassPath(final IJavaProject project, final ResourceSet resourceSet) {
        LOG.debug("PROJECT: " + project.getElementName());
        if (resourceSet == null) {
            LOG.info("RESOURCE SET: null");
            return;
        }
        LOG.debug("RESOURCE SET: " + resourceSet.eAdapters().size()
                + " eAdapters");
        for (final Adapter a : resourceSet.eAdapters()) {
            LOG.debug("ADAPTER: " + a);
            if (a instanceof JavaClasspath) {
                final JavaClasspath cp = (JavaClasspath) a;
                final Map<String, List<String>> pcMap = cp
                        .getPackageClassifierMap();
                LOG.debug("JavaClasspath: " + pcMap.size() + " keys");
                final Iterator<String> it = pcMap.keySet().iterator();
                while (it.hasNext()) {
                    final String key = it.next();
                    if (LOG.isTraceEnabled() || !isExcluded(key)) {
                        final StringBuilder sb = new StringBuilder(key + ": ");
                        final List<String> values = pcMap.get(key);
                        for (int i = 0; i < values.size(); i++) {
                            if (i > 0) {
                                sb.append(", ");
                            }
                            sb.append("[" + i + "]=" + values.get(i));
                        }
                        LOG.debug("CP KEY: " + sb.toString());
                    }
                }
            }
        }
    }
    // @formatter:on

    private static boolean isExcluded(final String key) {
        // @formatter:off
        return key.startsWith("java.") 
                || key.startsWith("javax.")
                || key.startsWith("com.") 
                || key.startsWith("sun.")
                || key.startsWith("org.jcp.")
                || key.startsWith("org.w3c.")
                || key.startsWith("org.omg.")
                || key.startsWith("org.xml.")
                || key.startsWith("org.ietf.")
                || key.startsWith("sunw.") 
                || key.startsWith("zkasig.")
                || key.startsWith("oracle.")
                || key.startsWith("javafx.")                            
                || key.startsWith("netscape.");
        // @formatter:on
    }

}
