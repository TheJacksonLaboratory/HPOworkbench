[![Documentation Status](https://readthedocs.org/projects/hpo-workbench/badge/?version=latest)](http://hpo-workbench.readthedocs.io/en/latest/?badge=latest)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/af2d44ee32e148eb92341578e1575e6d)](https://www.codacy.com/app/peter.robinson/HPOworkbench?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=TheJacksonLaboratory/HPOworkbench&amp;utm_campaign=Badge_Grade)
# HPO Workbench
A Java app for working with the Human Phenotype Ontology (HPO).

A Java app for working with HPO and HPO annotation data. HPO Workbench can be used to get an overview of the HPO by navigating
through the hierarchy and displaying the term definition etc. 

Please see the [full documentation](http://hpo-workbench.readthedocs.io/en/latest/).

## Version 2

We are working on a version 2 using Java 17 and Spring Boot. To run HPO Workbench, you will need
to have Java 16 and maven installed and to buld the application with the standard ``mvn package``
command. The app can then be started with
```bazaar
java -jar hpoworkbench-gui/target/HpoWorkbench.jar
```

## Download current release

We are working on releasing native apps for Windows and Mac. 
