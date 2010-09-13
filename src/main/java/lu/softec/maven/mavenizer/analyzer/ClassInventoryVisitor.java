/*
 * Copyright 2010 SOFTEC sa. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lu.softec.maven.mavenizer.analyzer;

import java.io.File;

import org.objectweb.asm.commons.EmptyVisitor;

/**
 * Implements a FileAwareClassVisitor that agregate visited class name in a ClassAnalyser
 */
public class ClassInventoryVisitor extends EmptyVisitor implements FileAwareClassVisitor
{
    /**
     * Associated ClassAnalyser.
     */
    private final ClassAnalyser analyser;

    /**
     * Currently processed file.
     */
    private File currentFile;

    /**
     * Construct an instance associated with the provided analyser.
     *
     * @param analyser the class analyser to feed
     */
    ClassInventoryVisitor(ClassAnalyser analyser)
    {
        this.analyser = analyser;
    }

    /**
     * Set the File containing the currently visited class.
     *
     * @param file the File containing the currently visited class
     */
    public void setFile(File file)
    {
        currentFile = file;
    }

    /**
     * Does nothing.
     *
     * @param pos the ordinal position of the class in the file containing the currently visited class
     */
    public void setClassPosition(int pos)
    {
        // Nothing to do here
    }

    /**
     * Feed the class analyser with the current class name and the current file name
     *
     * @param version Unused
     * @param access Unused
     * @param name {@inheritDoc}
     * @param signature Unused
     * @param superName Unused
     * @param interfaces Unused
     */
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        if (currentFile != null && analyser != null) {
            analyser.addClass(currentFile, name);
        }
    }
}
