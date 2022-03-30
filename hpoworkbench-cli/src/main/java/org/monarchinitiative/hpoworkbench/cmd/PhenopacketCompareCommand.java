package org.monarchinitiative.hpoworkbench.cmd;


import org.apache.commons.lang3.NotImplementedException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.Disease;
import picocli.CommandLine;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

import static org.apache.commons.math3.stat.inference.TestUtils.chiSquare;
import static org.apache.commons.math3.stat.inference.TestUtils.chiSquareTest;



/**
 * To use this command, run the simonsq app to create a directory with
 * phenopackets. Pass the location of the identifier with the --identifier argument
 * (e.g., disease
 *  "diseases": [{
 *     "term": {
 *       "id": "MONDO:0013847",
 *       "label": "chromosome 16p11.2 duplication syndrome"
 *     }
 *   }],
 *   )
 *
 *   and pass --groupA MONDO:0013847 --groupB MONDO:0013848 etc.
 *
 * java -jar target/HPOworkbench.jar  compare
 * --phenopackets
 * /Users/robinp/IdeaProjects/simonsq/TMP
 * --identifier
 * disease
 * --groupA
 * MONDO:0013847
 * --groupB MONDO:0013267
 * --hpo <path>/hp.json
 */
@CommandLine.Command(name = "phenopacketCompare",
        mixinStandardHelpOptions = true,
        description = "Compare two groups of patients in a colection of phenopackets")
public class PhenopacketCompareCommand implements Callable<Integer> {

    ///Users/robinp/IdeaProjects/simonsq
    @CommandLine.Option(names = {"-p", "--phenopackets"},required = true, description = "TSV file with terms")
    private String phenopacketDirectory;

    @CommandLine.Option(names = {"-i", "--identifier"},required = true, description = "name of phenopacket element that has the group name")
    private String identifier;

    @CommandLine.Option(names = {"-a", "--groupA"},required = true, description = "name of groupA")
    private String groupA;

    @CommandLine.Option(names = {"-b", "--groupB"},required = true, description = "name of groupA")
    private String groupB;

    @CommandLine.Option(names={"--hpo"}, description = "path to hp.json")
    private String hpoJsonPath = null;

    @Override
    public Integer call() throws Exception {
        List<File> files = getAllPhenopacketFiles(new File(this.phenopacketDirectory));
        List<Phenopacket> packets = getPhenopackets(files);
        List<Phenopacket> packetsA = getGroupA(packets);
        List<Phenopacket> packetsB = getGroupB(packets);
        Ontology hpo = null;
        if (hpoJsonPath != null) {
            hpo = OntologyLoader.loadOntology(new File(this.hpoJsonPath));
        }
        System.out.printf("Group A n=%d, Group B n=%d\n", packetsA.size(), packetsB.size());
        Set<TermId> allHpoTermIds = getAllHpos(packets);
        for (TermId hpoId : allHpoTermIds) {
            long[][] counts = getCounts(hpoId, packetsA, packetsB);
            double chiSquare = chiSquare(counts);
            double chiSquareP = chiSquareTest(counts);
            String hpoOut = hpoId.getValue();
            if (hpo != null) {
                Optional<String> labelOpt = hpo.getTermLabel(hpoId);
                if (labelOpt.isPresent()) {
                    String label = labelOpt.get();
                    hpoOut = String.format("%s (%s)", label, hpoOut);
                }
            }
            System.out.printf("%s: %s --  chi2 %.2f p-value %e\n",
                    hpoOut, outputAandB(counts),
                    chiSquare, chiSquareP);
        }
        return null;
    }



    public String outputAandB(long[][] counts) {
       long groupAannotated =  counts[0][0]; // group A with HPO
        long groupAnotAnnotated = counts[0][1] ; // group A with HPO
        long groupBannotated =counts[1][0]; // group A with HPO
        long groupBnotAnnotated = counts[1][1]; // group A with HPO
        long totalA = groupAannotated + groupAnotAnnotated;
        double aPerc =100.0* (double) groupAannotated/totalA;
        long totalB = groupBannotated + groupBnotAnnotated;
        double bPerc = 100.0* (double) groupBannotated/totalB;
        return String.format("%s: %d/%d (%.1f%%); %s: %d/%d (%.1f%%)",
                this.groupA, groupAannotated, totalA, aPerc, this.groupB, groupBannotated, totalB, bPerc);

    }



    private long[][] getCounts(TermId hpoId, List<Phenopacket> packetsA, List<Phenopacket> packetsB) {
        long[][]  counts = new long[2][2];
        // need to get counts for group A and B with/without HPO term annotation
        long groupAtotal = packetsA.size();
        long groupBtotal = packetsB.size();
        long groupAannotated = getAnnotatedCount(hpoId, packetsA);
        long groupAnotAnnotated = groupAtotal - groupAannotated;
        long groupBannotated = getAnnotatedCount(hpoId, packetsB);
        long groupBnotAnnotated = groupBtotal - groupBannotated;
        counts[0][0] = groupAannotated; // group A with HPO
        counts[0][1] = groupAnotAnnotated; // group A with HPO
        counts[1][0] = groupBannotated; // group A with HPO
        counts[1][1] = groupBnotAnnotated; // group A with HPO

        return counts;
    }

    long getAnnotatedCount(TermId hpoId, List<Phenopacket> packets) {
        long annotated = 0;
        String idAsString = hpoId.getValue();
        for (Phenopacket phenopacket: packets) {
            for (var pf : phenopacket.getPhenotypicFeaturesList()) {
                String id = pf.getType().getId();
                if (id.equals(idAsString)) {
                    annotated++;
                    continue;
                }
            }
        }
        return annotated;
    }


    Set<TermId> getAllHpos(List<Phenopacket> packets) {
        Set<TermId> hpoTermSet = new HashSet<>();
        for (Phenopacket packet : packets) {
            for (var pf : packet.getPhenotypicFeaturesList()) {
                String id = pf.getType().getId();
                TermId hpoId = TermId.of(id);
                hpoTermSet.add(hpoId);
            }
        }
        return hpoTermSet;
    }


    List<Phenopacket> getGroupA(List<Phenopacket> packets) {
        return getGroup(packets, this.groupA);
    }


    List<Phenopacket> getGroupB(List<Phenopacket> packets) {
        return getGroup(packets, this.groupB);
    }


    List<Phenopacket> getGroup(List<Phenopacket> packets, String id) {
        if (! this.identifier.equals("disease")) {
            throw new NotImplementedException("Only diseasec comparison implemented so far");
        }
        // filter on the disease corresponding to id
        List<Phenopacket> filtered = new ArrayList<>();
        for (Phenopacket ppacket : packets) {
            if (ppacket.getDiseasesList().size() != 1) {
                System.err.println("[ERROR] Size of disease list was " + ppacket.getDiseasesList().size());
                continue;
            }
            Disease disease = ppacket.getDiseasesList().get(0);
            String diseaseId = disease.getTerm().getId();
            if (diseaseId.equals(id)) {
                filtered.add(ppacket);
            }
        }
        return filtered;
    }



    List<Phenopacket> getPhenopackets(List<File> files) {
        List<Phenopacket> packets = new ArrayList<>();
        JSONParser parser = new JSONParser();
        for (File f : files) {
            try {
                Object obj = parser.parse(new FileReader(f));
                JSONObject jsonObject = (JSONObject) obj;
                String phenopacketJsonString = jsonObject.toJSONString();
                Phenopacket.Builder phenoPacketBuilder = Phenopacket.newBuilder();
                com.google.protobuf.util.JsonFormat.parser().merge(phenopacketJsonString, phenoPacketBuilder);
                Phenopacket v2phenopacket = phenoPacketBuilder.build();
                packets.add(v2phenopacket);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

        }
        return packets;
    }


    public List<File> getAllPhenopacketFiles(final File folder) {
        List<File> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.getAbsolutePath().endsWith(".json")) {
                files.add(fileEntry);
            }
        }
        return files;
    }


}
