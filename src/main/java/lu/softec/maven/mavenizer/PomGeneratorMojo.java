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
import java.io.Writer;
import java.util.Iterator;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;

import lu.softec.maven.mavenizer.mavenfile.MavenFile;
import lu.softec.maven.mavenizer.mavenfile.MavenFileFactory;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSerializer;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSet;

/**
 * Create POM files based on the dependency analysis
 *
 * @goal mavenize
 * @phase prepare-package
 */
public class PomGeneratorMojo extends AbstractPomMavenizerMojo
{
    /**
     * Maven file factory
     *
     * @component role="lu.softec.maven.mavenizer.mavenfile.MavenFileFactory"
     */
    private MavenFileFactory mavenFileFactory;

    /**
     * Maven file serializer
     *
     * @component role="lu.softec.maven.mavenizer.mavenfile.MavenFileSerializer"
     */
    private MavenFileSerializer mavenFileSerializer;

    /**
     * @throws org.apache.maven.plugin.MojoExecutionException if the operation fails, even partially
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (!isBuilt(getPomBaseDir())) {
            getLog().info("POM files should be provided locally, skipping POM creation.");
            return;
        }

        MavenFileSet mavenLibs = getMavenizerConfig();

        if (getEarliestFileModification(getPomBaseDir()) < getMavenizerConfigFile().lastModified()) {
            getPomBaseDir().mkdirs();
            getLog().info("Creating POM files in " + getPomBaseDir().getAbsolutePath());

            Iterator it = mavenLibs.iterator();
            while (it.hasNext()) {
                MavenFile mvnFile = (MavenFile) it.next();
                File pomFile = getPomFile(mvnFile);

                if (pomFile.lastModified() < getMavenizerConfigFile().lastModified()) {
                    writePomFile(mvnFile, pomFile);
                }
            }
        } else {
            getLog().info(
                "Mavenizer configuration is not newer, skipping POM creation.");
        }
    }

    /**
     * Write the POM file corresponding to a given maven file
     *
     * @param mvnFile the maven file
     * @param file the pom file to be written to
     * @throws MojoExecutionException when any issue occurs
     */
    private void writePomFile(MavenFile mvnFile, File file) throws MojoExecutionException
    {
        Model model = mvnFile.getModel();

        model.setDescription(
            "POM file generated by maven-mavenizer-plugin for " + mvnFile.getFile().getName() + " from archive " +
                getArchiveFile().getName());
        if (getArchiveURL() != null) {
            model.setUrl(getArchiveURL().toString());
        }

        Writer writer = null;
        try {
            writer = WriterFactory.newXmlWriter(file);
            new MavenXpp3Writer().write(writer, model);
        }
        catch (IOException e) {
            throw new MojoExecutionException("Error writing " + file.getAbsolutePath() + " : " + e.getMessage(), e);
        }
        finally {
            IOUtil.close(writer);
        }
    }
}
