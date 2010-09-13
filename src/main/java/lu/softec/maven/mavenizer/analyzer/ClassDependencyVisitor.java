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
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

/**
 * Implements a FileAwareClassVisitor that agregate dependencies in a ClassAnalyser
 */
public class ClassDependencyVisitor extends RemappingClassAdapter implements FileAwareClassVisitor
{
    /**
     * Internal {@link ClassDependencyVisitor.ClassDependencyRemapper} used to discover dependencies
     */
    private static final ClassDependencyRemapper depsRemapper = new ClassDependencyRemapper();

    /**
     * Associated ClassAnalyser.
     */
    private final ClassAnalyser analyser;

    /**
     * Currently processed file.
     */
    private File currentFile;

    /**
     * Currently visited class
     */
    private String currentClass;

    /**
     * Construct an instance associated with the provided analyser.
     *
     * @param analyser the class analyser to feed
     */
    public ClassDependencyVisitor(ClassAnalyser analyser)
    {
        super(new EmptyVisitor(), depsRemapper);
        this.analyser = analyser;
        depsRemapper.setVisitor(this);
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
        // nothing to do here
    }

    /**
     * Set the currently visited class name
     *
     * @param name the currently visited class name
     */
    private void setCurrentClass(String name)
    {
        currentClass = name;
        if (currentFile != null && analyser != null) {
            analyser.addClass(currentFile, name);
            analyser.addDependency(currentClass, null);
        }
    }

    /**
     * Feed the associated analyser with a new dependency between the currently visited class and the provided
     * dependency name
     *
     * @param name the name of dependency of the current class
     */
    private void addDependency(String name)
    {
        if (analyser != null) {
            analyser.addDependency(currentClass, name);
        }
    }

    /**
     * Set the currently visited class name
     *
     * @param version Unused
     * @param access Unuser
     * @param name {@inheritDoc}
     * @param signature Unused
     * @param superName Unused
     * @param interfaces Unused
     */
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        setCurrentClass(name);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * Internal static inner class extending {@link org.objectweb.asm.commons.Remapper} used to discover dependencies
     */
    private static class ClassDependencyRemapper extends Remapper
    {
        /**
         * Associated dependency visitor
         */
        private ClassDependencyVisitor visitor;

        /**
         * Associate a visitor to this remapper
         *
         * @param visitor the visitor class to be associated
         */
        void setVisitor(ClassDependencyVisitor visitor)
        {
            this.visitor = visitor;
        }

        /**
         * Add a dependency of the currently visited class to the provided name.
         *
         * @param name The name of a dependency
         * @return Always null, to avoid any effective remapping.
         */
        public String map(String name)
        {
            // Temporary fix, waiting for ASM 3.3
            if (name.startsWith("L") && name.endsWith(";")) {
                name = name.substring(1, name.length() - 1);
            }

            visitor.addDependency(name);

            return null; // Do not remap anything !
        }
    }
}

