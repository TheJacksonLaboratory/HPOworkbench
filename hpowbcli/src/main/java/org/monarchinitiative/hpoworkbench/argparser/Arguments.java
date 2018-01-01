package org.monarchinitiative.hpoworkbench.argparser;

/**
 * Created by peter on 24.06.17.
 */
public class Arguments {




    public static ArgumentAction version(String v) {
        return new VersionArgumentAction(v);
    }
}
