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
import java.util.Iterator;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.FileUtils;

/**
 * A Object that hold a library file with its maven coordinates and dependencies. This object is not mutable.
 *
 * Constructing useful instances of this object needs an appropriate factory implementing {@link MavenFileFactory} or
 * needs a derived mutable class.
 */
public class MavenFile implements Comparable
{
    /**
     * This file
     */
    protected File file;

    /**
     * groupId for this file.
     */
    protected String groupId;

    /**
     * artifactId for this file.
     */
    protected String artifactId;

    /**
     * version for this file.
     */
    protected String version;

    /**
     * classifier for this file. May be null.
     */
    protected String classifier;

    /**
     * dependencies of this file
     */
    protected MavenFileSet dependencies;

    /**
     * Private constructor to avoid instance creation
     */
    protected MavenFile()
    {

    }

    /**
     * Returns the {@link File} qualified by this instance
     *
     * @return the {@link File} qualified by this instance
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Returns the groupId for this file.
     *
     * @return the groupId for this file.
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * Returns the artifactId for this file.
     *
     * @return the artifactId for this file.
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * Returns the version for this file.
     *
     * @return the version for this file.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the classifier for this file.
     *
     * @return the classifier for this file. May be null.
     */
    public String getClassifier()
    {
        return classifier;
    }

    /**
     * Returns the packaging for this file. This currently returns the file extension.
     *
     * @return the packaging for this file.
     */
    public String getPackaging()
    {
        return FileUtils.getExtension(file.getName());
    }

    /**
     * Returns the dependencies for this file.
     *
     * @return the dependencies for this file.
     */
    public MavenFileSet getDependencies()
    {
        return dependencies;
    }

    /**
     * Gernerate a minimal model representing the coodinates of this {@link MavenFile}
     *
     * @return a {@link Model} 4.0.0 representing the coordinates of this {@link MavenFile}.
     */
    public Model getMinimalModel()
    {
        Model model = new Model();

        model.setModelVersion("4.0.0");

        model.setGroupId(getGroupId());
        model.setArtifactId(getArtifactId());
        model.setVersion(getVersion());
        model.setPackaging(getPackaging());

        return model;
    }

    /**
     * Gernerate a complete model representing this {@link MavenFile}
     *
     * @return a {@link Model} 4.0.0 representing this {@link MavenFile}.
     */
    public Model getModel()
    {
        Model model = getMinimalModel();

        model.setName(getArtifactId());

        Iterator it = getDependencies().iterator();
        while (it.hasNext()) {
            MavenFile mvnFile = (MavenFile) it.next();
            Dependency dep = new Dependency();
            dep.setGroupId(mvnFile.getGroupId());
            dep.setArtifactId(mvnFile.getArtifactId());
            dep.setVersion(mvnFile.getVersion());
            dep.setClassifier(mvnFile.getClassifier());
            model.addDependency(dep);
        }

        return model;
    }

    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MavenFile that = (MavenFile) o;

        return getFile().equals(that.getFile());
    }

    public int hashCode()
    {
        return getFile().hashCode();
    }

    /**
     * Implements Comparable by delegating comparison to the {@link File object}
     *
     * @param o the {@link MavenFile} to be compared this one
     * @return the values returns by this.getFile().compareTo().
     */
    public int compareTo(Object o)
    {
        MavenFile file = (MavenFile) o;
        return getFile().compareTo(file.getFile());
    }
}
