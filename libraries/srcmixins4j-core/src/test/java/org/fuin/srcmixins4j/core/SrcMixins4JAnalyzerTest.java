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

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.Test;

/**
 * Test for {@link SrcMixins4JAnalyzer} class.
 */
// CHECKSTYLE:OFF
public final class SrcMixins4JAnalyzerTest extends AbstractSrcMixins4JTest {

    private static File RES_DIR = new File("src/test/resources");

    private static File TEST_INTF = new File(RES_DIR, "TestIntf.java");

    private static File TEST_MIXIN_INTF = new File(RES_DIR,
            "TestMixinIntf.java");

    private static File TEST_MIXIN_PROVIDER = new File(RES_DIR,
            "TestMixinProvider.java");

    private static File TEST_MIXIN_USER3 = new File(RES_DIR,
            "TestMixinUser3.java");

    private static File TEST_MIXIN_USER3_AFTER = new File(RES_DIR,
            "TestMixinUser3_AFTER.java");

    @Test
    public void testAnalyze() throws IOException {

        // PREPARE
        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        final File tmpSrcDir = new File(tmpDir, this.getClass().getSimpleName()
                + ".src");

        // Copy files into the temporary source directory
        final File intfFile = new File(tmpSrcDir, TEST_INTF.getName());
        FileUtils.copyFile(TEST_INTF, intfFile);
        final File mixinIntfFile = new File(tmpSrcDir,
                TEST_MIXIN_INTF.getName());
        FileUtils.copyFile(TEST_MIXIN_INTF, mixinIntfFile);
        final File mixinProviderFile = new File(tmpSrcDir,
                TEST_MIXIN_PROVIDER.getName());
        FileUtils.copyFile(TEST_MIXIN_PROVIDER, mixinProviderFile);
        final File mixinUserFile = new File(tmpSrcDir,
                TEST_MIXIN_USER3.getName());
        FileUtils.copyFile(TEST_MIXIN_USER3, mixinUserFile);

        // Add user file to the list
        final List<File> filesToInspect = new ArrayList<File>();
        filesToInspect.add(mixinUserFile);

        // Create context
        final ResourceSet resourceSet = createResourceSet(tmpSrcDir);
        SrcMixins4JUtils.loadResources(resourceSet, tmpSrcDir);
        final SrcMixins4JAnalyzerFileContext context = new SrcMixins4JAnalyzerFileContext(
                filesToInspect, resourceSet);

        // TEST
        new SrcMixins4JAnalyzer().analyze(context);

        // VERIFY
        assertThat(mixinUserFile).hasSameContentAs(TEST_MIXIN_USER3_AFTER);

    }

}
// CHECKSTYLE:ON
