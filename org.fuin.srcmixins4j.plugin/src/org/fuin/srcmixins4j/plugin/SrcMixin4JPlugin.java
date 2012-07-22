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

import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jdt.core.IJavaProject;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.resource.java.IJavaOptions;
import org.osgi.framework.BundleContext;

/**
 * Handles mixin source code generation in Eclipse.
 */
public final class SrcMixin4JPlugin extends Plugin {

    private static SrcMixin4JPlugin plugin;

    private ResourceSet resourceSet = null;

    private IJavaProject currentProject = null;

    @Override
    public final void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public final void stop(final BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the plugin itself.
     *
     * @return Plugin.
     */
    public static SrcMixin4JPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns a resource set for a given project. The resource set is cached
     * until a resource set for another class path is requested.
     *
     * @param project
     *            Project to return a resource set for.
     *
     * @return Resource set.
     */
    public final ResourceSet getResourceSet(final IJavaProject project) {

        if (resourceSet == null) {
            System.out.println("Create new ResourceSet");
            resourceSet = new ResourceSetImpl();
            resourceSet.getLoadOptions().put(
                    IJavaOptions.DISABLE_CREATING_MARKERS_FOR_PROBLEMS,
                    Boolean.TRUE);
        }

        if (!project.equals(currentProject)) {
            System.out.println("project != currentProject [project="
                    + asString(project) + ", currentProject="
                    + asString(currentProject) + "]");
            resetClasspath();
        }
        currentProject = project;

        return resourceSet;
    }

    private static String asString(final IJavaProject project) {
        if (project == null) {
            return "null";
        }
        return project.getElementName() + "@"
                + Integer.toHexString(System.identityHashCode(project));
    }

    /**
     * Reset the cached class path.
     */
    public final void resetClasspath() {
        for (Adapter a : resourceSet.eAdapters()) {
            if (a instanceof JavaClasspath) {
                resourceSet.eAdapters().remove(a);
                break;
            }
        }
    }

}
