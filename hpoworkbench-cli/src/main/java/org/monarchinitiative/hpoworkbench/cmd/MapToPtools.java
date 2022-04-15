package org.monarchinitiative.hpoworkbench.cmd;

import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import picocli.CommandLine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "ptools",
        mixinStandardHelpOptions = true,
        description = "Match HPO terms to a list of candidates.")
public class MapToPtools extends  HPOCommand implements Callable<Integer> {


    @Override
    public Integer call() throws Exception {
        if (hpopath==null) {
            hpopath = this.downloadDirectory + File.separator + "hp.json";
        }
        Ontology hpo = OntologyLoader.loadOntology(new File(hpopath));
        //HP:0012836
        TermId spatialPattern = TermId.of("HP:0012836");
        Set<TermId> termIdSet = OntologyAlgorithm.getDescendents(hpo, spatialPattern);
        Map<String, String> termMap = new HashMap<>();
        for (TermId tid : termIdSet) {
            Optional<String> opt = hpo.getTermLabel(tid);
            if (opt.isPresent()) {
                String label = opt.get();
                String id = tid.getValue();
                termMap.put(id, label);
                //  private static final OntologyClass RIGHT = OntologyClassBuilder.ontologyClass("HP:0012834", "Right");
                String out = String.format("private static final OntologyClass %s = OntologyClassBuilder.ontologyClass(\"%s\", \"%s\");",
                      label.toUpperCase().replaceAll(" ","_"),  id, label);
                label = label.replaceAll(" ","_").replaceAll("-","_").replaceAll("\\(","").replaceAll("\\)","");
                termMap.put(id, label);
                System.out.println(out);
            }
        }
        for (var e: termMap.entrySet()) {
            // public static OntologyClass right() {
            //        return RIGHT;
            //    }
            String functionName = e.getValue();
            // lower-case first letter
            String [] parts = functionName.split("_");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i<parts.length;i++) {
                String p = parts[i];
                String first;
                if (i==0) {
                    first = p.substring(0, 1).toLowerCase();
                } else {
                    first = p.substring(0, 1).toUpperCase();
                }
                p = first + p.substring(1);
                sb.append(p);
            }
            functionName = sb.toString();
            String out = String.format("public static OntologyClass %s() { return %s; }",
                    functionName , e.getValue().toUpperCase() );
            System.out.println(out);
        }
        return 0;
    }
}
