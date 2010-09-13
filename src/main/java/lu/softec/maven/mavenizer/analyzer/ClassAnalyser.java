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

/**
 * Common interface for analysing classes
 */
public interface ClassAnalyser
{
    /**
     * A new class has been found
     *
     * @param file name of the file where the class was found
     * @param name name of the class found
     * @return true if the class found was unknown to the analyser
     */
    boolean addClass(File file, String name);

    /**
     * A new dependency has been found
     *
     * @param from name of the class that has the dependency
     * @param to name of the class on which the dependency occurs
     * @return true if the dependency was unknown to the analyser
     */
    boolean addDependency(String from, String to);
}
