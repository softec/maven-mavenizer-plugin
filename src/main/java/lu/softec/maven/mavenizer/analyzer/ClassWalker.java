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
package lu.softec.maven.mavenizer.analyzer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.codehaus.plexus.util.DirectoryWalkListener;
import org.codehaus.plexus.util.DirectoryWalker;

/**
 * Utility class used to walk through classes from a directory hierarchy containing either .class files or .jar files.
 *
 * One or more {@link ClassWalkListener} could be associated to this class to perform actions on the walked classes.
 * Processed files may be selected using includes and excludes patterns. SCM files are always excluded.
 *
 * This class is basically a wrapper over a {@link org.codehaus.plexus.util.DirectoryWalker} to walk over classes
 * contained in .class or .jar files walked by the underlying DirectoryWalker.
 */
public class ClassWalker
{
    /**
     * Internal DirectoryWalker wrapped for processing files
     */
    private DirectoryWalker walker;

    /**
     * Current reporting mode. When true, debug message are provided to the listeners.
     */
    private boolean debugMode = false;

    /**
     * Internal wrapper over a ClassWalkListener to expose a DirectoryWalkListener interface to the DirectoryWalker.
     */
    private static class LibraryWalkListenerWrapper implements DirectoryWalkListener
    {
        /**
         * ClassWalkListener wrapped by this wrapper
         */
        private ClassWalkListener listener;

        /**
         * Current reporting mode. When true, debug message are provided to the listener.
         */
        private boolean debugMode = false;

        /**
         * Construct an instance wrapping the provided ClassWalkListener.
         *
         * @param listener the listener to be wrapped
         * @param debugMode the reporting mode to use. When true, debug message are provided to the listener.
         */
        LibraryWalkListenerWrapper(ClassWalkListener listener, boolean debugMode)
        {
            this.listener = listener;
            this.debugMode = debugMode;
        }

        /**
         * Called once by the DirectoryWalker when starting the walk, it pass over the call to the libraryWalkStarted
         * method of the underlying ClassWalkListener listener.
         *
         * @param file the base directory about to be walked
         */
        public void directoryWalkStarting(File file)
        {
            listener.libraryWalkStarted(file);
        }

        /**
         * Called once by the DirectoryWalker when the walk is finished, it pass over the call to the
         * libraryWalkFinished method of the underlying ClassWalkListener listener.
         */
        public void directoryWalkFinished()
        {
            listener.libraryWalkFinished();
        }

        /**
         * Called when an error occurs, it pass over the call to the error method of the underlying ClassWalkListener
         * listener.
         *
         * @param file file currently concerned by the error
         * @param t throwable containing information about the error
         */
        public void error(File file, Throwable t)
        {
            listener.error(file, t);
        }

        /**
         * Called when a debug message is written, it pass over the call to the debug method of the underlying
         * ClassWalkListener listener in debug mode only.
         *
         * @param s the debug message
         */
        public void debug(String s)
        {
            if (debugMode) {
                listener.debug(s);
            }
        }

        /**
         * Called by the DirectoryWalker for each file walked, it process .class and .jar file and ignore others. For
         * each file, the libraryWalkFileOpened of the underlying ClassWalkListener listener is first called, passing
         * over the recieved arguments. Then for each Class in the file, the libraryWalkProcessClass of the underlying
         * ClassWalkListener listener is called. Finally the libraryWalkFileClosed of the underlying ClassWalkListener
         * listener is called.
         *
         * @param i progress information computed by the DirectoryWalker
         * @param file file to process
         */
        public void directoryWalkStep(int i, File file)
        {
            if (file.getName().endsWith(".class")) {
                processClassFile(i, file);
            } else if (file.getName().endsWith(".jar")) {
                processJarFile(i, file);
            } else {
                debug("File not processed: " + file.getAbsolutePath());
            }
        }

        /**
         * Internal method to process a .class file.
         *
         * @param i progress information computed by the DirectoryWalker
         * @param file file to process
         */
        private void processClassFile(int i, File file)
        {
            InputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(file));

                listener.libraryWalkFileOpened(i, file);

                listener.libraryWalkProcessClass(100, in);
                in.close();

                listener.libraryWalkFileClosed();
            } catch (IOException e) {
                error(file, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) { /* ignored */ }
                }
            }
        }

        /**
         * Internal method to process a .jar file, extract .class file out of it.
         *
         * @param i progress information computed by the DirectoryWalker
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

                listener.libraryWalkFileOpened(i, file);

                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        listener.libraryWalkProcessClass((++curEntry * 100 / nbEntries), zip.getInputStream(entry));
                    }
                }
                zip.close();

                listener.libraryWalkFileClosed();
            } catch (IOException e) {
                error(file, e);
            } finally {
                if (zip != null) {
                    try {
                        zip.close();
                    } catch (IOException e) {  /* ignored */ }
                }
            }
        }
    }

    /**
     * Contructs an instance of ClassWalker, wrapping a DirectoryWalker with default SCM exclusions.
     */
    public ClassWalker()
    {
        walker = new DirectoryWalker();
        walker.addSCMExcludes();
    }

    /**
     * Add some file exclusion patterns
     *
     * @param excludes array of file exclusion patterns
     */
    public void addExcludes(String[] excludes)
    {
        if (excludes != null) {
            for (int i = 0; i < excludes.length; i++) {
                walker.addExclude(excludes[i]);
            }
        }
    }

    /**
     * Add some file inclusion patterns
     *
     * @param excludes array of file inclusion patterns
     */
    public void addIncludes(String[] includes)
    {
        if (includes != null) {
            for (int i = 0; i < includes.length; i++) {
                walker.addInclude(includes[i]);
            }
        }
    }

    public void addLibraryWalkListener(ClassWalkListener listener)
    {
        walker.addDirectoryWalkListener(new LibraryWalkListenerWrapper(listener, debugMode));
    }

    /**
     * Add a file exclusion pattern
     *
     * @param exclude the file exclusion pattern
     */
    public void addExclude(String exclude)
    {
        walker.addExclude(exclude);
    }

    /**
     * Add a file inclusion pattern
     *
     * @param exclude the file inclusion pattern
     */
    public void addInclude(String include)
    {
        walker.addInclude(include);
    }

    /**
     * Set the debug mode.
     *
     * @param debugEnabled the debug mode. When true, debug message are provided to the listeners.
     */
    public void setDebugMode(boolean debugEnabled)
    {
        debugMode = debugEnabled;
        walker.setDebugMode(debugEnabled);
    }

    /**
     * Return the base directory that is (about to be) walked.
     *
     * @return the base directory that is (about to be) walked.
     */
    public File getBaseDir()
    {
        return walker.getBaseDir();
    }

    /**
     * Return an array containing the current exclude patterns
     *
     * @return an array containing the current exclude patterns
     */
    public List getExcludes()
    {
        return walker.getExcludes();
    }

    /**
     * Return an array containing the current include patterns
     *
     * @return an array containing the current include patterns
     */
    public List getIncludes()
    {
        return walker.getIncludes();
    }

    /**
     * Start scanning from the base directory, walking through all selected directories and files
     */
    public void scan()
    {
        walker.scan();
    }

    /**
     * Set the base directory to walked.
     *
     * @param baseDir the base directory to walked.
     */
    public void setBaseDir(File baseDir)
    {
        walker.setBaseDir(baseDir);
    }

    /**
     * Set the list of exclusion patterns
     *
     * @param entries the list of exclusion patterns
     */
    public void setExcludes(List entries)
    {
        walker.setExcludes(entries);
    }

    /**
     * Set the list of inclusion patterns
     *
     * @param entries the list of inclusion patterns
     */
    public void setIncludes(List entries)
    {
        walker.setIncludes(entries);
    }
}
