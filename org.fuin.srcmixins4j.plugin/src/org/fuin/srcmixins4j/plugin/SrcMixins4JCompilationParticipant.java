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

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.emftext.language.java.resource.java.mopp.JavaMarkerHelper;

/**
 * Adds/Removes mixin related code from the compiled classes.
 */
public final class SrcMixins4JCompilationParticipant extends CompilationParticipant {

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

        for (final BuildContext buildContext : buildContexts) {
            final Resource resource = loadResource(resourceSet,
                    buildContext.getFile());
            new SrcMixins4JAnalysis().analyze(project, buildContext, resourceSet,
                    resource);

        }

    }

    private Resource loadResource(final ResourceSet resourceSet,
            final IFile file) {

        final URI uri = URI.createPlatformResourceURI(file.getFullPath()
                .toString(), true);

        Resource resource = resourceSet.getResource(uri, false);
        if (resource != null) {
            resource.unload();
            new JavaMarkerHelper().unmark(resource);
        }
        resource = resourceSet.getResource(uri, true);

        System.out.println();

        return resource;
    }

// @formatter:off
//  private static void dumpClassPath(final IJavaProject project,
//          final ResourceSet resourceSet, final String label) {
//      System.out.println("--- " + label + " ---");
//      System.out.println("PROJECT: " + project.getElementName());
//      if (resourceSet == null) {
//          System.out.println("RESOURCE SET: null");
//          return;
//      }
//      System.out.println("RESOURCE SET: " + resourceSet.eAdapters().size()
//              + " eAdapters");
//      for (Adapter a : resourceSet.eAdapters()) {
//          System.out.println("Adapter: " + a);
//          if (a instanceof JavaClasspath) {
//              final JavaClasspath cp = (JavaClasspath) a;
//              final Map<String, List<String>> pcMap = cp
//                      .getPackageClassifierMap();
//              System.out.println("JavaClasspath: " + pcMap.size() + " keys");
//              final Iterator<String> it = pcMap.keySet().iterator();
//              while (it.hasNext()) {
//                  final String key = it.next();
//                  if (!(key.startsWith("java.") || key.startsWith("javax.")
//                          || key.startsWith("com.") || key.startsWith("sun.")
//                          || key.startsWith("org.jcp.")
//                          || key.startsWith("org.w3c.")
//                          || key.startsWith("org.omg.")
//                          || key.startsWith("org.xml.")
//                          || key.startsWith("org.ietf.")
//                          || key.startsWith("sunw.") || key
//                              .startsWith("zkasig."))) {
//                      System.out.print(key + ": ");
//                      final List<String> values = pcMap.get(key);
//                      for (int i = 0; i < values.size(); i++) {
//                          if (i > 0) {
//                              System.out.print(", ");
//                          }
//                          System.out.print("[" + i + "]=" + values.get(i));
//                      }
//                      System.out.println();
//                  }
//              }
//          }
//      }
//  }
// @formatter:on

}
