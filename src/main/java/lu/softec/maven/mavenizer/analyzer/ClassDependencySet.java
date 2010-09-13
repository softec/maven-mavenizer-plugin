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

import java.util.Collection;
import java.util.Iterator;

/**
 * Specialized Set to store class dependencies.
 *
 * This class is only mutable in the current package, and is protected from mutability in another package.
 */
public class ClassDependencySet extends AbstractDependencySet
{
    /**
     * Return true if the set already contains a given relation
     *
     * @param fromName name of the depending class
     * @param toName name of the dependency class
     * @return true if the set already contains a given relation
     */
    public boolean contains(String fromName, String toName)
    {
        return contains(new ClassPair(fromName, toName));
    }

    /**
     * Add a new dependency
     *
     * @param fromName name of the depending class
     * @param toName name of the dependency class
     * @return if the set has been changed
     */
    boolean add(String fromName, String toName)
    {
        return add(new ClassPair(fromName, toName));
    }

    /**
     * Remove a dependency
     *
     * @param fromName name of the depending class
     * @param toName name of the dependency class
     * @return if the set has been changed
     */
    boolean remove(String fromName, String toName)
    {
        return remove(new ClassPair(fromName, toName));
    }

    /**
     * Add a new dependency
     *
     * @param pair a {@link lu.softec.maven.mavenizer.analyzer.AbstractDependencySet.Pair} reprensenting the dependency
     * to add
     * @return if the set has been changed
     */
    boolean add(Pair pair)
    {
        if (!(pair instanceof ClassPair)) {
            throw new ClassCastException("Cannot cast " + pair.getClass() + " to " + ClassPair.class);
        }
        return super.add(pair);
    }

    /**
     * @param o element whose presence in this set is to be tested. If this is not an instance of {@link
     * lu.softec.maven.mavenizer.analyzer.AbstractDependencySet.Pair}, always return false.
     */
    public boolean contains(Object o)
    {
        return o instanceof ClassPair && super.contains(o);
    }

    public boolean containsAll(Collection c)
    {
        for (Iterator it = c.iterator(); it.hasNext();) {
            if (!(it.next() instanceof ClassPair)) {
                return false;
            }
        }
        return super.containsAll(c);
    }

    /**
     * Immutable inner Class used to store pairs of class dependencies
     */
    public static class ClassPair extends Pair
    {
        /**
         * Name of the dependent class
         */
        private final String fromName;

        /**
         * Class name of the dependency
         */
        private final String toName;

        /**
         * Construct an instance for the given dependency
         *
         * @param fromName name of the dependent class
         * @param toName class name of the dependency
         */
        public ClassPair(String fromName, String toName)
        {
            this.fromName = fromName;
            this.toName = toName;
        }

        /**
         * Return the name of the dependent class
         *
         * @return the name of the dependent class
         */
        public String getFromName()
        {
            return fromName;
        }

        /**
         * Return the class name of the dependency
         *
         * @return the class name of the dependency
         */
        public String getToName()
        {
            return toName;
        }
    }
}
