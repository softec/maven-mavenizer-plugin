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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.shared.jar.identification.JarIdentification;

import lu.softec.maven.mavenizer.analyzer.FileDependencySet;

/**
 * Factory for creating unique and consistant MavenFile instances
 *
 * All instances provided by this factory are held in memory until VM termination. A given {@link File} could be
 * associated with only a single {@link MavenFile}. Further request will returns the same {@link MavenFile}, and
 * inconsistant request will cause an IllegalArgumentException.
 */
public interface MavenFileFactory
{
    /**
     * Convert a {@link FileDependencySet} into a {@link MavenFileSet}.
     *
     * @param fileSet the {@link FileDependencySet} to convert
     * @return a {@link MavenFileSet} representing the provided {@link FileDependencySet}
     * @throws IOException when an I/O error occurs during access to the related files while analysing them.
     * @throws InvalidMavenCoordinatesException when a {@link MavenFile} with an invalid set of coordinate would have
     * been created
     */
    MavenFileSet getMavenFileSet(FileDependencySet fileSet) throws IOException, InvalidMavenCoordinatesException;

    /**
     * Convert a simple {@link File} into a {@link MavenFile} by analysing the file. Obviously, this could only works
     * using a JAR file as source.
     *
     * @param file the {@link File} to be mavenized
     * @return a {@link MavenFile} representing the provided {@link File}
     * @throws IOException when an I/O error occurs during access to the related files while analysing them.
     * @throws InvalidMavenCoordinatesException when a {@link MavenFile} with an invalid set of coordinate would have
     * been created
     */
    MavenFile getMavenFile(File file) throws IOException, InvalidMavenCoordinatesException;

    /**
     * Associate a {@link File} with its Maven coordinates and dependencies
     *
     * @param file the {@link File}
     * @param groupId the groupId for this file
     * @param artifactId the artifactId for this file
     * @param version the version for this file
     * @param classifier the classifier for this file. May be null.
     * @param deps the dependencies of this file.
     * @param repository local repository to look for missing artifacts
     * @param repositories list of remote repositories to download missing artifacts
     * @return a {@link MavenFile} associating the {@link File} with the above coordinates and dependencies.
     * @throws InvalidMavenCoordinatesException when a {@link MavenFile} with an invalid set of coordinate would have
     * been created
     * @throws IllegalArgumentException when a previously existing {@link MavenFile} exists and is inconsistant with the
     * current request
     */
    MavenFile getMavenFile(File file, String groupId, String artifactId, String version, String classifier,
        MavenFileSet deps, ArtifactRepository repository, List remoteRepositories)
        throws InvalidMavenCoordinatesException;

    /**
     * Provide hints to the factory for qualifying non-maven files when these are requested.
     *
     * @param info partial coordinates to be associated with either all non-qualified file or a specific file
     */
    void addMavenInfo(FileMavenInfo info);

    /**
     * Return available hint for a given name.
     *
     * @param name name of the file without extension or artifactId
     * @return the hint previously provided or null if none have been found.
     */
    FileMavenInfo getMavenInfo(String name);

    /**
     * Return computed hints for a given {@link File} or artifactId in this order, fallbacking to defaults or NOINFO as
     * appropriate.
     *
     * @param file the file that should be hinted
     * @param artifactId the artifactId the could be hinted
     * @return the hint computed or {@link lu.softec.maven.mavenizer.mavenfile.FileMavenInfo#NOINFO}
     */
    FileMavenInfo getFileMavenInfo(File file, String artifactId);

    /**
     * Return maven identification for the given library file (should be a JAR)
     *
     * @param file a jar file to analyse
     * @return the dectected coordinates for the given file
     * @throws IOException when an I/O error occurs during analysis.
     */
    JarIdentification getJarIdentification(File file) throws IOException;
}
