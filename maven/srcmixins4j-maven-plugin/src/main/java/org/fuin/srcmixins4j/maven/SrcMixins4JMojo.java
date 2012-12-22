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
package org.fuin.srcmixins4j.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.resource.JaMoPPUtil;
import org.fuin.srcmixins4j.core.SrcMixins4JAnalyzer;
import org.fuin.srcmixins4j.core.SrcMixins4JAnalyzerFileContext;
import org.fuin.srcmixins4j.core.SrcMixins4JUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * SrcMixins4J plugin for maven.
 * 
 * @goal process-mixins
 * @phase process-sources
 */
public final class SrcMixins4JMojo extends AbstractMojo {

    private static final Logger LOG = LoggerFactory
            .getLogger(SrcMixins4JMojo.class);

    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private ArtifactRepository local;

    /**
     * @component
     */
    private ArtifactFactory factory;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {

        StaticLoggerBinder.getSingleton().setMavenLog(getLog());
        JaMoPPUtil.initialize();

        final ResourceSet resourceSet = new ResourceSetImpl();
        registerJarFiles(resourceSet);
        final List<File> files = registerSourceDirectories(resourceSet);

        final SrcMixins4JAnalyzerFileContext context = new SrcMixins4JAnalyzerFileContext(
                files, resourceSet);
        new SrcMixins4JAnalyzer().analyze(context);

        // Log warnings
        final Map<Classifier, List<String>> warnings = context.getWarnings();
        if (warnings.size() > 0) {
            final Iterator<Classifier> warnIt = warnings.keySet().iterator();
            while (warnIt.hasNext()) {
                final Classifier classifier = (Classifier) warnIt.next();
                final List<String> messages = warnings.get(classifier);
                for (final String message : messages) {
                    LOG.warn(SrcMixins4JUtils.getFullQualifiedName(classifier)
                            + ": " + message);
                }
            }
        }

        // Log errors and terminate build with error
        final Map<Classifier, List<String>> errors = context.getErrors();
        if (errors.size() > 0) {
            final Iterator<Classifier> errorIt = errors.keySet().iterator();
            while (errorIt.hasNext()) {
                final Classifier classifier = (Classifier) errorIt.next();
                final List<String> messages = errors.get(classifier);
                for (final String message : messages) {
                    LOG.error(SrcMixins4JUtils.getFullQualifiedName(classifier)
                            + ": " + message);
                }
            }
            throw new MojoExecutionException("Couldn't apply mixins - See error log for details");
        }

    }

    @SuppressWarnings("unchecked")
    private List<File> registerSourceDirectories(final ResourceSet resourceSet)
            throws MojoExecutionException {

        final List<File> files = new ArrayList<File>();

        final List<String> sourceRoots = project.getCompileSourceRoots();
        LOG.debug("SourceRoots: " + sourceRoots.size());
        for (final String sourceRoot : sourceRoots) {
            LOG.debug("Register source directory: " + sourceRoot);
            JavaClasspath.get(resourceSet).registerSourceOrClassFileFolder(
                    URI.createFileURI(sourceRoot));
            final File dir = new File(sourceRoot);
            files.addAll(SrcMixins4JUtils.findRecursiveAllJavaFiles(dir));
            try {
                SrcMixins4JUtils.loadResources(resourceSet, dir);
            } catch (final IOException ex) {
                throw new MojoExecutionException(
                        "Error retrieving on eof the canonical path", ex);
            }
        }

        return files;

    }

    @SuppressWarnings("unchecked")
    private void registerJarFiles(final ResourceSet resourceSet)
            throws MojoExecutionException {

        final File localRepositoryDir = new File(local.getBasedir());
        final List<Dependency> dependencies = project.getDependencies();
        LOG.debug("Dependencies: " + dependencies.size());
        for (final Dependency dependency : dependencies) {
            LOG.debug("Dependency: " + dependency);
            final Artifact artifact = factory.createArtifact(
                    dependency.getGroupId(), dependency.getArtifactId(),
                    dependency.getVersion(), dependency.getScope(), "jar");
            if ("compile".equals(dependency.getScope())) {
                LOG.debug("Try to find JAR file: " + dependency);
                final File file = new File(localRepositoryDir,
                        local.pathOf(artifact));
                if (file.exists()) {
                    try {
                        JavaClasspath.get(resourceSet).registerClassifierJar(
                                URI.createFileURI(file.getCanonicalPath()));
                    } catch (final IOException ex) {
                        throw new MojoExecutionException(
                                "Error retrieving canonical path: " + file, ex);
                    }
                } else {
                    LOG.debug("No JAR file found: " + dependency + " [" + file
                            + "]");
                }
            } else {
                LOG.debug("Ignored scope '" + dependency.getScope() + "': "
                        + dependency);
            }

        }

    }

}
