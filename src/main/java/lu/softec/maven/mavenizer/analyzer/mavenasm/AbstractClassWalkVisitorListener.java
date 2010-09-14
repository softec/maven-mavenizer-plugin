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
package lu.softec.maven.mavenizer.analyzer.mavenasm;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;

import lu.softec.maven.mavenizer.analyzer.AbstractClassWalkListener;
import lu.softec.maven.mavenizer.analyzer.ClassWalkerRuntimeException;

/**
 * Abstract class extending {@link lu.softec.maven.mavenizer.analyzer.AbstractClassWalkListener} and providing basic
 * functionalities to parse classes using a {@link org.objectweb.asm.ClassReader} and a {@link FileAwareClassVisitor}.
 *
 * Derived class should at least implements error of the ClassWalkListener interface for error reporting.
 *
 * WARNING! Derived class should ensure calling super for libraryWalkFileOpened and libraryWalkFileClosed overridden
 * methods or provide accurate values by overriding getCurrentFile, getClassCount and incrementClassCount.
 */
public abstract class AbstractClassWalkVisitorListener extends AbstractClassWalkListener
{
    /**
     * The class analyser visitor linked to this listener.
     */
    final FileAwareClassVisitor visitor;

    /**
     * Construct an instance linked with the provided visitor.
     *
     * @param visitor the visitor that will be used to visit classes processed by this listener.
     */
    public AbstractClassWalkVisitorListener(FileAwareClassVisitor visitor)
    {
        this.visitor = visitor;
    }

    /**
     * Implement libraryWalkProcessClass of interface ClassWalkListener to visit class using a {@link
     * org.objectweb.asm.ClassReader} and the provided {@link FileAwareClassVisitor}. Potential IOException are catched
     * and passed to the listener error method.
     *
     * Before the visit, the visitor setFile and setClassPosition methods are called with values from getCurrentFile()
     * and getClassCount(). After the visit, these methods are called again with values null and -1 respectively and
     * than incrementClassCount is called.
     *
     * @param progress the approximative percentage of progress in the current walked file. For .class file, always
     * 100%.
     * @param in an input stream to read the class bytecode. This stream SHOULD NEVER be closed.
     */
    public void libraryWalkProcessClass(int progress, InputStream in)
    {
        try {
            visitor.setFile(getCurrentFile());
            visitor.setClassPosition(getClassCount());
            new ClassReader(in).accept(visitor, ClassReader.SKIP_DEBUG ^ ClassReader.EXPAND_FRAMES);
            visitor.setFile(null);
            visitor.setClassPosition(-1);
            incrementClassCount();
        } catch (IOException e) {
            throw new ClassWalkerRuntimeException(getCurrentFile(), e);
        }
    }
}
