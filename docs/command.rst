Command-line tools
==================

HPO Workbench provides a number of command-line tools in the ``hpowbcli`` package. These are functionalities
that may be included in the HPO Workbench app at a later point or in some cases are intended for working
with low-level files.






Downloading HPO data
~~~~~~~~~~~~~~~~~~~~
Most of the commands described in this section use the ``hp.obo`` or the
``phenotype_annotation.tab`` files. The following command can be used to download
both files (the default download directory is ``data``, which can also be used by any of the
following commands). ::

    $ java -jar HPOWorkbench.jar download  [-d <directory>]

``directory`` is the name of directory to which HPO data will be downloaded (default:"data")


Printing open issues to Word
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
We may want to send a summary of all open issues with our questions to our clinical collaborators in
the form of a Word document. For instance, this would be the command to get a list of all issues
with the label ``cardiovascular``. ::

    $ java -jar HPOWorkbench.jar git -g cardiovascular

We are using the `Apache POI <https://poi.apache.org/>` Java library to create the word documents, and the overall
formatting could probably be made nicer.

Currently, the GitHub API seems to limit the number of issues that can be requested to 30, so this function will only
work for up to 30 open issues.



CSV
~~~
This command makes a comma-separated value file that contains all of the terms in the HPO. It is similar to the
creation of an Excel file from the main app, and in the future, it will be offered as an option in the app. ::

    $ java -jar HPOWorkbench.jar csv [-h <hpo>]

``hpo`` is the path to hp.obo file


Rich Text Format (RTF) Output
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
This function is currently not complete!

We are working on an RTF output format that puts all HPO terms into an RTF table that can be imported into
Word and used to make corrections using Word's ``track changesd`` tool. The RTF format is currently not
tested. ::

    $ java -jar HPOWorkbench.jar rtf -h <hpo> -t <start-term>

The document will start off at ``start-term`` (e.g., using a start term of Ventricular septal defect would
produce a table with that term and all of its descendents). Use the HP id (e.g., HP:0000123).


Counting annotations
~~~~~~~~~~~~~~~~~~~~
The situation is that we have a list of disease annotations (which could be ``phenotype_annotation.tab`` or
a smaller selection of annotations) and an HPO term. We would like to find out the total number of annotations
to the term or any of its ancestors. ::

    $ java -jar HPOWorkbench.jar countfreq [-h <hpo.obo>] [-a <pheno_annot.tab>] -t <term id>

