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
import java.util.List;

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
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.resource.JaMoPPUtil;
import org.fuin.srcmixins4j.core.SrcMixins4JUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * SrcMixins4J plugin for maven.
 * 
 * @goal process-template
 * @phase generate-sources
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
        registerSourceDirectories(resourceSet);

        try {
            final List<Class> mixinUsers = SrcMixins4JUtils.applyMixins(resourceSet);
            
        } catch (final IOException ex) {
            throw new MojoExecutionException("Error applying mixin", ex);
        }

    }

    @SuppressWarnings("unchecked")
    private void registerSourceDirectories(final ResourceSet resourceSet)
            throws MojoExecutionException {

        final List<String> sourceRoots = project.getCompileSourceRoots();
        LOG.debug("SourceRoots: " + sourceRoots.size());
        for (final String sourceRoot : sourceRoots) {
            registerSrcFolder(resourceSet, sourceRoot);
        }

    }

    private void registerSrcFolder(final ResourceSet resourceSet,
            final String sourceRoot) throws MojoExecutionException {

        LOG.debug("Register source directory: " + sourceRoot);

        JavaClasspath.get(resourceSet).registerSourceOrClassFileFolder(
                URI.createFileURI(sourceRoot));
        try {
            SrcMixins4JUtils.loadResources(resourceSet, new File(sourceRoot));
        } catch (final IOException ex) {
            throw new MojoExecutionException(
                    "Error registering source directory: " + sourceRoot, ex);
        }

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
                    registerJarFile(resourceSet, file);
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

    private void registerJarFile(final ResourceSet resourceSet, final File file)
            throws MojoExecutionException {
        LOG.info("Register JAR file: " + file);
        try {
            JavaClasspath.get(resourceSet).registerClassifierJar(
                    URI.createFileURI(file.getCanonicalPath()));
        } catch (final IOException ex) {
            throw new MojoExecutionException("Error registering JAR: " + file,
                    ex);
        }
    }

}
