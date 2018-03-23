package org.monarchinitiative.hpoworkbench.cmd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.io.MondoParser;
import org.monarchinitiative.hpoworkbench.word.Hpo2Word;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.generic.GenericRelationship;
import org.monarchinitiative.phenol.formats.generic.GenericTerm;
import org.monarchinitiative.phenol.ontology.data.Dbxref;
import org.monarchinitiative.phenol.ontology.data.ImmutableOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Extract descriptive statistics about a Mondo.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class MondoCommand extends HPOCommand {
    private static final Logger logger = LogManager.getLogger();
    private final String pathToMondoFile;

    public MondoCommand(String path) {
        pathToMondoFile=path;
    }

    @Override
    public void run() {
        logger.trace("running Mondo command");

        try {
            MondoParser parser = new MondoParser(pathToMondoFile);
            ImmutableOntology<GenericTerm,GenericRelationship> mondo = parser.parse();
            int n = mondo.getTermMap().size();
            logger.trace("Parsed Mondo ontology and found "+ n+ "terms");
            Map<TermId,GenericTerm> termmap = mondo.getTermMap();
            int c=0;
            for (TermId tid : termmap.keySet()) {
                if (c++>5) break;
                GenericTerm gt = termmap.get(tid);
                logger.trace(String.format("%s [%s; %s] xrefs are ",gt.getName(),gt.getId(),gt.getDefinition() ));
                List<Dbxref> xrefs = gt.getXrefs();
                for (Dbxref x : xrefs) {
                    logger.trace("\t%s",x.getName());
                }
            }
        } catch (PhenolException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() { return "mondo";}
}
