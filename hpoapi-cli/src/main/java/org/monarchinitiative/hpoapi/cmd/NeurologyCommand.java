package org.monarchinitiative.hpoapi.cmd;

import com.github.phenomics.ontolib.formats.hpo.HpoDiseaseAnnotation;
import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermPrefix;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.github.phenomics.ontolib.ontology.data.TermPrefix;
import org.apache.log4j.Logger;
import org.monarchinitiative.hpoapi.argparser.ArgumentParserException;
import org.monarchinitiative.hpoapi.io.HPOAnnotationParser;
import org.monarchinitiative.hpoapi.io.HPOParser;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** This is a prototype to test usage of the ontolib library. It should be factored into an App.
 *
 */
public class NeurologyCommand extends HPOCommand  {
    private static Logger LOGGER = Logger.getLogger(NeurologyCommand.class.getName());
    private String hpopath=null;
    private String annotpath=null;
    HpoOntology hpoOntology=null;
    List<HpoDiseaseAnnotation>annots=null;
    /** Set of all HPO terms that are descencents of {@code Abnormality of the nervous system}, HP:0000707. */
    Set<HpoTerm> neurologyDescendents=null;



    public NeurologyCommand() {

    }





    private void inputHPOdata(String hpo, String annot) {

        if (hpo==null)hpo="data/hp.obo";
        if (annot==null)annot="data/phenotype_annotation.tab";

        LOGGER.trace(String.format("inputting data with files %s and %s",hpo,annot));
        HPOParser parser = new HPOParser(hpo);
        hpoOntology=parser.getHPO();
        HPOAnnotationParser annotparser=new HPOAnnotationParser(annot);
        annots=annotparser.getAnnotations();
    }

    private void getNeurologyTerms() {
        TermPrefix tp =new ImmutableTermPrefix("HP");
        TermId neuroId = new ImmutableTermId(tp, "0000707");
        neurologyDescendents=new HashSet<>() ;
        for (HpoTerm t: this.hpoOntology.getTerms()) {
            Set<TermId> ancs= hpoOntology.getAncestorTermIds(t.getId());
            if (ancs.contains(neuroId)) {
                neurologyDescendents.add(t);
            }
        }
        LOGGER.trace(String.format("We found a totla of %d neurology terms",neurologyDescendents.size()));
    }




   @Override
    public  void run() {
       inputHPOdata(this.hpopath,this.annotpath);
       getNeurologyTerms();
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
