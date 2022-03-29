package org.monarchinitiative.hpoworkbench.cmd;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;


@CommandLine.Command(name = "count",
        mixinStandardHelpOptions = true,
        description = "Count and compare gene to disease associations.")
public class CountGenes extends HPOCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CountGenes.class);

    private Multimap<TermId, TermId> geneToDiseaseMapPhenol;
    private Multimap<TermId, TermId> geneToDiseaseMapCh;


    @CommandLine.Option(names={"--allgenes"}, required = true)
    String pathToGenesToPhenotypeFile;

    @CommandLine.Option(names = {"--geneinfo"})
    String pathToGeneInfo = "data/Homo_sapiens_gene_info.gz";

    @CommandLine.Option(names = {"--orpha"})
    String pathToOrpha = "data/en_product6.xml";

    @CommandLine.Option(names = {"--mim2gene"})
    String pathToMim2Gene = "data/mim2gene_medgen";

    @Override
    public Integer call() {
        LOGGER.trace("Count genes command");
        parseGeneToPhenotypeFile(pathToGenesToPhenotypeFile);
        parsePhenolFiles();
        compare();
        return 0;
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
        //HpoAssociationParser parser = new HpoAssociationParser(pathToGeneInfo, pathToMim2Gene, pathToOrpha, annotpath, ontology);
        this.geneToDiseaseMapPhenol = null;//parser.getGeneToDiseaseIdMap();
    }








    private void parseGeneToPhenotypeFile(String path) {
        Set<Pair> geneDiseasePairSet = new HashSet<>();
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
                geneDiseasePairSet.add(new Pair(geneTid, diseaseTid));
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


}
