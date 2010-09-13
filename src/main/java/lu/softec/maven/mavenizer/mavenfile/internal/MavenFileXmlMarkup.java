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
package lu.softec.maven.mavenizer.mavenfile.internal;

/**
 * Constants for names of tags used by the {@link DefaultMavenFileParser} and the {@link DefaultMavenFileSerializer}
 */
class MavenFileXmlMarkup
{
    static final String ARTIFACTS_TAG = "artifacts";

    static final String DEPENDENCIES_TAG = "dependencies";

    static final String ARTIFACT_TAG = "artifact";

    static final String DEPENDENCY_TAG = "dependency";

    static final String NAME_ATTRIBUTE = "name";

    static final String GROUPID_TAG = "groupid";

    static final String ARTIFACTID_TAG = "artifactid";

    static final String VERSION_TAG = "version";

    static final String CLASSIFIER_TAG = "classifier";

    private MavenFileXmlMarkup()
    {
    }
}
