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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.SelectorUtils;

import lu.softec.maven.mavenizer.analyzer.ClassAnalyser;

/**
 * Class dependency analyser
 *
 * Agregate and report dependencies found during class analysis.
 */
public final class ClassDependencyAnalyser implements ClassAnalyser
{
    /**
     * Internal Map from visited classes name to containing File
     */
    private final Map classes = new HashMap();

    /**
     * List of provided class patterns
     */
    private List providedClasses = new ArrayList();

    /**
     * Set of dependencies between classname
     */
    private final ClassDependencySet classDeps = new ClassDependencySet();

    /**
     * Set of dependencies between Files
     */
    private final FileDependencySet fileDeps = new FileDependencySet();

    /**
     * Unresolved class name dependencies
     */
    private final ClassDependencySet unresolvedDeps = new ClassDependencySet();

    private boolean resolved = true;

    public boolean addClass(File file, String name)
    {
        if (file != null && name != null && !file.equals(classes.put(name, file))) {
            resolved = false;
            return true;
        }
        return false;
    }

    public boolean addDependency(String from, String to)
    {
        if (from != null && (to == null || !matchProvidedClass(to)) && classDeps.add(from, to)) {
            resolved = false;
            return true;
        }
        return false;
    }

    /**
     * Update the {@link FileDependencySet} and the unresolved {@link ClassDependencySet} from the {@link
     * ClassDependencySet}. Does nothing if these sets are already up to date.
     */
    public void resolveFileDependency()
    {
        if (!resolved) {
            fileDeps.clean();
            unresolvedDeps.clean();
            for (Iterator it = classDeps.iterator(); it.hasNext();) {
                ClassDependencySet.ClassPair pair = (ClassDependencySet.ClassPair) it.next();
                File from = (File) classes.get(pair.getFromName());
                File to = (File) classes.get(pair.getToName());
                if (to != null) {
                    if (!from.equals(to)) {
                        fileDeps.add(from, to);
                    }
                } else {
                    if (pair.getToName() != null) {
                        unresolvedDeps.add(new ClassDependencySet.ClassPair(pair.getToName(), pair.getFromName()));
                    }
                }
            }
            resolved = true;
        }
    }

    /**
     * Check a class name against the list of provided classes
     *
     * @param className class name to check
     * @return true if class is provided
     */
    private boolean matchProvidedClass(String className)
    {
        for (Iterator it = providedClasses.iterator(); it.hasNext();) {
            String s = (String) it.next();
            if (SelectorUtils.matchPath(s, className, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The map of class discovered during dependency analysis. The key is the name of the class, and the value is the
     * {@link File} where the class is defined
     *
     * @return an unmodifiable map of class discovered during dependency analysis.
     */
    public Map getProcessedClasses()
    {
        return Collections.unmodifiableMap(classes);
    }

    /**
     * Returns the {@link ClassDependencySet} built by the analysis
     *
     * @return the {@link ClassDependencySet} built by the analysis
     */
    public ClassDependencySet getClassDependencies()
    {
        return classDeps;
    }

    /**
     * Resolve if needed and returns the {@link FileDependencySet} based on the {@link ClassDependencySet} built by the
     * analysis
     *
     * @return the {@link FileDependencySet} based on the {@link ClassDependencySet} built by the analysis
     */
    public FileDependencySet getFileDependencies()
    {
        resolveFileDependency();
        return fileDeps;
    }

    /**
     * Resolve if needed and returns the {@link ClassDependencySet} of unresolved dependencies based on the {@link
     * ClassDependencySet} built by the analysis
     *
     * @return the {@link ClassDependencySet} of unresolved dependencies based on the {@link ClassDependencySet} built
     *         by the analysis
     */
    public ClassDependencySet getUnresolvedDependencies()
    {
        resolveFileDependency();
        return unresolvedDeps;
    }

    /**
     * Add a provided class pattern
     *
     * @param providedClass a pattern of class name to be ignore during resolution
     */
    public void addProvidedClasses(String providedClass)
    {
        this.providedClasses.add(providedClass);
    }
}
