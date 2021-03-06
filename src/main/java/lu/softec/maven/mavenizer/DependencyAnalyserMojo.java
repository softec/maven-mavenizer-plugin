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

import lu.softec.maven.mavenizer.analyzer.ClassDirectoryWalker;
import lu.softec.maven.mavenizer.analyzer.ClassWalker;
import lu.softec.maven.mavenizer.analyzer.ClassWalkerExecutionException;
import lu.softec.maven.mavenizer.analyzer.dependency.ClassDependencyAnalyser;
import lu.softec.maven.mavenizer.analyzer.dependency.ClassDependencySet;
import lu.softec.maven.mavenizer.analyzer.mavenasm.ClassWalkDependencyVisitorListener;
import lu.softec.maven.mavenizer.analyzer.mavenasm.ClassWalkInventoryVisitorListener;
import lu.softec.maven.mavenizer.analyzer.mavenasm.MavenDependencyWalker;
import lu.softec.maven.mavenizer.mavenfile.FileMavenInfo;
import lu.softec.maven.mavenizer.mavenfile.MavenFileFactory;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSerializer;

/**
 * Analyse dependencies between binaries of the project.
 *
 * @goal analyze
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
     * Selectors of provided classes, could be used to avoid reporting missing dependency.
     *
     * @parameter
     */
    private String[] jvmProvidedClasses;

    /**
     * Selectors of provided classes, could be used to avoid reporting missing dependency.
     *
     * @parameter
     */
    private String[] providedClasses;

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
     * Maven dependency walker
     *
     * @component role="lu.softec.maven.mavenizer.analyzer.mavenasm.MavenDependencyWalker"
     */
    private MavenDependencyWalker dependencyWalker;

    /**
     * Default pattern for the JVM provided class
     */
    private static final String[] JVM_PROVIDED_CLASS = {
        "java/**/*",
        "javax/**/*",
        "com/sun/**/*",
        "org/xml/sax/**/*",
        "org/w3c/dom/**/*",
        "org/ietf/jgss/*",
        "sunw/io/*",
        "sunw/util/*"
    };

    /**
     * @throws MojoExecutionException if the operation fails, even partially
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (!isBuilt(getMavenizerConfigFile())) {
            getLog().info("Mavenizer configuration should be provided locally, skipping analysis.");
            return;
        }

        // Check for changes and skip execution when uneeded
        if (getLatestFileModification(getBinariesBaseDir()) < getMavenizerConfigFile().lastModified()) {
            getLog().info("Binaries are not newer, skipping dependency analysis.");
            return;
        }

        // Analyse dependencies
        ClassDependencyAnalyser analyser = new ClassDependencyAnalyser();
        addJvmProvidedClass(analyser);
        addProvidedClass(analyser);

        try {
            ClassWalker libs = getLibsWalker();
            libs.addClassWalkListener(new ClassWalkDependencyVisitorListener(analyser, getLog()));
            libs.scan();

            if ((getLibsExcludes() != null || getLibsIncludes() != null) &&
                (getDepsExcludes() != null || getDepsIncludes() != null))
            {
                ClassWalker deps = getDepsWalker();
                deps.addClassWalkListener(new ClassWalkInventoryVisitorListener(analyser, getLog()));
                deps.scan();
            }

            if (getProject().getDependencies() != null && getProject().getDependencies().size() > 0) {
                dependencyWalker.setDependencies(getProject().getDependencies());
                dependencyWalker.setRepository(getLocalRepository());
                dependencyWalker.setRemoteRepositories(getProject().getRemoteArtifactRepositories());
                dependencyWalker.addClassWalkListener(new ClassWalkInventoryVisitorListener(analyser, getLog()));
                dependencyWalker.scan();
            }
        } catch (ClassWalkerExecutionException e) {
            if (e.getFile() != null) {
                throw new MojoExecutionException("Analysis failed on file " + e.getFile().getAbsolutePath(), e);
            } else if (e.getCause() != null) {
                throw new MojoExecutionException("Analysis failed: " + e.getCause().getMessage(),
                    e.getCause().getCause());
            } else {
                throw new MojoExecutionException("Analysis failed: " + e.getMessage(), e);
            }
        }

        // Write resulting configuration
        try {
            Writer writer = null;
            try {
                getMavenizerConfigFile().getParentFile().mkdirs();
                writer = WriterFactory.newXmlWriter(getMavenizerConfigFile());
                XmlSerializer serializer = new MXSerializer();
                serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
                serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n");
                serializer.setOutput(writer);
                serializer.startDocument("UTF-8", null);

                populateJarInfo();
                mavenFileSerializer.setSerializer(serializer);
                mavenFileSerializer.setBaseDir(getBinariesBaseDir());
                mavenFileSerializer.setRepository(getLocalRepository());
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

        if (analyser.getUnresolvedDependencies().size() > 0) {
            // Reports unresolved classes
            getLog().info("Unresolved classes:");
            for (Iterator it = analyser.getUnresolvedDependencies().iterator(); it.hasNext();) {
                ClassDependencySet.Pair pair = (ClassDependencySet.Pair) it.next();
                getLog().info(
                    pair.getFromName().replace('/', '.') + " (referenced by " + pair.getToName().replace('/', '.') +
                        ")");
            }
        } else {
            getLog().info("All dependencies as been properly resolved.");
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
     * Feed analyser with the list of JVM provided class selector patterns.
     *
     * @param analyser class dependency analyser to feed
     */
    public void addJvmProvidedClass(ClassDependencyAnalyser analyser)
    {
        if (jvmProvidedClasses == null) {
            for (int i = 0; i < JVM_PROVIDED_CLASS.length; i++) {
                analyser.addProvidedClasses(JVM_PROVIDED_CLASS[i]);
            }
        } else {
            for (int i = 0; i < jvmProvidedClasses.length; i++) {
                analyser.addProvidedClasses(jvmProvidedClasses[i].replace('.', '/'));
            }
        }
    }

    /**
     * Feed analyser with the list of provided class selector patterns.
     *
     * @param analyser class dependency analyser to feed
     */
    public void addProvidedClass(ClassDependencyAnalyser analyser)
    {
        if (providedClasses == null) {
            return;
        }

        for (int i = 0; i < providedClasses.length; i++) {
            analyser.addProvidedClasses(providedClasses[i].replace('.', '/'));
        }
    }

    /**
     * Return a ClassWalker for libraries that are analysed for dependencies.
     *
     * @return a ClassWalker for libraries that are analysed for dependencies.
     */
    public ClassWalker getLibsWalker()
    {
        ClassDirectoryWalker walker = new ClassDirectoryWalker();
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
        ClassDirectoryWalker walker = new ClassDirectoryWalker();
        walker.setBaseDir(getBinariesBaseDir());
        walker.addIncludes(getDepsIncludes());
        walker.addExcludes(getDepsExcludes());
        return walker;
    }
}
