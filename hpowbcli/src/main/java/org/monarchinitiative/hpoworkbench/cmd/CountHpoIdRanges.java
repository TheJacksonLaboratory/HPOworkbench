package org.monarchinitiative.hpoworkbench.cmd;

import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Count the ranges of the HPO ids. Useful to figure out blocks of unused ranges for new terms
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.1 (May 14, 2020)
 */

@CommandLine.Command(name = "ranges",
        mixinStandardHelpOptions = true,
        description = "Show used ranges for HPO ids.")
public class CountHpoIdRanges extends HPOCommand implements Callable<Integer>  {
    private static final Logger LOGGER = LoggerFactory.getLogger(CountHpoIdRanges.class);
    private Ontology hpoOntology=null;


    @Override
    public Integer call() {
        if (hpopath==null) {
            hpopath = this.downloadDirectory + File.separator + "hp.obo";
        }
        Ontology ontology = OntologyLoader.loadOntology(new File(hpopath));
        List<Integer> idlist = new ArrayList<>();
        for (TermId tid : ontology.getTermMap().keySet()) {
            Integer i = Integer.parseInt(tid.getId());
            idlist.add(i);
        }
        Collections.sort(idlist);
        int N = idlist.size();
        int currentStart = idlist.get(0);
        int currentEnd = currentStart;
        for (int i = 1; i<N; i++) {
            int next = idlist.get(i);
            if (next == currentEnd + 1) {
                currentEnd++;
            } else {
                int diff = currentEnd - currentStart + 1;
                System.out.printf("HP:%07d-HP:%07d (n=%d).\n",currentStart, currentEnd, diff);
                currentStart = next;
                currentEnd = next;
            }
        }
        return 0;
    }
}
