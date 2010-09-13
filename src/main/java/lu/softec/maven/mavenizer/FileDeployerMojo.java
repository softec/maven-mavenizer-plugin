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
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.util.StringUtils;

import lu.softec.maven.mavenizer.mavenfile.MavenFile;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSet;

/**
 * Generate POM files based on the dependency analysis
 *
 * @goal filedeploy
 * @phase deploy
 */
public class FileDeployerMojo extends AbstractPomMavenizerMojo
{
    /**
     * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
     */
    private ArtifactFactory artifactFactory;

    /**
     * Component used to create a repository.
     *
     * @component role="org.apache.maven.artifact.repository.ArtifactRepositoryFactory"
     */
    private ArtifactRepositoryFactory repositoryFactory;

    /**
     * @component role="org.apache.maven.artifact.deployer.ArtifactDeployer"
     */
    private ArtifactDeployer deployer;

    /**
     * Map that contains the layouts.
     *
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map repositoryLayouts;

    /**
     * Flag whether Maven is currently in online/offline mode.
     *
     * @parameter default-value="${settings.offline}"
     * @readonly
     */
    private boolean offline;

    /**
     * Server Id to map on the &lt;id&gt; under &lt;server&gt; section of settings.xml In most cases, this parameter
     * will be required for authentication.
     *
     * @parameter expression="${repositoryId}" default-value="remote-repository"
     * @required
     */
    private String repositoryId;

    /**
     * The type of remote repository layout to deploy to. Try <i>legacy</i> for a Maven 1.x-style repository layout.
     *
     * @parameter expression="${repositoryLayout}" default-value="default"
     * @required
     */
    private String repositoryLayout;

    /**
     * URL where the artifact will be deployed. <br/> ie ( file://C:\m2-repo or scp://host.com/path/to/repo )
     *
     * @parameter expression="${repositoryUrl}"
     * @required
     */
    private String repositoryUrl;

    /**
     * Whether to deploy snapshots with a unique version or not.
     *
     * @parameter expression="${uniqueVersion}" default-value="true"
     */
    private boolean uniqueVersion;

    /**
     * Whether to update the metadata to make installed artifacts a release version.
     *
     * @parameter expression="${updateReleaseInfo}" default-value="false"
     */
    private boolean updateReleaseInfo;

    private void failIfOffline() throws MojoFailureException
    {
        if (offline) {
            throw new MojoFailureException("Cannot deploy artifacts when Maven is in offline mode");
        }
    }

    private ArtifactRepositoryLayout getLayout(String id) throws MojoExecutionException
    {
        ArtifactRepositoryLayout layout = (ArtifactRepositoryLayout) repositoryLayouts.get(id);

        if (layout == null) {
            throw new MojoExecutionException("Invalid repository layout: " + id);
        }

        return layout;
    }

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        failIfOffline();

        MavenFileSet mavenLibs = getMavenizerConfig();

        for (Iterator it = mavenLibs.iterator(); it.hasNext();) {
            MavenFile mvnFile = (MavenFile) it.next();
            deployFile(mvnFile);
        }
    }

    public void deployFile(MavenFile mvnFile) throws MojoExecutionException, MojoFailureException
    {
        ArtifactRepositoryLayout layout = getLayout(repositoryLayout);

        ArtifactRepository deploymentRepository =
            repositoryFactory.createDeploymentArtifactRepository(repositoryId, repositoryUrl, layout, uniqueVersion);

        String protocol = deploymentRepository.getProtocol();

        if (StringUtils.isEmpty(protocol)) {
            throw new MojoExecutionException("No transfer protocol found.");
        }

        // Create the artifact
        Artifact artifact =
            artifactFactory
                .createArtifactWithClassifier(mvnFile.getGroupId(), mvnFile.getArtifactId(), mvnFile.getVersion(),
                    mvnFile.getPackaging(), mvnFile.getClassifier());

        if (mvnFile.getFile().equals(getLocalRepoFile(artifact))) {
            throw new MojoFailureException("Cannot deploy artifact from the local repository: " + mvnFile.getFile());
        }

        File pomFile = getPomFile(mvnFile);
        if (!pomFile.exists()) {
            throw new MojoExecutionException("No POM file found for: " + mvnFile.getFile());
        }

        ArtifactMetadata pomMetadata = new ProjectArtifactMetadata(artifact, pomFile);
        artifact.addMetadata(pomMetadata);

        if (updateReleaseInfo) {
            artifact.setRelease(true);
        }

        try {
            deployer.deploy(mvnFile.getFile(), artifact, deploymentRepository, getLocalRepository());
        }
        catch (ArtifactDeploymentException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
