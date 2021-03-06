Using HPO Workbench with the latest hp.obo version
==================================================

The ``hp-edit.owl`` file contains all of the latest changes. However, the version of the
``hp.obo`` file that HPO Workbench downloads via its Edit menu does not necessarily have
the latest changes, because the later file is released on a monthly basis. To get the very
latest version, it is possible to compile the hp.obo file locally and import the local file
into HPO Workbench. This step requires some familiarity with the command line; if you are
not familiar with this kind of work and need the latest hp.obo version, please contact the
HPO team.

To do so, you will need to download the HPO github repository as well as the repository for
owltools. The following text assumes that you download both repositories to the same place, e.g.,
to some directory called GIT on your local drive.

Downloading the HPO github repository
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The HPO GitHub repository can be found here: https://github.com/obophenotype/human-phenotype-hpoOntology.
Clone the repository with the following command. ::

    $ git clone https://github.com/obophenotype/human-phenotype-hpoOntology


Building owltools
~~~~~~~~~~~~~~~~~

We need to download and build owltools. The GitHub repository can be found here: https://github.com/owlcollab/owltools.git.
Clone the repository and build it as follows. ::

    $ git clone https://github.com/owlcollab/owltools.git
    $ cd owltools
    $ ./build.sh


Building hp.obo
~~~~~~~~~~~~~~~
In order to build hp.edit, we will use the Makefile located in ``human-phenotype-hpoOntology/src/hpoOntology/``. This makefile
needs access to owltools. You can either add the paths to your environment (e.g., using the ``.bashrc`` file) or you can
add the path to the environment of your current shell. The latter solution is shown here. ::

    $ cd src/hpoOntology
    $ export PATH=${PATH}:../../../owltools/OWLTools-Oort/bin/:../../../owltools/OWLTools-Runner/bin/
    $ make

This will create a new version of ``hp.obo``.


Using the new version of hp.obo in HPO Workbench
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
After you have created the up-to-date version of ``hp.obo``, import it using the ``Edit`` menu of
HPO Workbench.


.. figure:: img/importLocalHpObo.png
  :scale: 100 %
  :alt: HPO Workbench - import local hp.obo
  :align: center

After this, the browsing functions of HPO Workbench will use the new version of ``hp.obo`` (for the current session
only).
