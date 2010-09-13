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

/**
 * Parameter class to providing maven file hints
 */
public class FileMavenInfo
{
    public static final FileMavenInfo NOINFO = new FileMavenInfo();

    /**
     * Filename without extension or artifactId for files properly named, to allow version independant matching
     */
    private String name;

    /**
     * groupId for this file. If null, the default or discovered one is used
     */
    private String groupId;

    /**
     * artifactId for this file. If null, the default or discovered one is used
     */
    private String artifactId;

    /**
     * version for this file. If null, the default or discovered one is used
     */
    private String version;

    /**
     * classifier for this file. If null, the default, which may be null
     */
    private String classifier;

    /**
     * Return a filename without extension or an artifactId for files properly named which allow version independant
     * matching.
     *
     * @return a filename without extension or an artifactId for files properly named which allow version independant
     *         matching
     */
    public String getName()
    {
        return name;
    }

    /**
     * Return groupId for this file. If null, use the default or discovered one.
     *
     * @return groupId for this file. If null, use the default or discovered one
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * Return artifactId for this file. If null, use the default or discovered one.
     *
     * @return artifactId for this file. If null, use the default or discovered one
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * Return version for this file. If null, use the default or discovered one.
     *
     * @return verison for this file. If null, use the default or discovered one
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Return classifier for this file. If null, use the default or null.
     *
     * @return verison for this file. If null, use the default or null.
     */
    public String getClassifier()
    {
        return classifier;
    }
}
