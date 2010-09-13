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
import java.net.URL;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryWalkListener;
import org.codehaus.plexus.util.DirectoryWalker;
import org.codehaus.plexus.util.FileUtils;

/**
 * Base class for Mavenizer mojo providing paramater shared by all mojos.
 */
public abstract class AbstractMavenizerMojo extends AbstractMojo
{
    /**
     * Base directory where the binaries of the project to be mavenized are stored. If archiveFile and/or archiveURL
     * parameter is provided, these binaries are extracted from the specified archive file during the generate-source
     * phase. If no archive file is provided, the binaries should be available in the provided directory.
     *
     * @parameter expression="${project.build.directory}/bin"
     * @required
     */
    private File binariesBaseDir;

    /**
     * Mavenizer configuration file, results of the analysis and configuration for deployment
     *
     * @parameter expression="${project.build.directory}/mavenizer.xml"
     */
    private File mavenizerConfigFile;

    /**
     * Base directory where the archive of the project to be mavenized should be stored. This directory is only used to
     * compute a name for the archive file when no archiveFile parameter has been provided.
     *
     * @parameter expression="${project.build.directory}/archive"
     */
    protected File archiveBaseDir;

    /**
     * URL of the archive of the project to be mavenized. If ommitted, the file is directly expanded from the
     * archiveFile parameter.
     *
     * @parameter
     */
    protected URL archiveURL;

    /**
     * File of the archive of the project to be mavenized. If URL is also provided, this file could be updated.
     *
     * @parameter
     */
    private File archiveFile;

    /**
     * Local repository
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Maven project
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Return the base directory where the binaries of the project to be mavenized are stored.
     *
     * @return the base directory where the binaries of the project to be mavenized are stored.
     */
    public File getBinariesBaseDir()
    {
        return binariesBaseDir;
    }

    /**
     * Returns the file used to store the mavenizer analysis results and configuration
     *
     * @return the file used to store the mavenizer analysis results and configuration
     */
    public File getMavenizerConfigFile()
    {
        return mavenizerConfigFile;
    }

    /**
     * Return the base directory used to store the downloaded archive file
     *
     * @return the base directory used to store the downloaded archive file or from where it should be extracted
     */
    public File getArchiveBaseDir()
    {
        return archiveBaseDir;
    }

    /**
     * Return the URL used to check and download the latest version of the archive file of the project to mavenize.
     *
     * @return the URL used to check and download the latest version of the archive file of the project to mavenize
     */
    public URL getArchiveURL()
    {
        return archiveURL;
    }

    /**
     * Return the local file used to store the archive of the project to mavenize. If no archiveFile argument has been
     * provided, the archiveFile is initialized based on the archive base directory and the archive url.
     *
     * @return the local file used to store the archive of the project to mavenize.
     */
    public File getArchiveFile()
    {
        if (archiveFile == null) {
            if (archiveURL == null || archiveBaseDir == null) {
                return null;
            }
            archiveFile = new File(getArchiveBaseDir(), FileUtils.removePath(getArchiveURL().getPath()));
        }
        return archiveFile;
    }

    /**
     * Returns the current local repository
     *
     * @return the current local repository
     */
    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    /**
     * Returns the current maven project
     *
     * @return the current maven project
     */
    public MavenProject getProject()
    {
        return project;
    }

    // Utility methods

    /**
     * Utility class representing a time range
     */
    public static class TimeRange
    {
        private long lower;

        private long higher;

        TimeRange(long lower, long higher)
        {
            this.lower = lower;
            this.higher = higher;
        }

        public long getLower()
        {
            return lower;
        }

        public long getHigher()
        {
            return higher;
        }
    }

    /**
     * Listener class to compute the modification time range of a list of file
     */
    private static class TimeRangeListener implements DirectoryWalkListener
    {
        private long lowerTime = Long.MAX_VALUE;

        private long higherTime = 0;

        public TimeRange getModificationTimeRange()
        {
            return new TimeRange(lowerTime, higherTime);
        }

        public void directoryWalkStarting(File file)
        {
        }

        public void directoryWalkStep(int i, File file)
        {
            if (file.lastModified() < lowerTime) {
                lowerTime = file.lastModified();
            }
            if (file.lastModified() > higherTime) {
                higherTime = file.lastModified();
            }
        }

        public void directoryWalkFinished()
        {
        }

        public void debug(String s)
        {
        }
    }

    /**
     * Returns the modifications time range of files under a given directory
     *
     * @param file the directory to be scanned
     * @return the modifications time range of files under a given directory or null if file is not a directory
     */
    public static TimeRange getFilesModificationTimes(File file)
    {
        if (!file.isDirectory()) {
            return null;
        }

        DirectoryWalker walker = new DirectoryWalker();
        TimeRangeListener listener = new TimeRangeListener();

        walker.setBaseDir(file);
        walker.addDirectoryWalkListener(listener);
        walker.scan();
        return listener.getModificationTimeRange();
    }

    /**
     * Returns the latest modification date of files under a given directory
     *
     * @param file the directory to be scanned
     * @return the latest modification date of files under a given directory
     */
    public static long getLatestFileModification(File file)
    {
        return getFilesModificationTimes(file).getHigher();
    }

    /**
     * Returns the earliest modification date of files under a given directory
     *
     * @param file the directory to be scanned
     * @return the earliest modification date of files under a given directory
     */
    public static long getEarliestFileModification(File file)
    {
        return getFilesModificationTimes(file).getLower();
    }
}
