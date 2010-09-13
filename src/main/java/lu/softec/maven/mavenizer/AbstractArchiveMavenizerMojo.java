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
package lu.softec.maven.mavenizer;

/**
 * Base class for Mavenizer mojo providing paramater specific to archive and analysis operations
 */
public abstract class AbstractArchiveMavenizerMojo extends AbstractMavenizerMojo
{
    /**
     * Includes patterns selector for library file to mavenized.
     *
     * @parameter
     */
    private String[] libsIncludes;

    /**
     * Excludes patterns selector for library file to mavenized.
     *
     * @parameter
     */
    private String[] libsExcludes;

    /**
     * Includes patterns selector for addionnal library files that should be considered for dependency analysis.
     *
     * @parameter
     */
    private String[] depsIncludes;

    /**
     * Excludes patterns selector for addionnal library files that should be considered for dependency analysis.
     *
     * @parameter
     */
    private String[] depsExcludes;

    /**
     * Return includes patterns for library file to mavenized.
     *
     * @return Includes patterns for library file to mavenized.
     */
    public String[] getLibsIncludes()
    {
        return libsIncludes;
    }

    /**
     * Return excludes patterns for library file to mavenized.
     *
     * @return Excludes patterns for library file to mavenized.
     */
    public String[] getLibsExcludes()
    {
        return libsExcludes;
    }

    /**
     * Return includes patterns for addionnal library files that should be considered for dependency analysis.
     *
     * @return Includes patterns for addionnal library files that should be considered for dependency analysis.
     */
    public String[] getDepsIncludes()
    {
        return depsIncludes;
    }

    /**
     * Return excludes patterns for addionnal library files that should be considered for dependency analysis.
     *
     * @return Excludes patterns for addionnal library files that should be considered for dependency analysis.
     */
    public String[] getDepsExcludes()
    {
        return depsExcludes;
    }
}
