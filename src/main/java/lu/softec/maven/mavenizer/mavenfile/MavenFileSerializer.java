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

import org.codehaus.plexus.util.xml.pull.XmlSerializer;

/**
 * Generic interface for a serializer serializing {@link MavenFile} and {@link MavenFileSet} to XML
 */
public interface MavenFileSerializer
{
    /**
     * Set the serializer used for writing
     *
     * @param serializer the serializer to be used for writing
     */
    void setSerializer(XmlSerializer serializer);

    /**
     * Returns the serializer used for writing
     *
     * @return the serializer used for writing
     */
    XmlSerializer getSerializer();

    /**
     * Set the root directory used to truncate library paths into relative paths.
     *
     * @param baseDir the root directory used to truncate library paths into relative paths.
     */
    void setBaseDir(File baseDir);

    /**
     * Returns the root directory used to truncate library paths into relative paths.
     *
     * @return the root directory used to truncate library paths into relative paths.
     */
    File getBaseDir();

    /**
     * Write a {@link MavenFile} to the serializer
     *
     * @param mvnFile the {@link MavenFile} to be serialized
     * @throws IOException when an I/O error occurs during serialization
     */
    void SerializeMavenFile(MavenFile mvnFile) throws IOException;

    /**
     * Write a {@link MavenFileSet} to the serializer
     *
     * @param set the set to be serialized
     * @throws IOException when an I/O error occurs during serialization
     */
    void SerializeMavenFileSet(MavenFileSet set) throws IOException;
}
