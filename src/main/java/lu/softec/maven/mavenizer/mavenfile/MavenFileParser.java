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
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Generic interface for a parser deserializing {@link MavenFile} and {@link MavenFileSet} from XML
 */
public interface MavenFileParser
{
    /**
     * Set the XML Pull Parser to read from
     *
     * @param xpp the XPP parser
     */
    void setXmlPullParser(XmlPullParser xpp);

    /**
     * Returns the XML Pull Parser to read from
     *
     * @return the XPP parser
     */
    XmlPullParser getXmlPullParser();

    /**
     * Set the root directory for relative library paths found in the parsed XML
     *
     * @param baseDir the base directory containings the qualified binaries
     */
    void setBaseDir(File baseDir);

    /**
     * Returns the base directory used while resolving relative library paths
     *
     * @return the base directory used while resolving relative library paths
     */
    File getBaseDir();

    /**
     * Read a {@link MavenFile} from the parser
     *
     * On success, the parser position is moved on the end tag of the artifact read On error, parser is stop where the
     * error occurs.
     *
     * @return the {@link MavenFile} instance
     * @throws XmlPullParserException when a parsing error occurs or a previously existing {@link MavenFile} exists and
     * is inconsistant with the one read during parsing
     * @throws IOException when an I/O error occurs during parsing
     * @throws InvalidMavenCoordinatesException when a {@link MavenFile} with an invalid set of coordinate would have
     * been created
     */
    MavenFile getMavenFile() throws XmlPullParserException, IOException, InvalidMavenCoordinatesException;

    /**
     * Read a {@link MavenFileSet} from the parser
     *
     * On success, the parser position is moved on the end tag of the set read On error, parser is stop where the error
     * occurs.
     *
     * @return the {@link MavenFile} instance
     * @throws XmlPullParserException when a parsing error occurs or a previously existing {@link MavenFile} exists and
     * is inconsistant with one read during parsing
     * @throws IOException when an I/O error occurs during parsing
     * @throws InvalidMavenCoordinatesException when a {@link MavenFile} with an invalid set of coordinate would have
     * been created
     */
    MavenFileSet getMavenFileSet() throws XmlPullParserException, IOException, InvalidMavenCoordinatesException;

    /**
     * Returns the local repository used to resolved missing files
     *
     * @return the local repository used to resolved missing files
     */
    ArtifactRepository getRepository();

    /**
     * Set the local repository used to resolved missing files
     *
     * @param repository the local repository used to resolved missing files
     */
    void setRepository(ArtifactRepository repository);

    /**
     * Returns the remote repositories used to download missing files
     *
     * @return the remote repositories used to download missing files
     */
    List getRemoteRepositories();

    /**
     * Set the remote repositories used to download missing files
     *
     * @param remoteRepositories the remote repositories used to download missing files
     */
    void setRemoteRepositories(List remoteRepositories);
}
