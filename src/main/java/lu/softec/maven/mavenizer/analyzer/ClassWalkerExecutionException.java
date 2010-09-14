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
 * Runtime exception that is thrown while the scanner is in action to return error issues
 *
 * This is a checked exception used to convert the unchecked exception {@link lu.softec.maven.mavenizer.analyzer.ClassWalkerRuntimeException}
 */
public class ClassWalkerExecutionException extends Exception
{
    private File file;

    public ClassWalkerExecutionException()
    {
        super();
    }

    public ClassWalkerExecutionException(String message)
    {
        super(message);
    }

    public ClassWalkerExecutionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ClassWalkerExecutionException(Throwable cause)
    {
        super(cause);
    }

    public ClassWalkerExecutionException(File file)
    {
        super();
        this.file = file;
    }

    public ClassWalkerExecutionException(File file, String message)
    {
        super(message);
        this.file = file;
    }

    public ClassWalkerExecutionException(File file, Throwable cause)
    {
        super(cause);
        this.file = file;
    }

    public ClassWalkerExecutionException(File file, String message, Throwable cause)
    {
        super(message, cause);
        this.file = file;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }
}
