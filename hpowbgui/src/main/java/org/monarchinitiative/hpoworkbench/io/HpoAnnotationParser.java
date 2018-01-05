package org.monarchinitiative.hpoworkbench.io;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermPrefix;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.github.phenomics.ontolib.ontology.data.TermPrefix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * The purpose of this class is to parse the phenotype_annotation.tab file in order to give the user
 * and overview of the diseases annotated to any given HPO term. For now we will not provide an overview
 * that starts with diseases and show the HPO terms (ToDo add this once the functionality has been added
 * to the main branch of ontolib).
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.1
 */
public class HpoAnnotationParser {
    private static final Logger logger = LogManager.getLogger();
    private final String pathToPhenotypeAnnotationTab;
    private final HpoOntology ontology;
    private final TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");
    public HpoAnnotationParser(String path,HpoOntology onto) {
        pathToPhenotypeAnnotationTab=path;
        ontology=onto;
    }


    private TermId string2TermId(String termstring) {
        if (termstring.startsWith("HP:")) {
            termstring=termstring.substring(3);
        }
        if (termstring.length()!=7) {
            logger.error("Malformed termstring: "+termstring);
            return null;
        }
        TermId tid = new ImmutableTermId(HP_PREFIX,termstring);
        if (! ontology.getAllTermIds().contains(tid)) {
            logger.error("Unknown TermId "+tid.getIdWithPrefix());
            return null;
        }
        return tid;
    }

    public  Map<TermId,List<String>> parse() {
        Map<TermId,List<String>> annotmap=new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(pathToPhenotypeAnnotationTab));
            String line=null;
            while ((line=br.readLine())!=null) {
                String A[]= line.split("\t");
                if (A.length<5) {
                    logger.error(String.format("Annotation line with less than 5 fields, \n%s",line));
                    continue;
                }
                String db=A[0];
                String dbId=A[1];
                String diseasename=A[2];
                int i = diseasename.indexOf(";");
                if (i>0) diseasename=diseasename.substring(0,i); // other synonyms follow the first ";"
                String HPOid=A[4];
                TermId id=string2TermId(HPOid);
                // get all ancestors of the term (annotation propagation rule)
                Set<TermId> ancs =ontology.getAncestorTermIds(id);
                for (TermId t:ancs) {
                    if (!annotmap.containsKey(t)) {
                        annotmap.put(t, new ArrayList<String>());
                    }
                    List<String> diseaselist=annotmap.get(t);
                    diseaselist.add(String.format("%s|%s:%s",diseasename,db,dbId));
                }


               // logger.trace(String.format("Adding disease %s [%s:%s] to term %s",diseasename,db,dbId,id.getIdWithPrefix()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        return annotmap;
    }

}
