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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emftext.language.java.annotations.AnnotationInstance;
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.Interface;
import org.fuin.srcmixins4j.annotations.MixinProvider;

/**
 * File and resource set based context used when analyzing source files to apply
 * or remove mixin code.
 */
public final class SrcMixins4JAnalyzerFileContext implements
        SrcMixins4JAnalyzerContext {

    private final ResourceSet resourceSet;

    private final List<File> filesToInspect;

    private final List<File> updated;

    private final List<Class> mixinProviders;

    private final SrcMixins4JAnalyzerMapLog analyzerLog;

    private int idx = -1;

    private File currentFile;

    /**
     * Constructor with mandatory data.
     * 
     * @param filesToInspect
     *            Files to analyze.
     * @param resourceSet
     *            Resource set.
     */
    public SrcMixins4JAnalyzerFileContext(final List<File> filesToInspect,
            final ResourceSet resourceSet) {
        super();
        if (filesToInspect == null) {
            throw new IllegalArgumentException("filesToInspect == null");
        }
        if (resourceSet == null) {
            throw new IllegalArgumentException("resourceSet == null");
        }
        this.filesToInspect = filesToInspect;
        this.resourceSet = resourceSet;
        this.updated = new ArrayList<File>();
        this.analyzerLog = new SrcMixins4JAnalyzerMapLog();
        this.mixinProviders = SrcMixins4JUtils.findMixinProviders(resourceSet);
    }

    @Override
    public final ResourceSet getResourceSet() {
        return resourceSet;
    }

    @Override
    public final boolean hasNextResource() {
        if (idx < (filesToInspect.size() - 1)) {
            return true;
        }
        return false;
    }

    @Override
    public final Resource nextResource() {
        if (idx == (filesToInspect.size() - 1)) {
            throw new NoSuchElementException("There are no more resources!");
        }
        currentFile = filesToInspect.get(++idx);
        return loadResource(currentFile);
    }

    @Override
    public final void markResourceAsChanged() {
        if (currentFile == null) {
            throw new IllegalStateException(
                    "nextResource() has not yet been called");
        }
        updated.add(currentFile);
    }

    @Override
    public final Class findMixinProvider(final Interface mixinIntf) {
        for (final Class mixinProvider : mixinProviders) {
            final AnnotationInstance ai = SrcMixins4JUtils
                    .getAnnotationInstance(mixinProvider,
                            MixinProvider.class.getName());
            final Interface intf = SrcMixins4JUtils
                    .getSingleAnnotationRefElementParameter(ai, Interface.class);
            if (intf == mixinIntf) {
                return mixinProvider;
            }
        }
        return null;
    }

    @Override
    public final List<Class> findMixinUsers(final Interface mixinIntf) {
        return SrcMixins4JUtils.findImplementors(resourceSet, mixinIntf);
    }

    @Override
    public final void addWarning(final Classifier classifier,
            final String message) {
        analyzerLog.addWarning(classifier, message);
    }

    @Override
    public final void addError(final Classifier classifier, final String message) {
        analyzerLog.addError(classifier, message);
    }

    /**
     * Returns the map of warnings.
     * 
     * @return Map with classifiers that have one or more warnings attached.
     */
    public final Map<Classifier, List<String>> getWarnings() {
        return analyzerLog.getWarnings();
    }

    /**
     * Returns the map of errors.
     * 
     * @return Map with classifiers that have one or more errors attached.
     */
    public final Map<Classifier, List<String>> getErrors() {
        return analyzerLog.getErrors();
    }

    /**
     * Returns the resource for the given file.
     * 
     * @param file
     *            File to return a resource for.
     * 
     * @return Resource.
     */
    private Resource loadResource(final File file) {
        try {
            final URI uri = URI.createFileURI(file.getCanonicalPath());
            return resourceSet.getResource(uri, true);
        } catch (final IOException ex) {
            throw new RuntimeException("Error reading: " + file, ex);
        }
    }

}
