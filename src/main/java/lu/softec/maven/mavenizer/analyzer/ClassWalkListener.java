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

import java.io.File;
import java.io.InputStream;

/**
 * Interface that should be implemented for listening {@link ClassWalker} events
 */
public interface ClassWalkListener
{
    /**
     * Called once when a walk is started.
     *
     * @param file the base directory that is about to be walked
     */
    void libraryWalkStarted(File file);

    /**
     * Called for each file that is walked through for finding classes
     *
     * @param progress the approximative percentage of progress in the file list behing walked
     * @param file the file that is about to be walked
     */
    void libraryWalkFileOpened(int progress, File file);

    /**
     * Called for each classe found
     *
     * @param progress the approximative percentage of progress in the current walked file. For .class file, always
     * 100%.
     * @param in an input stream to read the class bytecode. This stream SHOULD NEVER be closed.
     */
    void libraryWalkProcessClass(int progress, InputStream in);

    /**
     * Called for each walked file after the file has been closed. This call is normally paired with
     * libraryWalkStarted.
     */
    void libraryWalkFileClosed();

    /**
     * Called once when a walk is finished
     */
    void libraryWalkFinished();

    /**
     * Called when an error occurs during a walk.
     *
     * @param file the current file being walked
     * @param t the throwable containing the current error
     */
    void error(File file, Throwable t);

    /**
     * Called when a debug message is issued
     *
     * @param s the text of the debug message
     */
    void debug(String s);
}
