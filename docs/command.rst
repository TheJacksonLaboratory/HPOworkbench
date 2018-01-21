Command-line tools
==================

HPO Workbench provides a number of command-line tools in the ``hpowbcli`` package. These are functionalities
that may be included in the HPO Workbench app at a later point or in some cases are intended for working
with low-level files.



Converting old "small files" to new format
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See Google doc that was sent around the phenotype list for background.
The HPO project is updating the rare disease annotation files to add some new features. This document is intended
to explain the process, but we note it is intended for internal use and will be deleted after the conversion has been
carried out.


Running HPO Workbench to perform the conversion
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
To run the demo program, enter the following command. ::

    $ java -jar HPOworkbench.jar convert -h <path to hp.obo> -d <path to rare-diseases/annotated>

Here, <path to rare-diseases/annotated> is the path to the ``annotated`` directory containing the original small files.


Downloading HPO data
~~~~~~~~~~~~~~~~~~~~
Most of the commands described in this section use the ``hp.obo`` or the
``phenotype_annotation.tab`` files. The following command can be used to download
both files (the default download directory is ``data``, which can also be used by any of the
following commands). ::

    $ java -jar HPOWorkbench.jar download  [-d <directory>]
	<directory>: name of directory to which HPO data will be downloaded (default:"data")



CSV
~~~
This command makes a comma-separated value file that contains all of the terms in the HPO. It is similar to the
creation of an Excel file from the main app, and in the future, it will be offered as an option in the app. ::

    $ java -jar HPOWorkbench.jar csv [-h <hpo>]
	<hpo>: path to hp.obo file


Rich Text Format (RTF) Output
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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