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

import java.io.File;

import lu.softec.maven.mavenizer.mavenfile.MavenFile;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSet;

/**
 * Internal mutable derived class used to construct a {@link MavenFile} and populates its dependencies during analysis
 * or deserialisation.
 */
class InternalMavenFile extends MavenFile
{
    /**
     * Constructs a new instance, populating all fields with provided values.
     *
     * @param file the file associated with the following coordinates
     * @param groupId the groupId of the file
     * @param artifactId the artifactId of the file
     * @param version the version of the file
     * @param classifier the classifier of the file
     * @param deps the dependencies of the file
     */
    InternalMavenFile(File file, String groupId, String artifactId, String version, String classifier,
        MavenFileSet deps)
    {
        this.file = file;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.dependencies = (deps != null) ? deps : new InternalMavenFileSet();
    }

    /**
     * Add a new dependency
     *
     * @param file the dependency to add
     * @return true if the dependency set has been changed
     */
    boolean addDependency(MavenFile file)
    {
        return ((InternalMavenFileSet) dependencies).add(file);
    }

    /**
     * Set the whole dependency set at once
     *
     * @param dependencies the new dependency set replacing any existing one
     */
    void setDependencies(MavenFileSet dependencies)
    {
        this.dependencies = dependencies;
    }
}
