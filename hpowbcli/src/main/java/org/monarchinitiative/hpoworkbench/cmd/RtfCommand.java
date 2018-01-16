package org.monarchinitiative.hpoworkbench.cmd;


import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.monarchinitiative.hpoworkbench.rtf.Hpo2Rtf;

/**
 * A command class to coordinate the production and output of an RTF file containing information about
 * a subhierarchy of the HPO.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.0
 */
public class RtfCommand extends HPOCommand  {
    private static Logger LOGGER = Logger.getLogger(NeurologyCommand.class.getName());

    private String hpopath=null;

    private String DEFAULT_OUTPUTNAME="hpotest.rtf";
    private static String DEFAULT_START_TERM="HP:0000118";


    private String startTerm=DEFAULT_START_TERM;
    private String IMMUNOLOGY_STAERT_TERM="HP:0002715";


    Hpo2Rtf hpo2rtf=null;


    public RtfCommand(String dir, String startTerm) {
        hpopath=dir;
        startTerm=startTerm;
    }


    @Override
    public void run() {
        LOGGER.trace("running RTF command");

        try {
            this.hpo2rtf = new Hpo2Rtf(DEFAULT_OUTPUTNAME);
            hpo2rtf.writeRtfHeader();
            hpo2rtf.writeTableFromTerm(IMMUNOLOGY_STAERT_TERM);
            hpo2rtf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
















    @Override
    public String getName() { return "rtf";}
}
