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

/**
 * Interface to walk through classes from a .jar files.
 *
 * One or more {@link ClassWalkListener} could be associated to implementing class to perform actions on the walked
 * classes.
 *
 * This works like the {@link org.codehaus.plexus.util.DirectoryWalker} works on files, but for classes inside jar
 * files.
 */
public interface ClassWalker
{
    /**
     * Add a class walk listener to receive events from this walker
     *
     * @param listener the listener to be added
     */
    void addClassWalkListener(ClassWalkListener listener);

    /**
     * Walk over classes
     */
    void scan() throws ClassWalkerExecutionException;

    /**
     * Set the debug mode to receive debug events.
     *
     * @param debugEnabled the debug mode. When true, debug message are provided to the listeners.
     */
    void setDebugMode(boolean debugEnabled);
}
