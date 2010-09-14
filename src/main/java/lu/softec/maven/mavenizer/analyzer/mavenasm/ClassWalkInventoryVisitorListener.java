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

import org.apache.maven.plugin.logging.Log;

import lu.softec.maven.mavenizer.analyzer.dependency.ClassDependencyAnalyser;

/**
 * Specialized {@link AbstractClassWalkVisitorListener} using a {@link ClassInventoryVisitor} to visit classes and
 * logging informational and error messages into a {@link org.apache.maven.plugin.logging.Log}.
 */
public class ClassWalkInventoryVisitorListener extends AbstractClassWalkVisitorListener
{
    protected final Log logger;

    public ClassWalkInventoryVisitorListener(ClassDependencyAnalyser analyser, Log logger)
    {
        super(new ClassInventoryVisitor(analyser));
        this.logger = logger;
    }

    public void libraryWalkStarted(File file)
    {
        logger.info("Listing classes from " + file.getAbsolutePath());
    }

    public void libraryWalkFileClosed()
    {
        logger.info("Found " + getClassCount() + " classes in file " + getCurrentFile().getAbsolutePath());
        super.libraryWalkFileClosed();
    }

    public void libraryWalkFinished()
    {
        logger.info("Listing done.");
    }

    public void debug(String s)
    {
        logger.debug(s);
    }
}
