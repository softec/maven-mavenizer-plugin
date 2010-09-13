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
package lu.softec.maven.mavenizer.mavenfile;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A specialized unmodifiable Set to hold a set of {@link MavenFile}
 *
 * Constructing useful instances of this object needs an appropriate factory implementing {@link MavenFileFactory} or
 * needs a derived mutable class.
 */
public class MavenFileSet implements Set
{
    protected final Set mavenFileSet = new TreeSet();

    /**
     * Private constructor to avoid instance creation
     */
    protected MavenFileSet()
    {
    }

    /**
     * Return true if the set already contains a given dependency
     *
     * @param file the dependency to be checked
     * @return true if the set already contains a given dependency
     */
    public boolean contains(MavenFile file)
    {
        return mavenFileSet.contains(file);
    }

    // Set interface implementation

    public int size()
    {
        return mavenFileSet.size();
    }

    public boolean isEmpty()
    {
        return mavenFileSet.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @param o element whose presence in this set is to be tested. If this is not an instance of {@link MavenFile},
     * always return false.
     * @return {@inheritDoc}
     */
    public boolean contains(Object o)
    {
        return o instanceof MavenFile && mavenFileSet.contains(o);
    }

    public Iterator iterator()
    {
        return mavenFileSet.iterator();
    }

    public Object[] toArray()
    {
        return mavenFileSet.toArray();
    }

    public Object[] toArray(Object[] a)
    {
        return mavenFileSet.toArray(a);
    }

    /**
     * {@inheritDoc}
     *
     * @throw UnsupportedOperationException
     */
    public boolean add(Object o)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
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
            if (!(iter.next() instanceof MavenFile)) {
                return false;
            }
        }
        return mavenFileSet.containsAll(c);
    }

    /**
     * {@inheritDoc}
     *
     * @throw UnsupportedOperationException
     */
    public boolean addAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @throw UnsupportedOperationException
     */
    public boolean retainAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @throw UnsupportedOperationException
     */
    public boolean removeAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @throw UnsupportedOperationException
     */
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object o)
    {
        return mavenFileSet.equals(o);
    }

    public int hashCode()
    {
        return mavenFileSet.hashCode();
    }
}
