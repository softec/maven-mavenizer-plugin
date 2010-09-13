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

import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;

import lu.softec.maven.mavenizer.analyzer.ClassWalkListener;

/**
 * This interface is similar to the ClassWalker interface, but on a list of maven dependencies.
 */
public interface DependencyWalker
{
    /**
     * Returns the list of {@link org.apache.maven.model.Dependency} that is walked
     *
     * @return the list of {@link org.apache.maven.model.Dependency} that is walked
     */
    List getDependencies();

    /**
     * Set the list of {@link org.apache.maven.model.Dependency} to be walked
     *
     * @param dependencies the list of {@link org.apache.maven.model.Dependency}
     */
    void setDependencies(List dependencies);

    /**
     * Returns the local repository from where dependencies are read
     *
     * @return the local repository from where dependencies are read
     */
    ArtifactRepository getRepository();

    /**
     * Set the local repository from where dependencies are read
     *
     * @param repository the local repository from where dependencies are read
     */
    void setRepository(ArtifactRepository repository);

    /**
     * Returns the list of remote repositories where dependencies could be downloaded
     *
     * @return the list of remote repositories where dependencies could be downloaded
     */
    List getRemoteRepositories();

    /**
     * Set the list of remote repositories where dependencies could be downloaded
     *
     * @param remoteArtifactRepositories the list of remote repositories where dependencies could be downloaded
     */
    void setRemoteRepositories(List remoteArtifactRepositories);

    /**
     * Add a listener
     *
     * @param listener listener to be added
     */
    void addLibraryWalkListener(ClassWalkListener listener);

    /**
     * Scan the list of dependencies, processing each class.
     */
    void scan();
}
