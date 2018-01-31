.. HPO Workbench documentation master file, created by
   sphinx-quickstart on Sun Sep 24 12:02:05 2017.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

HPO Workbench
===============================
.. toctree::
   :maxdepth: 2
   :caption: Contents:

   installation
   Browsing HPO Terms and Annotated Diseases <browsing>
   Suggest additions and corrections as GitHub issues <github>
   Exporting HPO data as an Excel file <excel>
   Using the very latest HPO version <latest>
   HPO Workbench from the command line <command>

HPO Workbench
~~~~~~~~~~~~~
HPO Workbench is a Java app designed as a browser for HPO terms and annotated diseases. HPO Workbench
has a series of functions that can be used by collaborators to review and make suggestions for extending,
correcting, or otherwise revising the HPO or the disease annotations.

HPO Workbench can be used to browser the HPO ontology; information about each term is shown including the
definition, comments, and synonyms (if any). If the term is used to annotate diseases in the HPO database,
then a list of diseases is shown. Additionally, diseases can be visualized, and all HPO terms that annotate
that disease are shown according to affected system.

HPO Workbench can also be used to suggest additions, corrections, or revisions to the HPO. To do this, users
can navigate to the part of the HPO they would like to revise, and mark a term and suggest new child terms or
other revisions. Also, new annotations for diseases can be suggested. To do so, users must enter their
GitHub name and password (only once per session), and the suggestions are sent to the HPO GitHub tracker, where they
will be registered under your id (so that you will get feedback via Email about your suggestion).

HPO Workbench can also be used to export Excel files that represent the HPO hierarchy by means of indentation of
columns. These excel files are meant to be used to record suggestions.

Instructions for these three main use cases of HPO Workbench can be found on the following pages.


GitHub repository
~~~~~~~~~~~~~~~~~
The HPO Workbench GitHub repository can be found here: https://github.com/TheJacksonLaboratory/HPOworkbench


 



