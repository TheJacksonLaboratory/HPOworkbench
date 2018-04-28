
Releasing HPO Workbench
=======================
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

After pushing this, create a release from the GitHub page and identify it with this tag. We will use launch4j (http://launch4j.sourceforge.net/) to create a Windows exectuble. This is
largely self-explanatory.

*  We call the output file HPOWorkbench.exe
*  Choose the jar file for the gui
*  Set the minimum JRE to 1.8.0

Clicking on the gear icon will cause launch4j to ask you for a file name for the XML config file (Enter HPOWorkbench).
It will then generate the exe file to the same location. Upload this to the GitHub release page. Also upload the JAR file.