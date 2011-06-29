Mavenizer Plugin
================

#### Mavenize standard library binaries ####

The Mavenizer Plugin aims to facilitate the deployment of non-maven binaries
into a maven repository while keeping the whole benefits of Maven dependency
management.

It provides a special maven livecycle and some specific goals that helps
analysing, installing and deploying JAR files into a Maven repository associated
with the appropriate POM definitions. The analysis of the dependencies directly
from the binaries helps in creating useful POM definition containing a correct
list of dependencies required by these non-maven libraries without requiring
access to their sources, and very quickly compared to a complete conversion of
the underlying project to Maven.

### Targeted platforms ###

Mavenizer Plugin is build and tested on Maven2 and should run on any
Maven2 compatible platform.

Contributing to Mavenizer Plugin
--------------------------------

Fork our repository on GitHub and submit your pull request.

Documentation
-------------

The full documentation is available on [mavenizer.softec.lu](http://mavenizer.softec.lu)

License
-------

Mavenizer Plugin by [SOFTEC sa](http://softec.lu) is license under
the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
If you need another license, please [contact us](mailto:support@softec.lu)
with an description of your expected usage, and we will propose you an
appropriate agreement on a case by case basis.
