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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.emftext.language.java.annotations.AnnotationInstance;
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.resource.java.JavaEProblemType;
import org.emftext.language.java.resource.java.mopp.JavaResource;
import org.fuin.srcmixins4j.annotations.MixinIntf;
import org.fuin.srcmixins4j.annotations.MixinProvider;
import org.fuin.srcmixins4j.core.SrcMixin4JUtils;

/**
 * Analyzes resources and applies mixin code.
 */
public final class SrcMixin4JAnalysis {

    /**
     * Analyze the given resource.
     *
     * @param javaProject
     *            Current project.
     * @param buildContext
     *            Build context.
     * @param resourceSet
     *            Current resource set.
     * @param resource
     *            Resource to analyze.
     */
    public final void analyze(final IJavaProject javaProject,
            final BuildContext buildContext, final ResourceSet resourceSet,
            final Resource resource) {

        final CompilationUnit cu = getCompilationUnit(javaProject, resourceSet,
                resource);
        if (cu == null) {
            return;
        }

        final EList<ConcreteClassifier> classifiers = cu.getClassifiers();
        for (final ConcreteClassifier classifier : classifiers) {
            if (classifier instanceof Class) {
                final Class clasz = (Class) classifier;
                final AnnotationInstance mixinProviderAnnotation = SrcMixin4JUtils
                        .getAnnotationInstance(clasz,
                                MixinProvider.class.getName());
                if (mixinProviderAnnotation == null) {
                    final List<Interface> mixinInterfaces = SrcMixin4JUtils
                            .getMixinInterfaces(clasz);
                    if (mixinInterfaces.size() == 0) {
                        handleStandardClassChanged(javaProject, buildContext,
                                resource, clasz);
                    } else {
                        handleMixinUserChanged(javaProject, buildContext,
                                resource, clasz, mixinInterfaces);
                    }
                } else {
                    handleMixinProviderChanged(javaProject, buildContext,
                            resource, clasz, mixinProviderAnnotation);
                }
            } else if (classifier instanceof Interface) {
                final Interface intf = (Interface) classifier;
                if (SrcMixin4JUtils.getAnnotationInstance(intf,
                        MixinIntf.class.getName()) != null) {
                    handleMixinIntfChanged(javaProject, buildContext, resource,
                            intf);
                }
            }
        }

    }

    private CompilationUnit getCompilationUnit(final IJavaProject javaProject,
            final ResourceSet resourceSet, final Resource resource) {
        if ((javaProject == null) || (resourceSet == null)
                || (resource == null)) {
            return null;
        }
        if (resource.getContents().size() != 1) {
            return null;
        }
        final EObject eObj = resource.getContents().get(0);
        if (!(eObj instanceof CompilationUnit)) {
            return null;
        }
        return (CompilationUnit) eObj;
    }

    private void handleMixinIntfChanged(final IJavaProject javaProject,
            final BuildContext buildContext, final Resource resource,
            final Interface intf) {

        System.out.println(intf.getName() + " is a mixin interface");
    }

    private void handleMixinProviderChanged(final IJavaProject project,
            final BuildContext buildContext, final Resource resource,
            final Class clasz, final AnnotationInstance ai) {

        System.out.println(clasz.getName() + " is a mixin provider");

        final Interface mixinIntf = SrcMixin4JUtils.getMixinInterface(clasz);
        if (mixinIntf == null) {
            // Should never happen because @MixinProvider has no default
            // parameter value
            ((JavaResource) clasz.eResource()).addWarning(
                    "No mixin interface class found in mixin provider",
                    JavaEProblemType.BUILDER_ERROR, clasz);
            return;
        }
        final List<Class> mixinUsers = findMixinUsers(mixinIntf, project);
        for (final Class mixinUser : mixinUsers) {
            System.out.println("    " + mixinUser.getName() + " uses mixin "
                    + mixinIntf.getName());
            final List<Interface> mixinInterfaces = SrcMixin4JUtils
                    .getMixinInterfaces(mixinUser);
            handleMixinUserChanged(project, buildContext,
                    mixinUser.eResource(), mixinUser, mixinInterfaces);
        }

    }

    private void handleStandardClassChanged(final IJavaProject javaProject,
            final BuildContext buildContext, final Resource resource,
            final Class clasz) {

        System.out.println(clasz.getName() + " is a standard class");

        SrcMixin4JUtils.removeAllMixinMembers(clasz);
        save(buildContext, resource);

    }

    private void handleMixinUserChanged(final IJavaProject javaProject,
            final BuildContext buildContext, final Resource resource,
            final Class clasz, final List<Interface> mixinIntfs) {

        System.out.println(clasz.getName() + " is a mixin user");

        SrcMixin4JUtils.removeAllMixinMembers(clasz);

        for (Interface mixinIntf : mixinIntfs) {
            System.out.print("    " + mixinIntf.getName());
            final Class provider = findMixinProvider(mixinIntf, javaProject);
            if (provider != null) {
                System.out.println(" implemented by " + provider.getName());
                SrcMixin4JUtils.applyMixin(clasz, provider, mixinIntf);
            }
        }

        save(buildContext, resource);

    }

    private void save(final BuildContext buildContext, final Resource resource) {
        try {
            resource.save(null);
            buildContext.recordAddedGeneratedFiles(new IFile[] { buildContext
                    .getFile() });
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Class findMixinProvider(final Interface mixinIntf,
            final IJavaElement... elements) {

        final SearchPattern mixinImpl = SearchPattern.createPattern(
                MixinProvider.class.getName(),
                IJavaSearchConstants.ANNOTATION_TYPE,
                IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
                SearchPattern.R_EXACT_MATCH);

        final List<IType> types = findSourceTypes(mixinImpl, elements);
        for (final IType type : types) {
            final ConcreteClassifier cc = mixinIntf.getConcreteClassifier(type
                    .getFullyQualifiedName());
            if (cc instanceof Class) {
                final Class clasz = (Class) cc;
                final AnnotationInstance ai = SrcMixin4JUtils
                        .getAnnotationInstance(clasz,
                                MixinProvider.class.getName());
                final Interface intf = SrcMixin4JUtils
                        .getSingleAnnotationInterfaceParameter(ai);
                if (intf == mixinIntf) {
                    return clasz;
                }
            }
        }
        return null;

    }

    private static List<IType> findMixinIntfs(final IJavaElement... elements) {

        final SearchPattern mixinIntf = SearchPattern.createPattern(
                MixinIntf.class.getName(),
                IJavaSearchConstants.ANNOTATION_TYPE,
                IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
                SearchPattern.R_EXACT_MATCH);

        return findSourceTypes(mixinIntf, elements);

    }

    private static List<Class> findMixinProviders(final CompilationUnit cu,
            final IJavaElement... elements) {

        final SearchPattern mixinImpl = SearchPattern.createPattern(
                MixinProvider.class.getName(),
                IJavaSearchConstants.ANNOTATION_TYPE,
                IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
                SearchPattern.R_EXACT_MATCH);

        final List<Class> list = new ArrayList<Class>();
        final List<IType> types = findSourceTypes(mixinImpl, elements);
        for (final IType type : types) {
            final ConcreteClassifier cc = cu.getConcreteClassifier(type
                    .getFullyQualifiedName());
            if (cc instanceof Class) {
                list.add((Class) cc);
            }
        }
        return list;

    }

    private static List<Class> findMixinUsers(final Interface mixinIntf,
            final IJavaElement... elements) {

        if (mixinIntf == null) {
            return new ArrayList<Class>();
        }

        final String mixinIntfName = SrcMixin4JUtils
                .getFullQualifiedName(mixinIntf);

        final SearchPattern mixinUsers = SearchPattern.createPattern(
                mixinIntfName, IJavaSearchConstants.CLASS,
                IJavaSearchConstants.IMPLEMENTORS, SearchPattern.R_EXACT_MATCH);

        final List<Class> list = new ArrayList<Class>();
        final List<IType> types = findSourceTypes(mixinUsers, elements);
        for (final IType type : types) {
            final ConcreteClassifier cc = mixinIntf.getConcreteClassifier(type
                    .getFullyQualifiedName());
            if (cc instanceof Class) {
                list.add((Class) cc);
            }
        }
        return list;
    }

    /**
     * Locates all source types of a given pattern.
     *
     * @param searchPattern
     *            Pattern to use.
     * @param elements
     *            Elements to search.
     *
     * @return List of matching types.
     */
    private static List<IType> findSourceTypes(
            final SearchPattern searchPattern, final IJavaElement... elements) {
        final List<IType> resultList = new ArrayList<IType>();
        final SearchRequestor requestor = createRequestor(resultList);
        try {
            new SearchEngine().search(searchPattern,
                    new SearchParticipant[] { SearchEngine
                            .getDefaultSearchParticipant() }, SearchEngine
                            .createJavaSearchScope(elements,
                                    IJavaSearchScope.SOURCES), requestor, null);
        } catch (final CoreException ex) {
            // TODO Handle exceptions
            ex.printStackTrace();
        }
        return resultList;
    }

    /**
     * Returns an object that stores the found types in a given list.
     *
     * @param resultList
     *            List to store results.
     *
     * @return Search requestor.
     */
    private static SearchRequestor createRequestor(final List<IType> resultList) {
        return new SearchRequestor() {
            @Override
            public void acceptSearchMatch(final SearchMatch match)
                    throws CoreException {
                final Object element = match.getElement();
                if (element instanceof IType) {
                    resultList.add((IType) element);
                }
            }
        };
    }

}
