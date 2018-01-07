# HPO Workbench
A Java app for working with the Human Phenotype Ontology (HPO).

A Java app for working with HPO and HPO annotation data. HPO Workbench can be used to get an overview of the HPO by navigating
through the hierarchy and displaying the term definition etc. To start the application enter the following command.
```
$ mvn package
$ java -jar hpowbgui/target/hpowbgui-0.0.3-SNAPSHOT.jar 
```
## Using HPO Workbench
Navigate through the hierarchy of the using the tree browser or
use the autocomplete text field to find the HPO term of your choice.
HPO Workbench will display the ID, definition, comment, and synyonyms 
for the term. If any diseases in the HPO corpus are annotated to 
the term, a list of the diseases will be displayed.

![HPO Workbench screenshot](misc/HPOworkbench.png)

## Creating Excel files to revise or extend the HPO
Users who would like to contribute new terms or other information to the
HPO project and who would prefer to use Excel can use HPO Workbench to
create an Excel file to work with. We recommend using the "Create hierarchical summary"
option. To do so, first navigate to the area of the HPO you would like to 
work with (e.g., Abnormality of thyroid physiology). Clicking
on the "Create hierarchical summary" button will create an Excel file that
contains only the portion of the ontology that starts from this term.
It will suggest the hierarchy of the ontology by indenting child, grandchild,
great-grandchild (etc) terms in columns located further to the right ("indentation by column").
Please create a new column or columns in this file that will contain your comments
and suggestions. You are welcome to contact the HPO team to 
get advice about this before you start (see the HPO Website for email addresses).

## Creating GitHub issues
Users can create an issue with a suggestion for an existing term by navigating to a term they would like to
comment on and clicking on the create suggestion button. Users will need to enter their GitHub username
and password (which will be stored for the duration of the session, so that multiple issues can be created
without having to reenter the GitHub username and password multiple times.
If users would like to suggest a new term, navigate to the proposed parent of the new term, and click on
suggest child term.

## Viewing annotated diseases
The diseases annotated to a given HPO term (including direct and implied annotations) can be viewed in the
browser. If desired, users can limit the diseases to one of the databases, Orphanet, OMIM, or DECIPHER.

## Command line
Some of the functionality is also available in the command line (cli) module
and may be useful for scripting.

## Warning

This project is still in a very early phase, the software is mature enough for demonstration purposes but will be subject to Q/C and testing shortly.
