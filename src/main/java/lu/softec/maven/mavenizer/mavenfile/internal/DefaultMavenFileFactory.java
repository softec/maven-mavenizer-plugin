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
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.project.validation.ModelValidationResult;
import org.apache.maven.project.validation.ModelValidator;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.identification.JarIdentification;
import org.apache.maven.shared.jar.identification.JarIdentificationAnalysis;
import org.codehaus.plexus.util.StringUtils;

import lu.softec.maven.mavenizer.analyzer.FileDependencySet;
import lu.softec.maven.mavenizer.mavenfile.FileMavenInfo;
import lu.softec.maven.mavenizer.mavenfile.InvalidMavenCoordinatesException;
import lu.softec.maven.mavenizer.mavenfile.MavenFile;
import lu.softec.maven.mavenizer.mavenfile.MavenFileFactory;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSet;

/**
 * Default implementation of the {@link MavenFileFactory} interface.
 *
 * This implementation is a singleton and keep hard references to all created instances of {@link MavenFile}. It ensure
 * only one {@link MavenFile} instance is created for a given {@link File}, and that any subsequent request matches the
 * initially created instance.
 */
public class DefaultMavenFileFactory implements MavenFileFactory
{
    /**
     * ModelValidator populated by Plexus
     */
    private ModelValidator modelValidator;

    /**
     * JarIdentificationAnalysis populated by Plexus
     */
    private JarIdentificationAnalysis jarIdentificationAnalysis;

    /**
     * Hints for creating new instances
     */
    private final Map fileMavenInfo = new HashMap();

    /**
     * Map of all existing instances of {@link MavenFile} constructed by this factory
     */
    private static Map mavenFiles = new HashMap();

    public void addMavenInfo(FileMavenInfo info)
    {
        fileMavenInfo.put(info.getName(), info);
    }

    public FileMavenInfo getMavenInfo(String name)
    {
        return (FileMavenInfo) fileMavenInfo.get(name);
    }

    public MavenFileSet getMavenFileSet(FileDependencySet fileSet) throws IOException, InvalidMavenCoordinatesException
    {
        InternalMavenFileSet mavenFiles = new InternalMavenFileSet();

        Iterator it = fileSet.iterator();
        while (it.hasNext()) {
            FileDependencySet.FilePair pair = (FileDependencySet.FilePair) it.next();

            InternalMavenFile from = (InternalMavenFile) getMavenFile(pair.getFromFile());
            MavenFile to = getMavenFile(pair.getToFile());

            mavenFiles.add(from);
            from.addDependency(to);
        }

        return mavenFiles;
    }

    public MavenFile getMavenFile(File file) throws IOException, InvalidMavenCoordinatesException
    {
        if (mavenFiles.containsKey(file)) {
            return (MavenFile) mavenFiles.get(file);
        }

        JarIdentification id = getJarIdentification(file);
        FileMavenInfo info = getFileMavenInfo(file, id.getArtifactId());

        return getMavenFile(
            file,
            (info.getGroupId() != null) ? info.getGroupId() :
                getCommonPrefix(id.getGroupId(), id.getPotentialGroupIds()),
            (info.getArtifactId() != null) ? info.getArtifactId() : id.getArtifactId(),
            (info.getVersion() != null) ? info.getVersion() : id.getVersion(),
            (info.getClassifier() != null) ? info.getClassifier() : null,
            null
        );
    }

    public MavenFile getMavenFile(File file, String groupId, String artifactId, String version, String classifier,
        MavenFileSet deps) throws InvalidMavenCoordinatesException
    {
        if (mavenFiles.containsKey(file)) {
            InternalMavenFile mvnFile = (InternalMavenFile) mavenFiles.get(file);
            if (!StringUtils.equals(mvnFile.getGroupId(), groupId)
                || !StringUtils.equals(mvnFile.getArtifactId(), artifactId)
                || !StringUtils.equals(mvnFile.getVersion(), version)
                || !StringUtils.equals(mvnFile.getClassifier(), classifier)
                || (deps != null && mvnFile.getDependencies().size() != 0 && !deps.equals(mvnFile.getDependencies())))
            {
                throw new IllegalArgumentException(
                    "The instance requested mismatch with a previously existing instance for file " +
                        file.getAbsolutePath());
            }

            if (deps != null && mvnFile.getDependencies().size() == 0) {
                mvnFile.setDependencies(deps);
            }

            return mvnFile;
        }

        MavenFile mvnFile = new InternalMavenFile(file, groupId, artifactId, version, classifier, deps);

        validateArtifactInformation(mvnFile);

        mavenFiles.put(file, mvnFile);
        return mvnFile;
    }

    /**
     * Validate coordinates of a {@link MavenFile} using the {@link ModelValidator}
     *
     * @param file {@link MavenFile} to be validated
     * @throws InvalidMavenCoordinatesException when validation fails
     */
    private void validateArtifactInformation(MavenFile file) throws InvalidMavenCoordinatesException
    {
        Model model = file.getMinimalModel();

        ModelValidationResult result = modelValidator.validate(model);

        if (result.getMessageCount() > 0) {
            throw new InvalidMavenCoordinatesException(
                "Coordinates are incomplete or not valid:\n" + result.render("  "));
        }
    }

    public JarIdentification getJarIdentification(File file) throws IOException
    {
        JarAnalyzer jar = new JarAnalyzer(file);
        return (jarIdentificationAnalysis.analyze(jar));
    }

    public FileMavenInfo getFileMavenInfo(File file, String artifactId)
    {
        // Get file specific info
        FileMavenInfo info = (FileMavenInfo) fileMavenInfo.get(file.getName());
        // Get artefact specific info
        if (info == null && artifactId != null) {
            info = (FileMavenInfo) fileMavenInfo.get(artifactId);
        }
        // Get default info
        if (info == null) {
            info = (FileMavenInfo) fileMavenInfo.get(null);
        }
        // Get empty info
        if (info == null) {
            info = FileMavenInfo.NOINFO;
        }
        return info;
    }

    private static String getCommonPrefix(String initialPrefix, List strings)
    {
        if (strings == null) {
            return initialPrefix;
        }

        String prefix = initialPrefix;
        Iterator it = strings.iterator();
        if (isIgnored(prefix)) {
            prefix = findNextPotentialId(it);
        }

        while (it.hasNext()) {
            int pl = getCommonPrefixLength(prefix, findNextPotentialId(it));
            if (pl != -1) {
                if (pl == 0 || pl < prefix.length()) {
                    int p = prefix.lastIndexOf('.', pl);
                    if (p == -1) {
                        if (isIgnored(initialPrefix)) {
                            return prefix;
                        } else {
                            return initialPrefix;
                        }
                    }
                    prefix = prefix.substring(0, p);
                }
            }
        }

        if (prefix == null || prefix.length() == 0) {
            return initialPrefix;
        }

        return prefix;
    }

    private static boolean isIgnored(String s)
    {
        return (s == null
            || s.startsWith("java.")
            || s.startsWith("javax.")
            || s.startsWith("com.sun.")
            || s.startsWith("org.xml.sax.")
            || s.startsWith("org.omg.")
            || s.startsWith("org.w3c.dom.")
            || s.indexOf("log4j") != -1
        );
    }

    private static String findNextPotentialId(Iterator it)
    {
        while (it.hasNext()) {
            String s = (String) it.next();
            if (!isIgnored(s)) {
                return s;
            }
        }
        return null;
    }

    private static int getCommonPrefixLength(String s, String t)
    {
        if (s == null || t == null || s.length() == 0 || t.length() == 0) {
            return -1;
        }

        int m = Math.min(s.length(), t.length());
        for (int k = 0; k < m; ++k) {
            if (s.charAt(k) != t.charAt(k)) {
                return k;
            }
        }
        return m;
    }
}
