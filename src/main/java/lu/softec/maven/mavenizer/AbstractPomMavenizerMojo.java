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
import java.io.IOException;
import java.io.Reader;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import lu.softec.maven.mavenizer.mavenfile.InvalidMavenCoordinatesException;
import lu.softec.maven.mavenizer.mavenfile.MavenFile;
import lu.softec.maven.mavenizer.mavenfile.MavenFileParser;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSet;

/**
 * Base class for Mavenizer mojo providing paramater specific to pom operations
 */
public abstract class AbstractPomMavenizerMojo extends AbstractMavenizerMojo
{
    /**
     * Base directory where the POM of the project to be mavenized are stored.
     *
     * @parameter expression="${project.build.directory}/pom"
     * @required
     */
    private File pomBaseDir;

    /**
     * Returns the base directory where the POM of the project to be mavenized are stored.
     *
     * @return the base directory where the POM of the project to be mavenized are stored.
     */
    public File getPomBaseDir()
    {
        return pomBaseDir;
    }

    /**
     * Maven file parser
     *
     * @component role="lu.softec.maven.mavenizer.mavenfile.MavenFileParser"
     */
    private MavenFileParser mavenFileParser;

    /**
     * Parse the mavenizer configuration into a {@link MavenFileSet}
     *
     * @return the mavenizer configuration
     * @throws MojoExecutionException when an issue occurs during parsing
     */
    protected MavenFileSet getMavenizerConfig() throws MojoExecutionException
    {
        MavenFileSet mavenLibs;

        Reader reader = null;
        try {
            reader = ReaderFactory.newXmlReader(getMavenizerConfigFile());
            XmlPullParser xpp = new MXParser();
            xpp.setInput(reader);
            xpp.nextTag();
            mavenFileParser.setXmlPullParser(xpp);
            mavenFileParser.setBaseDir(getBinariesBaseDir());
            mavenFileParser.setRepository(getLocalRepository());
            mavenFileParser.setRemoteRepositories(getProject().getRemoteArtifactRepositories());
            mavenLibs = mavenFileParser.getMavenFileSet();
        } catch (IOException e) {
            throw new MojoExecutionException("Error while reading mavenizer configuration", e);
        } catch (XmlPullParserException e) {
            throw new MojoExecutionException("Error while parsing mavenizer configuration", e);
        } catch (InvalidMavenCoordinatesException e) {
            throw new MojoExecutionException("Invalid maven coordinates detected in mavenizer configuration", e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Missing artifact detected in mavenizer configuration", e);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Unresolvable artifact detected in mavenizer configuration", e);
        } finally {
            IOUtil.close(reader);
        }

        return mavenLibs;
    }

    /**
     * Get the POM file from the pom base dir corresponding to a given maven file from the pom base dir
     *
     * @param mvnFile the maven file
     * @return a File that may exist or not
     */
    protected File getPomFile(MavenFile mvnFile)
    {
        return (new File(getPomBaseDir(), FileUtils.removeExtension(mvnFile.getFile().getName()) + ".pom"));
    }

    /**
     * Gets the path of the specified artifact within the local repository. Note that the returned path need not exist
     * (yet).
     *
     * @param artifact The artifact whose local repo path should be determined, must not be <code>null</code>.
     * @return The absolute path to the artifact when installed, never <code>null</code>.
     */
    protected File getLocalRepoFile(Artifact artifact)
    {
        String path = getLocalRepository().pathOf(artifact);
        return new File(getLocalRepository().getBasedir(), path);
    }
}
