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
import java.util.Iterator;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;

import lu.softec.maven.mavenizer.mavenfile.MavenFile;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSerializer;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSet;

/**
 * Default implementation of the {@link MavenFileSerializer} interface.
 *
 * This implementation is instanciated per-lookup.
 */
public class DefaultMavenFileSerializer implements MavenFileSerializer
{
    private XmlSerializer serializer;

    private File baseDir = null;

    private ArtifactRepository repository;

    public XmlSerializer getSerializer()
    {
        return serializer;
    }

    public void setSerializer(XmlSerializer serializer)
    {
        this.serializer = serializer;
    }

    public File getBaseDir()
    {
        return baseDir;
    }

    public void setBaseDir(File baseDir)
    {
        this.baseDir = baseDir;
    }

    public ArtifactRepository getRepository()
    {
        return repository;
    }

    public void setRepository(ArtifactRepository repository)
    {
        this.repository = repository;
    }

    public void SerializeMavenFileSet(MavenFileSet set) throws IOException
    {
        serializer.startTag(null, MavenFileXmlMarkup.ARTIFACTS_TAG);
        for (Iterator it = set.iterator(); it.hasNext();) {
            MavenFile mvnFile = (MavenFile) it.next();
            SerializeMavenFile(mvnFile);
        }
        serializer.endTag(null, MavenFileXmlMarkup.ARTIFACTS_TAG);
    }

    public void SerializeMavenFile(MavenFile mvnFile) throws IOException
    {
        serializer.startTag(null, MavenFileXmlMarkup.ARTIFACT_TAG);
        writeNameAttribute(mvnFile);
        writeCoordinates(mvnFile);

        for (Iterator it = mvnFile.getDependencies().iterator(); it.hasNext();) {
            serializer.startTag(null, MavenFileXmlMarkup.DEPENDENCIES_TAG);
            while (it.hasNext()) {
                MavenFile file = (MavenFile) it.next();

                serializer.startTag(null, MavenFileXmlMarkup.DEPENDENCY_TAG);
                writeNameAttribute(file);
                writeCoordinates(file);
                serializer.endTag(null, MavenFileXmlMarkup.DEPENDENCY_TAG);
            }
            serializer.endTag(null, MavenFileXmlMarkup.DEPENDENCIES_TAG);
        }
        serializer.endTag(null, MavenFileXmlMarkup.ARTIFACT_TAG);
    }

    private void writeNameAttribute(MavenFile mvnFile) throws IOException
    {
        String relativePath = relativePath(mvnFile);
        if (relativePath != null) {
            serializer.attribute(null, MavenFileXmlMarkup.NAME_ATTRIBUTE, relativePath);
        }
    }

    private void writeCoordinates(MavenFile mvnFile) throws IOException
    {
        writeTag(MavenFileXmlMarkup.GROUPID_TAG, mvnFile.getGroupId());
        writeTag(MavenFileXmlMarkup.ARTIFACTID_TAG, mvnFile.getArtifactId());
        writeTag(MavenFileXmlMarkup.VERSION_TAG, mvnFile.getVersion());
        writeTag(MavenFileXmlMarkup.CLASSIFIER_TAG, mvnFile.getClassifier());
    }

    private String relativePath(MavenFile mvnFile)
    {
        String filePath = mvnFile.getFile().getAbsolutePath();
        if (repository != null && filePath.startsWith(repository.getBasedir())) {
            return null;
        }

        if (baseDir == null) {
            return filePath;
        }

        String dirPath = baseDir.getAbsolutePath();

        if (filePath.startsWith(dirPath)) {
            return filePath.substring(dirPath.length() + 1);
        }
        return filePath;
    }

    private void writeTag(String tag, String text) throws IOException
    {
        if (text != null && tag != null) {
            serializer.startTag(null, tag).text(text).endTag(null, tag);
        }
    }
}
