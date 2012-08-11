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

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.emftext.language.java.annotations.AnnotationInstance;
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.resource.java.JavaEProblemType;
import org.emftext.language.java.resource.java.mopp.JavaResource;
import org.fuin.srcmixins4j.annotations.MixinIntf;
import org.fuin.srcmixins4j.annotations.MixinProvider;

/**
 * Analyzes resources and applies mixin code.
 */
public final class SrcMixins4JAnalyzer {

    /**
     * Analyze all resources in the given context.
     * 
     * @param context
     *            Context to analyze.
     */
    public final void analyze(final SrcMixins4JAnalyzerContext context) {
        while (context.hasNextResource()) {
            final Resource resource = context.nextResource();
            if (resource == null) {
                // Interface contract violation
                throw new IllegalStateException("resource == null");
            }
            analyze(context, resource);
        }
    }

    /**
     * Analyze the given resource.
     * 
     * @param context
     *            Context to analyze.
     * @param resource
     *            Resource to analyze.
     */
    public final void analyze(final SrcMixins4JAnalyzerContext context,
            final Resource resource) {

        final CompilationUnit cu = SrcMixins4JUtils
                .getCompilationUnit(resource);
        if (cu == null) {
            return;
        }

        final EList<ConcreteClassifier> classifiers = cu.getClassifiers();
        for (final ConcreteClassifier classifier : classifiers) {
            if (classifier instanceof Class) {
                final Class clasz = (Class) classifier;
                final AnnotationInstance mixinProviderAnnotation = SrcMixins4JUtils
                        .getAnnotationInstance(clasz,
                                MixinProvider.class.getName());
                if (mixinProviderAnnotation == null) {
                    final List<Interface> mixinInterfaces = SrcMixins4JUtils
                            .getMixinInterfaces(clasz);
                    if (mixinInterfaces.size() == 0) {
                        handleStandardClassChanged(context, resource, clasz);
                    } else {
                        handleMixinUserChanged(context, resource, clasz,
                                mixinInterfaces);
                    }
                } else {
                    handleMixinProviderChanged(context, resource, clasz,
                            mixinProviderAnnotation);
                }
            } else if (classifier instanceof Interface) {
                final Interface intf = (Interface) classifier;
                if (SrcMixins4JUtils.getAnnotationInstance(intf,
                        MixinIntf.class.getName()) != null) {
                    handleMixinIntfChanged(context, resource, intf);
                }
            }
        }

    }

    private void handleMixinIntfChanged(
            final SrcMixins4JAnalyzerContext context, final Resource resource,
            final Interface intf) {

        System.out.println(intf.getName() + " is a mixin interface");
    }

    private void handleMixinProviderChanged(
            final SrcMixins4JAnalyzerContext context, final Resource resource,
            final Class clasz, final AnnotationInstance ai) {

        System.out.println(clasz.getName() + " is a mixin provider");

        final Interface mixinIntf = SrcMixins4JUtils.getMixinInterface(clasz);
        if (mixinIntf == null) {
            // Should never happen because @MixinProvider has no default
            // parameter value
            ((JavaResource) clasz.eResource()).addWarning(
                    "No mixin interface class found in mixin provider",
                    JavaEProblemType.BUILDER_ERROR, clasz);
            return;
        }
        final List<Class> mixinUsers = context.findMixinUsers(mixinIntf);
        for (final Class mixinUser : mixinUsers) {
            System.out.println("    " + mixinUser.getName() + " uses mixin "
                    + mixinIntf.getName());
            final List<Interface> mixinInterfaces = SrcMixins4JUtils
                    .getMixinInterfaces(mixinUser);
            handleMixinUserChanged(context, mixinUser.eResource(), mixinUser,
                    mixinInterfaces);
        }

    }

    private void handleStandardClassChanged(
            final SrcMixins4JAnalyzerContext context, final Resource resource,
            final Class clasz) {

        System.out.println(clasz.getName() + " is a standard class");

        SrcMixins4JUtils.removeAllMixinMembers(clasz);
        save(context, resource);

    }

    private void handleMixinUserChanged(
            final SrcMixins4JAnalyzerContext context, final Resource resource,
            final Class clasz, final List<Interface> mixinIntfs) {

        System.out.println(clasz.getName() + " is a mixin user");

        SrcMixins4JUtils.removeAllMixinMembers(clasz);

        for (Interface mixinIntf : mixinIntfs) {
            System.out.print("    " + mixinIntf.getName());
            final Class provider = context.findMixinProvider(mixinIntf);
            if (provider != null) {
                System.out.println(" implemented by " + provider.getName());
                SrcMixins4JUtils.applyMixin(clasz, provider, mixinIntf);
            }
        }

        save(context, resource);

    }

    private void save(final SrcMixins4JAnalyzerContext context,
            final Resource resource) {
        try {
            resource.save(null);
            context.markResourceAsChanged();
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
