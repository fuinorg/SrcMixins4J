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

import org.emftext.language.java.classifiers.Classifier;

/**
 * Logs errors and warnings.
 */
public interface SrcMixins4JAnalyzerLog {

    /**
     * Adds a warning for the given classifier.
     * 
     * @param classifier Classifier to add the warning to.
     * @param message Warning message.
     */
    public void addWarning(Classifier classifier, String message);
    
    /**
     * Adds an error for the given classifier.
     * 
     * @param classifier Classifier to add the error to.
     * @param message Error message.
     */
    public void addError(Classifier classifier, String message);

}
