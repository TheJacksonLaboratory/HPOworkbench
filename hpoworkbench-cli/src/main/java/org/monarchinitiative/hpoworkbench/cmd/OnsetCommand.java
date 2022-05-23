package org.monarchinitiative.hpoworkbench.cmd;

import org.monarchinitiative.phenol.annotations.base.Ratio;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseaseAnnotation;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoader;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaderOptions;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermSynonym;
import picocli.CommandLine;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Output diseases with congenital onset that we infer as such because they are characterized
 * by one or more terms with congenital onset.
 * #DatabaseID	DiseaseName	Qualifier	HPO_ID	Reference	EvidenceOnset	Frequency	Sex	Modifier	Aspect	Biocuration
 * OMIM:619340	Developmental and epileptic encephalopathy 96		HP:0011097	PMID:31675180	PCS		1/2
 */
public class OnsetCommand extends  HPOCommand implements Callable<Integer> {
    /** Terms such as Polydactyly that have a certain assignment to an age of onset (Congenital is taken
     * here to comprise also antenatal). The map is derived from the file {@code term2onset.txt} in the
     * resources section.
     */
    private Set<TermId> termIdToCongenitalOnsetSet;

    /** Congenital onset HP:0003577 */
    private final String CONGENITAL_ONSET = "HP:0003577";
    private final String EMPTY_STRING = "";

    private final String INFERRED_FROM_ELECTRONIC_ANNOTATION = "IEA";

    private final String C_ASPECT = "C";
    /**
     * Köhler S, et al. The Human Phenotype Ontology in 2021. Nucleic Acids Res. 2021;49(D1):D1207-D1217.
     * doi: 10.1093/nar/gkaa1043. PMID: 33264411; PMCID: PMC7778952.
     */
    private final String HPO_PMID = "PMID:33264411";

    @CommandLine.Option(names={"--outfile"}, description = "path to outfile")
    private String outfilePath = "predictedCongenital.hpoa";

    public OnsetCommand() {

    }


    @Override
    public Integer call() throws Exception {
        if (hpopath==null) {
            throw new PhenolRuntimeException("Need to specify hp.json path");
        }
        if (annotpath==null) {
            throw new PhenolRuntimeException("Need to specify annotpath path");
        }

        Ontology ontology = OntologyLoader.loadOntology(new File(hpopath));
        HpoDiseaseLoaderOptions options = HpoDiseaseLoaderOptions.defaultOptions();
        HpoDiseaseLoader loader = HpoDiseaseLoader.of(ontology, options);
        HpoDiseases diseases = loader.load(Path.of(annotpath));
        // HPO terms that are asserted to always be congenital
        termIdToCongenitalOnsetSet = parseHpoTermToHpoOnsetMap(ontology);
        Set<HpoDisease> congenitalDiseaseSet = new HashSet<>();
        for (var disease: diseases) {
            if (! disease.id().getPrefix().equals("OMIM")) {
                continue; // do not annotate ORPHA or DECIPHER
            }
            if (disease.globalOnset().isPresent()) {
                continue;
            } else {
                var iterator = disease.phenotypicAbnormalities();
                while (iterator.hasNext()) {
                    HpoDiseaseAnnotation diseaseAnnotation = iterator.next();
                    if (diseaseAnnotation.ratio().orElse(Ratio.of(0, 1)).frequency() > 0) {
                        TermId hpoId = diseaseAnnotation.id();
                        if (termIdToCongenitalOnsetSet.contains(hpoId)) {
                            congenitalDiseaseSet.add(disease);
                            continue;
                        }
                    }
                }
            }
        }
        int n_inferred = 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfilePath))) {
            for (HpoDisease disease : congenitalDiseaseSet) {
                n_inferred++;
                List<String> fields = new ArrayList<>();
                // 0 #DatabaseID		Evidence Onset		Sex	Modifier	Aspect	Biocuration
                fields.add(disease.id().getValue());
                // 1  DiseaseName
                fields.add(disease.diseaseName());
                // 2 Qualifier
                fields.add(EMPTY_STRING);
                // 3 HPO_ID
                fields.add(CONGENITAL_ONSET);
                // 4 Reference
                fields.add(HPO_PMID);
                // 5 Evidence
                fields.add(INFERRED_FROM_ELECTRONIC_ANNOTATION);
                // 6 Onset (this field only used if annotation is feature
                fields.add(EMPTY_STRING);
                // 7 Frequency (we cannot inferred this)
                fields.add(EMPTY_STRING);
                // 8. Sex(we cannot inferred this)
                fields.add(EMPTY_STRING);
                // 9. Modifier n/a
                fields.add(EMPTY_STRING);
                // 10. Aspect
                fields.add(C_ASPECT);
                // 11. Biocuration
                fields.add("HPO:probinson[2022-05-21]");
                writer.write(String.join("\t", fields) + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        System.out.printf("[INFO] Inferred congenital onset for %d diseases.\n", n_inferred);
        return 0;
    }



    private Set<TermId> parseHpoTermToHpoOnsetMap(Ontology ontology) {
        URL url = OnsetCommand.class.getResource("/term2onset.txt");
        if (url == null) {
            System.err.println("Could not read term2onset file");
            return Set.of();
        }
        Set<TermId> termSet = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(url.getFile()))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length != 2) {
                    throw new PhenolRuntimeException("Malformed line with " + fields.length + " fields");
                }
                TermId hpoId = TermId.of(fields[1]);
                termSet.add(hpoId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // get all agenesis terms
        for (Term term : ontology.getTermMap().values()) {
            Set<String> labels = new HashSet<>();
            labels.add(term.getName().toLowerCase(Locale.ROOT));
            for (var syn : term.getSynonyms()) {
                labels.add(syn.getValue().toLowerCase(Locale.ROOT));
            }
            for (var lbl : labels) {
                if (lbl.contains("agenesis") || lbl.contains("aplasia") || lbl.contains("supernumerary")
                || lbl.contains("situs inversus") || lbl.contains("situs ambiguous")) {
                    termSet.add(term.id());
                }
            }
        }
        Set<TermId> termSetWithAncestors = new HashSet<>();
        for (TermId tid : termSet) {
            termSetWithAncestors.add(tid);
            termSetWithAncestors.addAll(OntologyAlgorithm.getDescendents(ontology, tid));
        }


        return termSetWithAncestors;
    }
}
