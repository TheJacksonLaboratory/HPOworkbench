# HPOAPI
Human Phenotype Ontology API

A Java programming library for working with HPO and HPO annotation data. Still in a very preliminary phase. 
The app currently can download the hp.obo file and transform it to a TSV file.

```
$ mvn package
$ java -jar hpoapi-cli/target/hpoapi-cli-0.0.2-SNAPSHOT.jar download
$ java -jar hpoapi-cli/target/hpoapi-cli-0.0.2-SNAPSHOT.jar csv
```
This will download hp.api to data/hp.obo (default) and will generate a csv (tsv) file.
