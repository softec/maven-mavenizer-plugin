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

import java.io.File;

import org.objectweb.asm.ClassVisitor;

/**
 * Interface extending ClassVisitor to be able to supply to the visitor, the file containing the currently visited class
 * and the ordinal position of the class in that file.
 */
public interface FileAwareClassVisitor extends ClassVisitor
{
    /**
     * Supply to the visitor, the File containing the currently visited class.
     *
     * @param file the File containing the currently visited class
     */
    void setFile(File file);

    /**
     * Supply to the visitor, the position of the class in the file containing the currently visited class. The position
     * of the first class visited is 0. The position of the second class visited in the same file is 1, and so on...
     *
     * @param pos the ordinal position of the class in the file containing the currently visited class
     */
    void setClassPosition(int pos);
}
