
Installing and running HPO Workbench
====================================
The following sections explain how to install HPO Workbench on Mac, Windows, and linux. The final section explains
how to setup the program (the HPO data files need to be downloaded upon initial use of the program).


Mac OSX
~~~~~~~
Download the latest version of ``HPOWorkbench.jar`` from https://github.com/TheJacksonLaboratory/HPOworkbench/releases/.
You can run the program by double-clicking on this file (Prerequisite: Java version 8 or better must be installed
on your Mac). You can also start the app from the command line as follows. ::

    $ java -jar HPOWorkbench.jar


Windows
~~~~~~~
Download the latest version of ``HPOWorkbench.exe`` from https://github.com/TheJacksonLaboratory/HPOworkbench/releases/.
You can run the program by double-clicking on this file (Prerequisite: Java version 8 or better must be installed
on your machine).


Linux
~~~~~
Linux users can follow the instructions given above for Macintosh. Alternatively, they
can build the HPO Workbench from source. To do so,
clone the GitHub repository, and build HPO Workbench using maven. ::


    $ git clone https://github.com/TheJacksonLaboratory/HPOworkbench.git
    $ cd HPOworkbench
    $ mvn clean package

This will create an executable jar file. It may be convenient to move the file to the current working directory (or someplace on
the PATH).  ::

    $ mv hpowbgui/target/HpoWorkbench.jar .
    $ java -jar HpoWorkbench.jar


