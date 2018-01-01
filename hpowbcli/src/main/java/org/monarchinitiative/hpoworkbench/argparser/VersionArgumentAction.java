package org.monarchinitiative.hpoworkbench.argparser;


/**
 * Created by peter on 24.06.17.
 */
public class VersionArgumentAction implements ArgumentAction {

    private String version=null;
    VersionArgumentAction(String v) {
        this.version=v;
    }

    public void run(Argument arg) {
        System.out.println("Version: "+version);
        System.exit(0);
    }

}
