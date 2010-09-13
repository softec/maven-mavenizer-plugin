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

import java.io.File;
import java.util.Iterator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;

import lu.softec.maven.mavenizer.mavenfile.MavenFile;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSet;

/**
 * Generate POM files based on the dependency analysis
 *
 * @goal fileinstall
 * @phase install
 */
public class FileInstallerMojo extends AbstractPomMavenizerMojo
{
    /**
     * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component role="org.apache.maven.artifact.installer.ArtifactInstaller"
     */
    private ArtifactInstaller installer;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Whether to update the metadata to make installed artifacts a release version.
     *
     * @parameter expression="${updateReleaseInfo}" default-value="false"
     */
    private boolean updateReleaseInfo;

    /**
     * Gets the path of the specified artifact within the local repository. Note that the returned path need not exist
     * (yet).
     *
     * @param artifact The artifact whose local repo path should be determined, must not be <code>null</code>.
     * @return The absolute path to the artifact when installed, never <code>null</code>.
     */
    private File getLocalRepoFile(Artifact artifact)
    {
        String path = localRepository.pathOf(artifact);
        return new File(localRepository.getBasedir(), path);
    }

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        MavenFileSet mavenLibs = getMavenizerConfig();

        Iterator it = mavenLibs.iterator();
        while (it.hasNext()) {
            MavenFile mvnFile = (MavenFile) it.next();
            installFile(mvnFile);
        }
    }

    public void installFile(MavenFile mvnFile) throws MojoExecutionException
    {
        Artifact artifact =
            artifactFactory
                .createArtifactWithClassifier(mvnFile.getGroupId(), mvnFile.getArtifactId(), mvnFile.getVersion(),
                    mvnFile.getPackaging(), mvnFile.getClassifier());

        if (mvnFile.getFile().equals(getLocalRepoFile(artifact))) {
            getLog().warn("Cannot install artifact. "
                + "Artifact is already in the local repository.\n\nFile in question is: " + mvnFile.getFile() + "\n");
        }

        ArtifactMetadata pomMetadata = new ProjectArtifactMetadata(artifact, getPomFile(mvnFile));
        artifact.addMetadata(pomMetadata);

        if (updateReleaseInfo) {
            artifact.setRelease(true);
        }

        try {
            installer.install(mvnFile.getFile(), artifact, localRepository);
        }
        catch (ArtifactInstallationException e) {
            throw new MojoExecutionException("Error installing artifact '" + artifact.getDependencyConflictId()
                + "': " + e.getMessage(), e);
        }
    }
}
