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
package lu.softec.maven.mavenizer.analyzer.mavenasm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Dependency;

import lu.softec.maven.mavenizer.analyzer.ClassWalkListener;
import lu.softec.maven.mavenizer.analyzer.ClassWalkerExecutionException;
import lu.softec.maven.mavenizer.analyzer.ClassWalkerRuntimeException;
import lu.softec.maven.mavenizer.mavenfile.InvalidMavenCoordinatesException;
import lu.softec.maven.mavenizer.mavenfile.MavenFile;
import lu.softec.maven.mavenizer.mavenfile.MavenFileFactory;

/**
 * Default implementation of {@link MavenDependencyWalker} Dependencies are resolved as needed.
 */
public class DefaultMavenDependencyWalker implements MavenDependencyWalker
{
    /**
     * MavenFileFactory used to create MavenFiles to resolve dependencies
     */
    private MavenFileFactory mavenFileFactory;

    /**
     * Local artifact repository where to look for dependencies
     */
    private ArtifactRepository repository;

    /**
     * Remote actifact repositories where missing dependencies could be downloaded
     */
    private List remoteRepositories;

    /**
     * List of {@link Dependency} to be walked
     */
    private List dependencies = null;

    /**
     * List of registered {@link ClassWalkListener}
     */
    private List listeners = new ArrayList();

    /**
     * Debug mode
     */
    private boolean debugMode = false;

    public List getDependencies()
    {
        return Collections.unmodifiableList(dependencies);
    }

    public void setDependencies(List dependencies)
    {
        this.dependencies = new ArrayList(dependencies);
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

    public void setRemoteRepositories(List remoteArtifactRepositories)
    {
        this.remoteRepositories = new ArrayList(remoteArtifactRepositories);
    }

    public void addClassWalkListener(ClassWalkListener listener)
    {
        listeners.add(listener);
    }

    public void scan() throws ClassWalkerExecutionException
    {
        if (dependencies == null) {
            fireDebug(".No dependency to process.");
            return;
        }

        try {
            int i = 1;
            for (Iterator it = dependencies.iterator(); it.hasNext(); i++) {
                Dependency dep = (Dependency) it.next();

                fireDebug(
                    ".Processing dependency " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion() +
                        ":" + dep.getClassifier());
                MavenFile mvnFile;
                try {
                    mvnFile = mavenFileFactory
                        .getMavenFile(null, dep.getGroupId(), dep.getArtifactId(), dep.getVersion(),
                            dep.getClassifier(),
                            null, repository, remoteRepositories);
                } catch (InvalidMavenCoordinatesException e) {
                    // Should never happen
                    throw new ClassWalkerRuntimeException(e);
                } catch (ArtifactNotFoundException e) {
                    throw new ClassWalkerRuntimeException(e);
                } catch (ArtifactResolutionException e) {
                    throw new ClassWalkerRuntimeException(e);
                }

                fireDebug("..Analysing file " + mvnFile.getFile().getAbsolutePath());
                processJarFile(i * 100 / dependencies.size(), mvnFile.getFile());
                fireDebug("..File analysed.");
                fireDebug(".Dependency processed.");
            }
        } catch (ClassWalkerRuntimeException e) {
            throw new ClassWalkerExecutionException(e.getFile(), e.getCause());
        }
    }

    public void setDebugMode(boolean debugEnabled)
    {
        this.debugMode = debugEnabled;
    }

    /**
     * Process a JarFile, firing listener events as needed.
     *
     * @param i current progress indicator
     * @param file file to process
     */
    private void processJarFile(int i, File file)
    {
        ZipFile zip = null;
        try {
            zip = new JarFile(file);
            Enumeration entries = zip.entries();
            int nbEntries = zip.size();
            int curEntry = 0;

            fireLibraryWalkFileOpened(i, file);

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    fireLibraryWalkProcessClass((++curEntry * 100 / nbEntries), zip.getInputStream(entry));
                }
            }
            zip.close();

            fireLibraryWalkFileClosed();
        } catch (IOException e) {
            throw new ClassWalkerRuntimeException(file, e);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {  /* ignored */ }
            }
        }
    }

    /**
     * Fire debug events in all registered listener.
     *
     * @param message the debug message
     */
    private void fireDebug(String message)
    {
        if (debugMode && message != null) {
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                ((ClassWalkListener) it.next()).debug(message);
            }
        }
    }

    /**
     * Fire LibraryWalkProcessClass events in all registered listener.
     *
     * @param i current progress indicator
     * @param in input stream to be processed
     */
    private void fireLibraryWalkProcessClass(int i, InputStream in)
    {
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ((ClassWalkListener) it.next()).libraryWalkProcessClass(i, in);
        }
    }

    /**
     * Fire LibraryWalkFileOpened events in all registered listener.
     *
     * @param i current progress indicator
     * @param file file currently processed
     */
    private void fireLibraryWalkFileOpened(int i, File file)
    {
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ((ClassWalkListener) it.next()).libraryWalkFileOpened(i, file);
        }
    }

    /**
     * Fire LibraryWalkFileClosed events in all registered listener.
     */
    private void fireLibraryWalkFileClosed()
    {
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ((ClassWalkListener) it.next()).libraryWalkFileClosed();
        }
    }
}
