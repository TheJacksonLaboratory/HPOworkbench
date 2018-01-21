Converting old "small files" to new format
==========================================

See Google doc that was sent around the phenotype list for background.
The HPO project is updating the rare disease annotation files to add some new features. This document is intended
to explain the process, but we note it is intended for internal use and will be deleted after the conversion has been
carried out.


Running HPO Workbench to perform the conversion
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
To run the demo program, enter the following command. ::

    $ java -jar HPOworkbench.jar convert -h <path to hp.obo> -d <path to rare-diseases/annotated>

Here, <path to rare-diseases/annotated> is the path to the ``annotated`` directory containing the original small files.