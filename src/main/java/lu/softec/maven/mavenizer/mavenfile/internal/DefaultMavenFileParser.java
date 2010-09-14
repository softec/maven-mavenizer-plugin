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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import lu.softec.maven.mavenizer.mavenfile.InvalidMavenCoordinatesException;
import lu.softec.maven.mavenizer.mavenfile.MavenFile;
import lu.softec.maven.mavenizer.mavenfile.MavenFileFactory;
import lu.softec.maven.mavenizer.mavenfile.MavenFileParser;
import lu.softec.maven.mavenizer.mavenfile.MavenFileSet;

/**
 * Default implementation of the {@link MavenFileParser} interface.
 *
 * This implementation is instanciated per-lookup.
 */
public class DefaultMavenFileParser implements MavenFileParser
{
    /**
     * MavenFileFactory populated by Plexus
     */
    private MavenFileFactory mavenFileFactory;

    /**
     * xmlPullParser used for parsing
     */
    private XmlPullParser xmlPullParser;

    /**
     * Base directory used for relative file name resolution
     */
    private File baseDir;

    /**
     * Local artifact repository used to search for missing artifacts
     */
    private ArtifactRepository repository;

    /**
     * Remote repositories used to download missing artifacts
     */
    private List remoteRepositories;

    public XmlPullParser getXmlPullParser()
    {
        return xmlPullParser;
    }

    public void setXmlPullParser(XmlPullParser xpp)
    {
        this.xmlPullParser = xpp;
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

    public List getRemoteRepositories()
    {
        return Collections.unmodifiableList(remoteRepositories);
    }

    public void setRemoteRepositories(List remoteRepositories)
    {
        this.remoteRepositories = new ArrayList(remoteRepositories);
    }

    public MavenFile getMavenFile()
        throws XmlPullParserException, IOException, InvalidMavenCoordinatesException, ArtifactResolutionException,
        ArtifactNotFoundException
    {
        return getMavenFile(MavenFileXmlMarkup.ARTIFACT_TAG, false);
    }

    public MavenFileSet getMavenFileSet()
        throws XmlPullParserException, IOException, InvalidMavenCoordinatesException, ArtifactResolutionException,
        ArtifactNotFoundException
    {
        return getMavenFileSet(MavenFileXmlMarkup.ARTIFACTS_TAG, MavenFileXmlMarkup.ARTIFACT_TAG, true);
    }

    /**
     * Parse a set of maven file definition.
     *
     * @param tags list enclosing tags
     * @param tag maven file definition enclosing tags
     * @param withDeps when true, the definitions may contains dependencies
     * @return a {@link MavenFileSet} representing the parsed list
     * @throws XmlPullParserException when a parsing error occurs
     * @throws IOException when an I/O error occurs
     * @throws InvalidMavenCoordinatesException when the definition provide invalid maven coordinates
     */
    private MavenFileSet getMavenFileSet(String tags, String tag, boolean withDeps)
        throws XmlPullParserException, IOException, InvalidMavenCoordinatesException, ArtifactResolutionException,
        ArtifactNotFoundException
    {
        InternalMavenFileSet ms = new InternalMavenFileSet();

        xmlPullParser.require(XmlPullParser.START_TAG, null, tags);

        while (xmlPullParser.nextTag() != XmlPullParser.END_TAG) {
            ms.add(getMavenFile(tag, withDeps));
        }

        xmlPullParser.require(XmlPullParser.END_TAG, null, tags);

        return ms;
    }

    /**
     * Parse a maven file definition enclosed between tags
     *
     * @param tag enclosing tags
     * @param withDeps when true, the definition may contains dependencies
     * @return the MavenFile represented by the parsed definition
     * @throws XmlPullParserException when a parsing error occurs
     * @throws IOException when an I/O error occurs
     * @throws InvalidMavenCoordinatesException when the definition provide invalid maven coordinates
     */
    private MavenFile getMavenFile(String tag, boolean withDeps)
        throws XmlPullParserException, IOException, InvalidMavenCoordinatesException, ArtifactResolutionException,
        ArtifactNotFoundException
    {
        xmlPullParser.require(XmlPullParser.START_TAG, null, tag);

        MavenFile mvnFile = getMavenFile(withDeps);

        xmlPullParser.require(XmlPullParser.END_TAG, null, tag);

        return mvnFile;
    }

    /**
     * Parse a maven file definition
     *
     * @param withDeps when true, the definition may contains dependencies
     * @return the MavenFile represented by the parsed definition
     * @throws XmlPullParserException when a parsing error occurs
     * @throws IOException when an I/O error occurs
     * @throws InvalidMavenCoordinatesException when the definition provide invalid maven coordinates
     */
    private MavenFile getMavenFile(boolean withDeps)
        throws XmlPullParserException, IOException, InvalidMavenCoordinatesException, ArtifactResolutionException,
        ArtifactNotFoundException
    {
        String groupId = null;
        String artifactId = null;
        String version = null;
        String classifier = null;
        MavenFileSet deps = null;
        File file = getFile();

        if (file != null && !file.isFile()) {
            throw new IOException(
                "File " + file.getAbsolutePath() + " does not exists or is not a readable file.");
        }

        while (xmlPullParser.nextTag() != XmlPullParser.END_TAG) {
            if (groupId == null && xmlPullParser.getName().equals(MavenFileXmlMarkup.GROUPID_TAG)) {
                groupId = getTaggedString();
            } else if (artifactId == null && xmlPullParser.getName().equals(MavenFileXmlMarkup.ARTIFACTID_TAG)) {
                artifactId = getTaggedString();
            } else if (version == null && xmlPullParser.getName().equals(MavenFileXmlMarkup.VERSION_TAG)) {
                version = getTaggedString();
            } else if (classifier == null && xmlPullParser.getName().equals(MavenFileXmlMarkup.CLASSIFIER_TAG)) {
                classifier = getTaggedString();
            } else if (withDeps && xmlPullParser.getName().equals(MavenFileXmlMarkup.DEPENDENCIES_TAG)) {
                xmlPullParser.require(XmlPullParser.START_TAG, null, MavenFileXmlMarkup.DEPENDENCIES_TAG);
                deps = getMavenFileSet(MavenFileXmlMarkup.DEPENDENCIES_TAG, MavenFileXmlMarkup.DEPENDENCY_TAG, false);
            } else {
                throw getParsingException("Unknown, unexpected or duplicate tag found.");
            }
        }

        try {
            return mavenFileFactory
                .getMavenFile(file, groupId, artifactId, version, classifier, deps, repository, remoteRepositories);
        } catch (IllegalArgumentException e) {
            throw getParsingException(
                "This declaration mismatch with a previously existing instance of file " +
                    file.getAbsolutePath());
        }
    }

    /**
     * Parse the name attribute when available and returns the corresponding file
     *
     * @return a {@link File} representing the name attribute or null if no attribute has been found
     */
    private File getFile()
    {
        String fileName = xmlPullParser.getAttributeValue(null, MavenFileXmlMarkup.NAME_ATTRIBUTE);
        if (fileName == null) {
            return null;
        }

        File file = new File(fileName);

        if (file.isAbsolute()) {
            return file;
        }

        return new File(baseDir, fileName);
    }

    /**
     * Parse a simple tagged string
     *
     * @return the string
     * @throws IOException when an I/O error occurs
     * @throws XmlPullParserException when a parsing error occurs
     */
    private String getTaggedString() throws IOException, XmlPullParserException
    {
        xmlPullParser.require(XmlPullParser.START_TAG, null, null);
        String tag = xmlPullParser.getName();
        String text = xmlPullParser.nextText();
        xmlPullParser.require(XmlPullParser.END_TAG, null, tag);

        return text;
    }

    private XmlPullParserException getParsingException(String message)
        throws XmlPullParserException
    {
        return getParsingException(message, -1, null, null);
    }

    private XmlPullParserException getParsingException(int type, String name)
        throws XmlPullParserException
    {
        return getParsingException(null, type, null, name);
    }

    private XmlPullParserException getParsingException(String message, int type, String name)
        throws XmlPullParserException
    {
        return getParsingException(message, type, null, name);
    }

    private XmlPullParserException getParsingException(int type, String namespace,
        String name)
        throws XmlPullParserException
    {
        return getParsingException(null, type, namespace, name);
    }

    private XmlPullParserException getParsingException(String message, int type, String namespace, String name)
        throws XmlPullParserException
    {
        return new XmlPullParserException(
            ((message != null) ? message + " " : "")
                + ((type > 0) ? ("Expected event " + XmlPullParser.TYPES[type]
                + (name != null ? " with name '" + name + "'" : "")
                + (namespace != null && name != null ? " and namespace '" + namespace + "'" : "") + " but got") : "Got")
                + ((type < 0 || type != xmlPullParser.getEventType()) ?
                " " + XmlPullParser.TYPES[xmlPullParser.getEventType()] : "")
                + ((xmlPullParser.getName() != null &&
                (type < 0 || (name != null && !name.equals(xmlPullParser.getName())))) ?
                " with name '" + xmlPullParser.getName() + "'" : "")
                + (((xmlPullParser.getNamespace() != null && xmlPullParser.getNamespace().length() > 0)
                && (type < 0 || (namespace != null && name != null
                && xmlPullParser.getName() != null && !name.equals(xmlPullParser.getName())
                && !namespace.equals(xmlPullParser.getNamespace())))) ? " and" : "")
                + (((xmlPullParser.getNamespace() != null && xmlPullParser.getNamespace().length() > 0)
                && (type < 0 || (namespace != null && xmlPullParser.getNamespace() != null
                && !namespace.equals(xmlPullParser.getNamespace())))) ?
                " namespace '" + xmlPullParser.getNamespace() + "'" : "")
                + (" (position: " + xmlPullParser.getPositionDescription()) + ")");
    }
}
