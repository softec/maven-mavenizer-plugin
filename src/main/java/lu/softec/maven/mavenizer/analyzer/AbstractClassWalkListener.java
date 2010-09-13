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

/**
 * Abstract class providing a basic implementation of a ClassWalkListener.
 *
 * Provide convenience method to access the currently processed file and the position of the class in that file.
 * Implement empty methods for minor events.
 *
 * Derived class just need to implements libraryWalkProcessClass.
 *
 * WARNING! Derived class should ensure calling super for libraryWalkFileOpened and libraryWalkFileClosed overridden
 * methods or provide accurate values by overriding getCurrentFile, getClassCount and incrementClassCount.
 */
public abstract class AbstractClassWalkListener implements ClassWalkListener
{
    /**
     * The current class count of classes processed by this listener in the currently processed file.
     */
    private int classCount;

    /**
     * The file containing the class currently processed.
     */
    private File currentFile;

    /**
     * Construct an instance
     */
    public AbstractClassWalkListener()
    {
    }

    /**
     * Implement libraryWalkStarted of interface ClassWalkListener and does nothing.
     *
     * @param file the base directory that is about to be walked
     */
    public void libraryWalkStarted(File file)
    {
        // nothing has to be done here, implemented for convenience of derived class
    }

    /**
     * Implement libraryWalkFileOpened of interface ClassWalkListener to keep track of the currently processed file and
     * the number of classes processed in that file.
     *
     * Ensure calling this method at the start of overriding method any derived class for accuracy of getCurrentFile and
     * getClassPosition or provide accurate values by overriding getCurrentFile, getClassCount and incrementClassCount.
     *
     * @param progress the approximative percentage of progress in the file list behing walked
     * @param file the file that is about to be walked
     */
    public void libraryWalkFileOpened(int progress, File file)
    {
        currentFile = file;
        classCount = 0;
    }

    /**
     * Implement libraryWalkFileOpened of interface ClassWalkListener to keep track of the currently processed file and
     * the number of classes processed in that file.
     *
     * Ensure calling this method at the start of overriding method any derived class.
     */
    public void libraryWalkFileClosed()
    {
        currentFile = null;
        classCount = 0;
    }

    /**
     * Implement libraryWalkFinished of interface ClassWalkListener and does nothing.
     */
    public void libraryWalkFinished()
    {
        // nothing has to be done here, implemented for convenience of derived class
    }

    /**
     * Implement debug of interface ClassWalkListener and does nothing.
     *
     * @param s the text of the debug message
     */
    public void debug(String s)
    {
        // nothing has to be done here, implemented for convenience of derived class
    }

    /**
     * Return the currently processed file
     *
     * @return the currently processed file
     */
    protected File getCurrentFile()
    {
        return currentFile;
    }

    /**
     * Return the number of classes currently processed in the currently processed file
     *
     * @return the number of classes currently processed in the currently processed file
     */
    protected int getClassCount()
    {
        return classCount;
    }

    /**
     * Increment the number of classes currently processed in the currently processed file
     */
    protected void incrementClassCount()
    {
        classCount++;
    }
}
