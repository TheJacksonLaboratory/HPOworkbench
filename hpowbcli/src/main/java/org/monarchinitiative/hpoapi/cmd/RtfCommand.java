package org.monarchinitiative.hpoapi.cmd;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import org.apache.log4j.Logger;
import org.monarchinitiative.hpoapi.io.HPOAnnotationParser;
import org.monarchinitiative.hpoapi.io.HPOParser;
import org.monarchinitiative.hpoapi.rtf.Hpo2Rtf;

import java.io.File;
import java.util.Map;

public class RtfCommand extends HPOCommand  {
    private static Logger LOGGER = Logger.getLogger(NeurologyCommand.class.getName());

    private String hpopath=null;
    private HpoOntology hpoOntology=null;

    private String startTerm=null;

    Hpo2Rtf hpo2rtf=null;


    public RtfCommand() {
        this.hpo2rtf=new Hpo2Rtf();
    }


    public void setOptions(Map<String,String> mp) {
        if (mp.containsKey("directory")) {
            this.hpopath=String.format("%s%shp.obo",mp.get("directory"), File.separator);
        }
        if (mp.containsKey("startterm")) {
            this.startTerm=mp.get("startterm");
        }

    }
    public void run() {
        LOGGER.trace("running RTF command");
        inputHPOdata(hpopath);

    }

    private void inputHPOdata(String hpo) {

        if (hpo==null)hpo="data/hp.obo";


        LOGGER.trace(String.format("inputting HPO ontology from file %s.",hpo));
        HPOParser parser = new HPOParser(hpo);
        hpoOntology=parser.getHPO();

    }

    public String getName() { return "rtf";}
}
