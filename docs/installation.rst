
Installing and running HPO Workbench
=====================================
The following sections explain how to install HPO Workbench on Mac, Windows, and linux. The final section explains
how to setup the program (the HPO data files need to be downloaded upon initial use of the program).


Mac OSX
~~~~~~~
**ToDo**


Windows
~~~~~~~
**ToDo**


Linux
~~~~~
We assume that linux users will want to build the HPO Workbench from source. To do so,
clone the GitHub repository, and build HPO Workbench using maven. ::


    $ git clone https://github.com/TheJacksonLaboratory/HPOworkbench.git
    $ cd HPOworkbench
    $ mvn clean package

This will create an executable jar file. It may be convenient to move the file to the current working directory (or someplace on
the PATH).  ::

    $ mv hpowbgui/target/HpoWorkbench.jar .
    $ java -jar HpoWorkbench.jar


