package lu.softec.maven.mavenizer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;/*
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

/**
 * Delete the mavenizer analysis results (xml and pom)
 *
 * @goal clean-analyze
 */
public class CleanAnalysisMojo extends AbstractPomMavenizerMojo
{
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        deleteTargetFile(getMavenizerConfigFile());
        deleteTargetDirectory(getPomBaseDir());
        getLog().info("Mavenizer analysis cleaned.");
    }
}
