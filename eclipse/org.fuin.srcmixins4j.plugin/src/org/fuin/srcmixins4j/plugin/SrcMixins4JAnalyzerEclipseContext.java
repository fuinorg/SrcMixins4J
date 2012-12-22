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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
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
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.resource.java.JavaEProblemType;
import org.emftext.language.java.resource.java.mopp.JavaMarkerHelper;
import org.emftext.language.java.resource.java.mopp.JavaResource;
import org.fuin.srcmixins4j.annotations.MixinProvider;
import org.fuin.srcmixins4j.core.SrcMixins4JAnalyzerContext;
import org.fuin.srcmixins4j.core.SrcMixins4JUtils;

/**
 * Context used in Eclipse when analyzing source files to apply or remove mixin
 * code.
 */
public final class SrcMixins4JAnalyzerEclipseContext implements
        SrcMixins4JAnalyzerContext {

    private final IJavaProject project;

    private final ResourceSet resourceSet;

    private final BuildContext[] buildContexts;

    private int idx = -1;

    private BuildContext buildContext;

    /**
     * Constructor with mandatory data.
     * 
     * @param project
     *            Current project.
     * @param buildContexts
     *            Build context.
     * @param resourceSet
     *            Resource set.
     */
    public SrcMixins4JAnalyzerEclipseContext(final IJavaProject project,
            final BuildContext[] buildContexts, final ResourceSet resourceSet) {
        super();
        if (project == null) {
            throw new IllegalArgumentException("project == null");
        }
        if (buildContexts == null) {
            throw new IllegalArgumentException("buildContexts == null");
        }
        if (resourceSet == null) {
            throw new IllegalArgumentException("resourceSet == null");
        }
        this.project = project;
        this.buildContexts = buildContexts;
        this.resourceSet = resourceSet;
    }

    @Override
    public final ResourceSet getResourceSet() {
        return resourceSet;
    }

    @Override
    public final boolean hasNextResource() {
        if (idx < (buildContexts.length - 1)) {
            return true;
        }
        return false;
    }

    @Override
    public final Resource nextResource() {
        if (idx == (buildContexts.length - 1)) {
            throw new NoSuchElementException("There are no more resources!");
        }
        buildContext = buildContexts[++idx];
        return loadResource(buildContext.getFile());
    }

    @Override
    public final void markResourceAsChanged() {
        if (buildContext == null) {
            throw new IllegalStateException(
                    "nextResource() has not yet been called");
        }
        buildContext.recordAddedGeneratedFiles(new IFile[] { buildContext
                .getFile() });
    }

    @Override
    public final Class findMixinProvider(final Interface mixinIntf) {

        if (mixinIntf == null) {
            throw new IllegalArgumentException("mixinIntf == null");
        }

        final SearchPattern mixinImpl = SearchPattern.createPattern(
                MixinProvider.class.getName(),
                IJavaSearchConstants.ANNOTATION_TYPE,
                IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
                SearchPattern.R_EXACT_MATCH);

        final List<IType> types = findSourceTypes(mixinImpl, project);
        for (final IType type : types) {
            final ConcreteClassifier cc = mixinIntf.getConcreteClassifier(type
                    .getFullyQualifiedName());
            if (cc instanceof Class) {
                final Class clasz = (Class) cc;
                final AnnotationInstance ai = SrcMixins4JUtils
                        .getAnnotationInstance(clasz,
                                MixinProvider.class.getName());
                final Interface intf = SrcMixins4JUtils
                        .getSingleAnnotationRefElementParameter(ai,
                                Interface.class);
                if (intf == mixinIntf) {
                    return clasz;
                }
            }
        }
        return null;

    }

    @Override
    public final List<Class> findMixinUsers(final Interface mixinIntf) {

        if (mixinIntf == null) {
            throw new IllegalArgumentException("mixinIntf == null");
        }

        final String mixinIntfName = SrcMixins4JUtils
                .getFullQualifiedName(mixinIntf);

        final SearchPattern mixinUsers = SearchPattern.createPattern(
                mixinIntfName, IJavaSearchConstants.CLASS,
                IJavaSearchConstants.IMPLEMENTORS, SearchPattern.R_EXACT_MATCH);

        final List<Class> list = new ArrayList<Class>();
        final List<IType> types = findSourceTypes(mixinUsers, project);
        for (final IType type : types) {
            final ConcreteClassifier cc = mixinIntf.getConcreteClassifier(type
                    .getFullyQualifiedName());
            if (cc instanceof Class) {
                list.add((Class) cc);
            }
        }
        return list;
    }

    @Override
    public final void addWarning(final Classifier classifier, final String message) {
        ((JavaResource) classifier.eResource()).addWarning(message,
                JavaEProblemType.BUILDER_ERROR, classifier);
    }
    
    @Override
    public final void addError(final Classifier classifier, final String message) {
        ((JavaResource) classifier.eResource()).addError(message,
                JavaEProblemType.BUILDER_ERROR, classifier);
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
            throw new RuntimeException("Error trying to find source types", ex);
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

    /**
     * Loads the resource for the given file.
     * 
     * @param file
     *            File to load the resource for.
     * 
     * @return Resource from the resource set.
     */
    private Resource loadResource(final IFile file) {

        final URI uri = URI.createPlatformResourceURI(file.getFullPath()
                .toString(), true);

        Resource resource = resourceSet.getResource(uri, false);
        if (resource != null) {
            resource.unload();
            new JavaMarkerHelper().unmark(resource);
        }
        resource = resourceSet.getResource(uri, true);

        return resource;
    }

}
