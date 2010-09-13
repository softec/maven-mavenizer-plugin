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
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.codehaus.plexus.util.FileUtils;

/**
 * Download and extract the binaries of the project
 *
 * @goal download
 * @phase generate-sources
 */
public class RemoteArchiveExtractorMojo extends AbstractArchiveMavenizerMojo
{
    /**
     * The Plexus Archiver Manager.
     *
     * @component role="org.codehaus.plexus.archiver.manager.ArchiverManager"
     */
    private ArchiverManager archiveManager;

    /**
     * @throws MojoExecutionException if the operation fails, even partially
     */
    public void execute() throws MojoExecutionException
    {
        // If no archive file is provided, expect the binaries to be already extracted and do nothing
        if (getArchiveFile() == null) {
            return;
        }

        // Retrieve archive if URL is provided and remote file is newer than local one

        if (getURLLastModificationDate() > getArchiveFile().lastModified()) {
            getLog().info(
                "Retrieving project archive from " + getArchiveURL().toString() + " to " +
                    getArchiveFile().getAbsolutePath());

            try {
                FileUtils.copyURLToFile(getArchiveURL(), getArchiveFile());
            } catch (IOException e) {
                throw new MojoExecutionException(
                    "Unable to retrieve the project archive from " + getArchiveURL().toString() + " to " +
                        getArchiveFile().getAbsolutePath(), e);
            }

            deleteDirectory(getBinariesBaseDir());
        } else {
            if (getArchiveURL() != null) {
                getLog().info("No changes in remote file detected, skipping archive download.");
            }
        }

        // Extract archive file if archive is newer than extracted data

        if (getBinariesBaseDir().mkdirs() ||
            getArchiveFile().lastModified() > getBinariesBaseDir().lastModified())
        {
            try {
                UnArchiver unarchiver = archiveManager.getUnArchiver(getArchiveFile());
                unarchiver.setSourceFile(getArchiveFile());
                unarchiver.setDestDirectory(getBinariesBaseDir());

                // Extract libraries
                FileSelector selector = getLibsSelector();
                if (selector != null) {
                    unarchiver.setFileSelectors(new FileSelector[]{selector});
                    getLog().info("Expanding libraries from project archive " + getArchiveFile().getAbsolutePath());
                } else {
                    getLog().info("Expanding all files from project archive " + getArchiveFile().getAbsolutePath());
                }
                unarchiver.extract();

                // If libraries has been selectively extracted and other dependencies are selected, extract them
                if (selector != null) {
                    selector = getDepsSelector();
                    if (selector != null) {
                        unarchiver.setFileSelectors(new FileSelector[]{selector});
                        getLog()
                            .info("Expanding dependencies from project archive " + getArchiveFile().getAbsolutePath());
                        unarchiver.extract();
                    }
                }
            } catch (NoSuchArchiverException e) {
                deleteDirectory(getBinariesBaseDir());
                throw new MojoExecutionException("No unarchiver is available to handle the retrieved archive", e);
            } catch (ArchiverException e) {
                deleteDirectory(getBinariesBaseDir());
                throw new MojoExecutionException("Unable to expand the retrieved archive", e);
            }
        } else {
            getLog().info("Archive file seems unchanged, skipping archive expansion.");
        }
    }

    /**
     * Set the base directory used to store the downloaded archive file
     *
     * @param archiveBaseDir the directory used to store the downloaded archive file or from where it should be
     * extracted
     */
    public void setArchiveBaseDir(File archiveBaseDir)
    {
        this.archiveBaseDir = archiveBaseDir;
    }

    /**
     * Set the URL used to check and download the latest version of the archive file of the project to mavenize.
     *
     * @param archiveURL the URL used to check and download the latest version of the archive file of the project to
     * mavenize.
     */
    public void setArchiveURL(URL archiveURL)
    {
        this.archiveURL = archiveURL;
    }

    /**
     * Return a FileSelector for selecting the libraries from the archive file. If no selection has been provided, null
     * is returned.
     *
     * @return a FileSelector for selecting the libraries into the archive file.
     */
    public FileSelector getLibsSelector()
    {
        return getSelector(getLibsIncludes(), getLibsExcludes());
    }

    /**
     * Return a FileSelector for selecting the additionnal libraries used only for dependency resolution from the
     * archive file.  If no selection has been provided, null is returned.
     *
     * @return a FileSelector for selecting the additionnal libraries used only for dependency resolution from the
     *         archive file.
     */
    public FileSelector getDepsSelector()
    {
        return getSelector(getDepsIncludes(), getDepsExcludes());
    }

    /**
     * Internal helper function to build IncludeExcludeFileSelector
     *
     * @param includes includes patterns for the selector
     * @param excludes excludes patterns for the selector
     * @return a IncludeExcludeFileSelector based on the includes and excludes arguments. If both arguments are null,
     *         null is returned.
     */
    private static FileSelector getSelector(String[] includes, String[] excludes)
    {
        if (includes == null && excludes == null) {
            return null;
        }

        IncludeExcludeFileSelector fs = new IncludeExcludeFileSelector();
        fs.setIncludes(includes);
        fs.setExcludes(excludes);
        fs.setUseDefaultExcludes(true);
        return fs;
    }

    /**
     * Internal helper function to recursively delete a directory without throwing an error. If an error occurs, it is
     * simply logged as a warning and processing is continued.
     *
     * @param file the directory to delete
     */
    private void deleteDirectory(File file)
    {
        if (file != null && file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                getLog().warn("Unable to cleanup binary base directory " + file.getAbsolutePath(), e);
            }
        }
    }

    /**
     * Return the timestamp of the last modification to the remote archive file. If no URL has been provided, 0L is
     * returned. If the archive URL is not reachable or any other error occurs, Long.MAX_VALUE is returned.
     *
     * @return the timestamp of the last modification to the remote archive file.
     */
    public long getURLLastModificationDate()
    {
        if (getArchiveURL() == null) {
            return 0;
        }

        long lastmodified = Long.MAX_VALUE;
        try {
            URLConnection uc = getArchiveURL().openConnection();
            lastmodified = uc.getLastModified();
        } catch (IOException e) { // ignored
        }
        return lastmodified;
    }
}
