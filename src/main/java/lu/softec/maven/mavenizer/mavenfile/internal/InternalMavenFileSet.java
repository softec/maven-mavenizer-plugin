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
package lu.softec.maven.mavenizer.mavenfile.internal;

import lu.softec.maven.mavenizer.mavenfile.MavenFile;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSet;

/**
 * Internal mutable derived class used to construct a {@link MavenFileSet} and populate it.
 */
class InternalMavenFileSet extends MavenFileSet
{
    /**
     * Add a {@link MavenFile} to the set
     *
     * @param file the {@link MavenFile} to add
     * @return true if the set has been changed
     */
    boolean add(MavenFile file)
    {
        return mavenFileSet.add(file);
    }

    /**
     * Remove a {@link MavenFile} from the set
     *
     * @param file the {@link MavenFile} to remove
     * @return true if the set has been changed
     */
    boolean remove(MavenFile file)
    {
        return mavenFileSet.remove(file);
    }

    /**
     * Clear the Set
     */
    void clean()
    {
        mavenFileSet.clear();
    }
}
