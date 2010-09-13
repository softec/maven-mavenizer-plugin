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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;

import lu.softec.maven.mavenizer.analyzer.ClassDependencyAnalyser;
import lu.softec.maven.mavenizer.analyzer.ClassDependencySet;
import lu.softec.maven.mavenizer.analyzer.ClassWalkDependencyListener;
import lu.softec.maven.mavenizer.analyzer.ClassWalkInventoryListener;
import lu.softec.maven.mavenizer.analyzer.ClassWalker;
import lu.softec.maven.mavenizer.mavenfile.FileMavenInfo;
import lu.softec.maven.mavenizer.mavenfile.MavenFileFactory;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSerializer;

/**
 * Analyse dependencies between binaries of the project.
 *
 * @goal analyse
 * @phase generate-resources
 */
public class DependencyAnalyserMojo extends AbstractArchiveMavenizerMojo
{
    /**
     * Identification information for any provided library file.
     *
     * @parameter
     */
    private FileMavenInfo[] artifacts;

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
     * {@inheritDoc}
     *
     * @throws MojoExecutionException if the operation fails, even partially
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (getLatestFileModification(getBinariesBaseDir()) < getMavenizerConfigFile().lastModified()) {
            getLog().info("No changes in binaries detected, skipping dependency analysis.");
            return;
        }

        ClassDependencyAnalyser analyser = new ClassDependencyAnalyser();

        ClassWalker libs = getLibsWalker();
        libs.addLibraryWalkListener(new ClassWalkDependencyListener(analyser, getLog()));
        libs.scan();

        if ((getLibsExcludes() != null || getLibsIncludes() != null) &&
            (getDepsExcludes() != null || getDepsIncludes() != null))
        {
            ClassWalker deps = getDepsWalker();
            deps.addLibraryWalkListener(new ClassWalkInventoryListener(analyser, getLog()));
            deps.scan();
        }

        try {
            Writer writer = null;
            try {
                writer = WriterFactory.newXmlWriter(getMavenizerConfigFile());
                XmlSerializer serializer = new MXSerializer();
                serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
                serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n");
                serializer.setOutput(writer);
                serializer.startDocument("UTF-8", null);

                populateJarInfo();
                mavenFileSerializer.setSerializer(serializer);
                mavenFileSerializer.setBaseDir(getBinariesBaseDir());
                mavenFileSerializer
                    .SerializeMavenFileSet(mavenFileFactory.getMavenFileSet(analyser.getFileDependencies()));

                serializer.endDocument();
            } finally {
                IOUtil.close(writer);
            }
        } catch (Exception e) {
            try {
                if (getMavenizerConfigFile().isFile()) {
                    FileUtils.forceDelete(getMavenizerConfigFile());
                }
            } catch (IOException e1) {
                // Cleanup operation, ignore failure
            }
            throw new MojoExecutionException("Error while dumping the mavenizer configuration", e);
        }

        getLog().info("Unresolved results:");
        Iterator it = analyser.getUnresolvedDependencies().iterator();
        while (it.hasNext()) {
            ClassDependencySet.Pair pair = (ClassDependencySet.Pair) it.next();
            getLog().info(pair.getFromName() + " -> " + pair.getToName());
        }
    }

    /**
     * Populate mavenFileFactory with Jar information
     *
     * @throws MojoExecutionException when a duplicate information is provided
     */
    public void populateJarInfo() throws MojoExecutionException
    {
        if (this.artifacts == null) {
            return;
        }

        for (int i = 0; i < this.artifacts.length; i++) {
            if (mavenFileFactory.getMavenInfo(this.artifacts[i].getName()) == null) {
                mavenFileFactory.addMavenInfo(this.artifacts[i]);
            } else {
                throw new MojoExecutionException("Duplicate artifact information for " +
                    ((this.artifacts[i].getName() != null) ? this.artifacts[i].getName() : "default values."));
            }
        }
    }

    /**
     * Return a ClassWalker for libraries that are analysed for dependencies.
     *
     * @return a ClassWalker for libraries that are analysed for dependencies.
     */
    public ClassWalker getLibsWalker()
    {
        ClassWalker walker = new ClassWalker();
        walker.setBaseDir(getBinariesBaseDir());
        walker.addIncludes(getLibsIncludes());
        walker.addExcludes(getLibsExcludes());
        return walker;
    }

    /**
     * Return a ClassWalker for additional libraries that are inventoried for dependencies resolution during analysis.
     *
     * @return a ClassWalker for additional libraries that are inventoried for dependencies resolution during analysis.
     */
    public ClassWalker getDepsWalker()
    {
        ClassWalker walker = new ClassWalker();
        walker.setBaseDir(getBinariesBaseDir());
        walker.addIncludes(getDepsIncludes());
        walker.addExcludes(getDepsExcludes());
        return walker;
    }
}
