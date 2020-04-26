package org.monarchinitiative.hpoworkbench.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.annotations.assoc.HpoAssociationParser;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Parameters(commandDescription = "count.  Count and compare gene to disease associations")
public class CountGenes extends HPOCommand {
    private static final Logger LOGGER = Logger.getLogger(CountGenes.class.getName());

    private Multimap<TermId, TermId> geneToDiseaseMapPhenol;
    private Multimap<TermId, TermId> geneToDiseaseMapCh;

    private Set<Pair> geneDiseasePairSet;


    @Parameter(names={"--allgenes"}, required = true)
    String pathToGenesToPhenotypeFile;

    @Parameter(names = {"--geneinfo"})
    String pathToGeneInfo = "data/Homo_sapiens_gene_info.gz";

    @Parameter(names = {"--orpha"})
    String pathToOrpha = "data/en_product6.xml";

    @Parameter(names = {"--mim2gene"})
    String pathToMim2Gene = "data/mim2gene_medgen";


    public void run() {
        LOGGER.trace("Count genes command");
        parseGeneToPhenotypeFile(pathToGenesToPhenotypeFile);
        parsePhenolFiles();
        compare();
    }




    private void compare() {
        for (TermId geneId : geneToDiseaseMapPhenol.keys()) {
            if (! geneToDiseaseMapCh.containsKey(geneId)) {
                System.out.printf("[INFO] Could not find %s in Ch map.\n", geneId.getValue());
            }
        }

        for (TermId geneId : geneToDiseaseMapCh.keys()) {
            if (! geneToDiseaseMapPhenol.containsKey(geneId)) {
                System.out.printf("[INFO] Could not find %s in Phenol map.\n", geneId.getValue());
            }
        }
    }



    private void parsePhenolFiles() {
        Ontology ontology = OntologyLoader.loadOntology(new File(hpopath));
        HpoAssociationParser parser = new HpoAssociationParser(pathToGeneInfo, pathToMim2Gene, pathToOrpha, annotpath, ontology);
        this.geneToDiseaseMapPhenol = parser.getGeneToDiseaseIdMap();
    }








    private void parseGeneToPhenotypeFile(String path) {
        geneDiseasePairSet = new HashSet<>();
        ImmutableSet.Builder<Pair> builder = new ImmutableSet.Builder<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathToGenesToPhenotypeFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#"))
                    continue;
                String [] fields = line.split("\t");
                if (fields.length < 9) {
                    System.err.println("[ERROR] Malformed line with less than 9 fields: " + line);
                    continue;
                }
                String geneid = fields[0];
                TermId geneTid = TermId.of("NCBIGene", geneid);
                String diseaseid = fields[8];
                TermId diseaseTid = TermId.of(diseaseid);
                this.geneDiseasePairSet.add(new Pair(geneTid, diseaseTid));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("[INFO] Parsed %d pairs of genes/diseases.\n", geneDiseasePairSet.size());
        geneToDiseaseMapCh = ArrayListMultimap.create();
        for (Pair p : geneDiseasePairSet) {
            geneToDiseaseMapCh.put(p.geneid, p.diseaseid);
        }
    }


    static class Pair {
        TermId geneid;
        TermId diseaseid;

        public Pair(TermId t1, TermId t2) {
            geneid = t1;
            diseaseid = t2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return Objects.equals(geneid, pair.geneid) &&
                    Objects.equals(diseaseid, pair.diseaseid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(geneid, diseaseid);
        }
    }

    @Override
    public String getName() {
        return "count";
    }
}
