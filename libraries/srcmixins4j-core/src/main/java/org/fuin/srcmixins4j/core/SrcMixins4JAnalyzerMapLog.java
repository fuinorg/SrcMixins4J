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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emftext.language.java.classifiers.Classifier;

/**
 * Stores errors and warnings in a map. 
 */
public final class SrcMixins4JAnalyzerMapLog implements SrcMixins4JAnalyzerLog {

    private final Map<Classifier, List<String>> errors;

    private final Map<Classifier, List<String>> warnings;

    /**
     * Default constructor.
     */
    public SrcMixins4JAnalyzerMapLog() {
        this.errors = new HashMap<Classifier, List<String>>();
        this.warnings = new HashMap<Classifier, List<String>>();
    }

    @Override
    public final void addWarning(final Classifier classifier, final String message) {
        List<String> messages = warnings.get(classifier);
        if (messages == null) {
            messages = new ArrayList<String>();
            warnings.put(classifier, messages);
        }
        if (!messages.contains(message)) {
            messages.add(message);
        }
    }

    @Override
    public final void addError(final Classifier classifier, final String message) {
        List<String> messages = errors.get(classifier);
        if (messages == null) {
            messages = new ArrayList<String>();
            errors.put(classifier, messages);
        }
        if (!messages.contains(message)) {
            messages.add(message);
        }
    }
    
    /**
     * Returns the map of warnings.
     * 
     * @return Immutable map with classifiers that have one or more warnings attached.
     */
    public final Map<Classifier, List<String>> getWarnings() {        
        return Collections.unmodifiableMap(warnings);
    }

    /**
     * Returns the map of errors.
     * 
     * @return Immutable map with classifiers that have one or more errors attached.
     */
    public final Map<Classifier, List<String>> getErrors() {
        return Collections.unmodifiableMap(errors);
    }
    
}
