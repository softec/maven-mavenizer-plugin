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
package lu.softec.maven.mavenizer.analyzer.dependency;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * Specialized ordered Set to store file dependencies.
 *
 * This class is only mutable in the current package, and is protected from mutability from another package.
 */
public class FileDependencySet extends AbstractDependencySet
{
    /**
     * Return true if the set already contains a given dependency
     *
     * @param fromFile the depending file
     * @param toFile the dependency file
     * @return true if the set already contains a given dependency
     */
    public boolean contains(File fromFile, File toFile)
    {
        return contains(new FilePair(fromFile, toFile));
    }

    /**
     * Add a new dependency
     *
     * @param fromFile the depending file
     * @param toFile the dependency file
     * @return true if the set has been changed
     */
    boolean add(File fromFile, File toFile)
    {
        return add(new FilePair(fromFile, toFile));
    }

    /**
     * Remove a dependency
     *
     * @param fromFile the depending file
     * @param toFile the dependency file
     * @return true if the set has been changed
     */
    boolean remove(File fromFile, File toFile)
    {
        return remove(new FilePair(fromFile, toFile));
    }

    /**
     * Add a new dependency
     *
     * @param pair the {@link FileDependencySet.FilePair} reprensenting the dependency to add
     * @return if the set has been changed
     */
    boolean add(Pair pair)
    {
        if (!(pair instanceof FilePair)) {
            throw new ClassCastException("Cannot cast " + pair.getClass() + " to " + FilePair.class);
        }
        return super.add(pair);
    }

    /**
     * @param o element whose presence in this set is to be tested. If this is not an instance of {@link
     * FileDependencySet.FilePair}, always return false.
     */
    public boolean contains(Object o)
    {
        return o instanceof FilePair && super.contains(o);
    }

    public boolean containsAll(Collection c)
    {
        for (Iterator it = c.iterator(); it.hasNext();) {
            if (!(it.next() instanceof FilePair)) {
                return false;
            }
        }
        return super.containsAll(c);
    }

    /**
     * Immutable inner Class used to store pairs of class name dependencies
     */
    public static class FilePair extends Pair
    {
        /**
         * The dependent file
         */
        private final File fromFile;

        /**
         * The dependency file
         */
        private final File toFile;

        /**
         * Construct an instance for the given dependency
         *
         * @param fromFile the dependent file
         * @param toFile the dependency file
         */
        public FilePair(File fromFile, File toFile)
        {
            this.fromFile = fromFile;
            this.toFile = toFile;
        }

        /**
         * Return the absolute path name of the dependent class
         *
         * @return the absolute path name of the dependent class
         */
        public String getFromName()
        {
            if (fromFile == null) {
                return null;
            }
            return fromFile.getAbsolutePath();
        }

        /**
         * Return the absolute path name of the dependency
         *
         * @return the absolute path name of the dependency
         */
        public String getToName()
        {
            if (toFile == null) {
                return null;
            }
            return toFile.getAbsolutePath();
        }

        /**
         * Return the dependent file
         *
         * @return the dependent file
         */
        public File getFromFile()
        {
            return fromFile;
        }

        /**
         * Return the dependency file
         *
         * @return the dependency file
         */
        public File getToFile()
        {
            return toFile;
        }
    }
}
