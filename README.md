# HPO Workbench
A Java app for working with the Human Phenotype Ontology (HPO).

A Java app for working with HPO and HPO annotation data. HPO Workbench can be used to get an overview of the HPO by navigating
through the hierarchy and displaying the term definition etc. To start the application enter the following command.
```
$ mvn package
$ java -jar hpowbgui/target/hpowbgui-0.0.3-SNAPSHOT.jar 
```
Excel files containing all terms of the HPO or containing a "hierarchical view" of the HPO and starting from a specific term of the HPO can be exported. Especially the latter may be useful for clinical colleagues who would like to provide feedback on current HPO terms (add a column with comments or corrections etc).

Some of the functionality is also available in the cli module.

This project is still in a very early phase, the software is mature enough for demonstration purposes but will be subject to Q/C and testing shortly.
