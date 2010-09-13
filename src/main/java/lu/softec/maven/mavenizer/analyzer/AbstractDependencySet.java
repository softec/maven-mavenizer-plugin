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
import java.util.Set;
import java.util.TreeSet;

/**
 * Abstract class for specialized Set to store dependencies.
 *
 * This a wrapper class over a ordered set used to store dependency relations between two indentified objects This class
 * is only mutable in the current package, and is protected from mutability from another package.
 */
public abstract class AbstractDependencySet implements Set
{
    /**
     * Wrapped TreeSet
     */
    private final Set dependencySet = new TreeSet();

    /**
     * Return true if the set already contains a given dependency
     *
     * @param pair the dependency to be checked
     * @return true if the set already contains a given dependency
     */
    public boolean contains(Pair pair)
    {
        return dependencySet.contains(pair);
    }

    /**
     * Add a new dependency
     *
     * @param pair the dependency to add
     * @return true if the set has been changed
     */
    boolean add(Pair pair)
    {
        return dependencySet.add(pair);
    }

    /**
     * Remove a dependency
     *
     * @param pair the dependency to remove
     * @return true if the set has been changed
     */
    boolean remove(Pair pair)
    {
        return dependencySet.remove(pair);
    }

    /**
     * Clear the dependency Set
     */
    void clean()
    {
        dependencySet.clear();
    }

    // Set interface implementation

    public int size()
    {
        return dependencySet.size();
    }

    public boolean isEmpty()
    {
        return dependencySet.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @param o element whose presence in this set is to be tested. If this is not an instance of {@link
     * AbstractDependencySet.Pair}, always return false.
     * @return {@inheritDoc}
     */
    public boolean contains(Object o)
    {
        return o instanceof Pair && dependencySet.contains(o);
    }

    public Iterator iterator()
    {
        return dependencySet.iterator();
    }

    public Object[] toArray()
    {
        return dependencySet.toArray();
    }

    public Object[] toArray(Object[] a)
    {
        return dependencySet.toArray(a);
    }

    /**
     * @throw UnsupportedOperationException
     */
    public boolean add(Object o)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throw UnsupportedOperationException
     */
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection c)
    {
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            if (!(iter.next() instanceof Pair)) {
                return false;
            }
        }
        return dependencySet.containsAll(c);
    }

    /**
     * @throw UnsupportedOperationException
     */
    public boolean addAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throw UnsupportedOperationException
     */
    public boolean retainAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throw UnsupportedOperationException
     */
    public boolean removeAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throw UnsupportedOperationException
     */
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object o)
    {
        return dependencySet.equals(o);
    }

    public int hashCode()
    {
        return dependencySet.hashCode();
    }

    /**
     * Immutable inner Class used to store pairs of dependencies
     */
    public static abstract class Pair implements Comparable
    {
        /**
         * Return the name of the dependent class
         *
         * @return the name of the dependent class
         */
        public abstract String getFromName();

        /**
         * Return the class name of the dependency
         *
         * @return the class name of the dependency
         */
        public abstract String getToName();

        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Pair that = (Pair) o;

            if (!getFromName().equals(that.getFromName())) {
                return false;
            }
            return getToName().equals(that.getToName());
        }

        public int hashCode()
        {
            int result = getFromName().hashCode();
            result = 31 * result + getToName().hashCode();
            return result;
        }

        /**
         * Compare this to another dependencies. Allowing a sort per dependent class name than by their dependency.
         *
         * @param o the {@link AbstractDependencySet.Pair} to be compared
         * @return the value 0 if the argument Pair is equal to this Pair; a value less than 0 if this Pair has its
         *         dependent class name lexicographically less than the Pair argument or equal to it and have its
         *         dependency class name lexicographically less than the Pair argument; and a value greater than 0 in
         *         all other cases.
         */
        public int compareTo(Object o)
        {
            Pair pair = (Pair) o;
            if (this.getFromName() == null) {
                return -1;
            }
            int c = this.getFromName().compareTo(pair.getFromName());
            if (c != 0) {
                return c;
            } else {
                if (this.getToName() == null) {
                    return -1;
                }
                return this.getToName().compareTo(pair.getToName());
            }
        }
    }
}
