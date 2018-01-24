
Releasing HPO Workbench
===========================
Summary of procedure to release a version of HPO Workbench.


Preparing the code
~~~~~~~~~~~~~~~~~~
In general, the release numbering should follow Semantic versioning (https://semver.org/). The version reported by
the command line and the GUI is taken from the pom.xml file, and should be updated accordingly. Prepare a jar exectuable
file by ::

    $ mvn clean package

Tagging the release
~~~~~~~~~~~~~~~~~~~
Check the current version as follows. ::

    $ java -jar hpowbcli/target/HPOWorkbench.jar
    Program: HPOWorkbench (Human Phenotype Ontology Workbench)
    Version: 0.1.8
    (...)

The release should get the following tag (using the same version number).

    $ git tag v0.1.8

After pushing this,