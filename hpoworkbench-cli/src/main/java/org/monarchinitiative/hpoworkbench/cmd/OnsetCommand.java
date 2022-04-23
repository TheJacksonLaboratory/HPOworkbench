package org.monarchinitiative.hpoworkbench.cmd;

import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.annotations.io.hpo.DiseaseDatabase;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoader;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaderOptions;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class OnsetCommand extends  HPOCommand implements Callable<Integer> {

    private Map<TermId, TermId> termIdTermIdMap;

    public OnsetCommand() {

    }


    @Override
    public Integer call() throws Exception {
        if (hpopath==null) {
            hpopath = this.downloadDirectory + File.separator + "hp.json";
        }
        String hpJsonPath = this.downloadDirectory + File.separator + this.hpopath;
        String annotationPath = this.downloadDirectory + File.separator + annotpath;

        Ontology ontology = OntologyLoader.loadOntology(new File(hpJsonPath));
        HpoDiseaseLoaderOptions options = HpoDiseaseLoaderOptions.defaultOptions();
        HpoDiseaseLoader loader = HpoDiseaseLoader.of(ontology, options);
        HpoDiseases diseases = loader.load(Path.of(annotationPath));

        termIdTermIdMap = parseHpoTermToHpoOnsetMap();
        diseases.hpoDiseases().forEach(this::processDisease);


        return 0;
    }


    public void processDisease(HpoDisease disease) {
        if (disease.globalOnset().isPresent())
           System.out.println(disease.globalOnset().get());

    }

    private Map<TermId, TermId> parseHpoTermToHpoOnsetMap() {
        URL url = OnsetCommand.class.getResource("/term2onset.txt");
        if (url == null) {
            System.err.println("Could not read term2onset file");
            return Map.of();
        }
        Map<TermId, TermId> termMap = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader(url.getFile()))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String []fields = line.split(",");
                if (fields.length != 3) {
                    throw new PhenolRuntimeException("Malformed line with " + fields.length + " fields");
                }
                TermId hpoId = TermId.of(fields[1]);
                TermId onsetId = getOnsetId(fields[2]);
                termMap.put(hpoId, onsetId);
            }
        } catch(IOException e) {
            e.printStackTrace();

        }
        return termMap;
    }


    private TermId getOnsetId(String onset) {
        switch (onset) {
            case "Congenital" -> {
                return TermId.of("HP:0003577");
            }
            default -> { throw new PhenolRuntimeException("Did not recognize onset term " + onset);
            }
        }
    }


    static class TermToOnset {

    }


}
