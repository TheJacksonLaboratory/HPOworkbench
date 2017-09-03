package org.monarchinitiative.hpoapi.cmd;

import com.github.phenomics.ontolib.formats.hpo.HpoDiseaseAnnotation;
import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import org.apache.log4j.Logger;
import org.monarchinitiative.hpoapi.argparser.ArgumentParserException;
import org.monarchinitiative.hpoapi.io.HPOAnnotationParser;
import org.monarchinitiative.hpoapi.io.HPOParser;

import java.io.File;
import java.util.List;
import java.util.Map;

/** This is a prototype to test usage of the ontolib library. It should be factored into an App.
 *
 */
public class NeurologyCommand extends HPOCommand  {
    private static Logger LOGGER = Logger.getLogger(NeurologyCommand.class.getName());
    private String hpopath=null;
    private String annotpath=null;
    HpoOntology hpoOntology=null;
    List<HpoDiseaseAnnotation>annots=null;



    public NeurologyCommand() {
        inputHPOdata(this.hpopath,this.annotpath);
    }





    private void inputHPOdata(String hpo, String annot) {
        LOGGER.trace(String.format("inputing data with files %s and %s",hpo,annot));
        HPOParser parser = new HPOParser(hpo);
        hpoOntology=parser.getHPO();
        HPOAnnotationParser annotparser=new HPOAnnotationParser(annot);
        annots=annotparser.getAnnotations();
    }




   @Override
    public  void run() {

    }




    @Override
    public String getName() {return "neuro";}

    @Override
    public void setOptions(Map<String,String> mp) throws ArgumentParserException {
        if (mp.containsKey("directory")) {
            this.hpopath=String.format("%s%shp.obo",mp.get("directory"), File.separator);
            this.annotpath=String.format("%s%sphenotype_annotation.tab",mp.get("directory"), File.separator);
        }

    }
}
