package org.monarchinitiative.hpoworkbench.cmd;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.phenol.annotations.assoc.HpoAssociationParser;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoOnsetTermIds;
import org.monarchinitiative.phenol.annotations.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.graph.IdLabeledEdge;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;

/**
 * Extract descriptive statistics about a a certain subhierarchy of the HPO.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
@Parameters(commandDescription = "stats. Extract descriptive statistics about a subhierarchy of the HPO.")
public class HpoStatsCommand extends HPOCommand  {
    private static final Logger LOGGER = LoggerFactory.getLogger(HpoStatsCommand.class);
    private Ontology hpoOntology=null;
    /** All disease annotations for the entire ontology. */
    private Map<TermId, HpoDisease> diseaseMap =null;

    /** Set of all HPO terms that are descendents of {@link #termOfInterest}. */
    private Set<TermId> descendentsOfTheTermOfInterest =null;
    /** We use this to count the number of subclass relations underneath {@link #termOfInterest}. */
    private Set<IdLabeledEdge> relationsUnderTermOfInterest;
    private Set<TermId> adultOnset=null;
    private Set<TermId> childhoodOnset=null;


    private List<HpoDisease> omim;
    private List<HpoDisease> orphanet;
    private List<HpoDisease> decipher;





    /** the root of the subhierarchy for which we are calculating the descriptive statistics. */
    @Parameter(names={"-t","--term"},description = "the root of the subhierarchy for which we are calculating the descriptive statistics.")
    private String term;


    private TermId termOfInterest;



    public HpoStatsCommand() {

    }

    @Override
    public  void run() {

        if (hpopath==null) {
            hpopath = this.downloadDirectory + File.separator + "hp.obo";
        }
        if (annotpath==null) {
            annotpath = this.downloadDirectory + File.separator + "phenotype.hpoa";
        }

        LOGGER.trace(String.format("HPO path: %s, annotations: %s",hpopath,annotpath ));
        if (! term.startsWith("HP:") || term.length()!=10) {
            LOGGER.error(String.format("Malformed HPO id: \"%s\". Terminating program...",term ));
            System.exit(1);
        }
        this.termOfInterest=TermId.of(term);
        LOGGER.trace("Term of interest: "+termOfInterest.getValue());
        omim=new ArrayList<>();
        orphanet=new ArrayList<>();
        decipher=new ArrayList<>();
        inputHPOdata();
        getDescendentsOfTermOfInterest();
        filterDiseasesAccordingToDatabase();
        /*initializeAdultOnsetTerms();
        initializeChildhoodOnsetTerms();
        LOGGER.trace("Getting OMIM diseases according to onset");
        filterDiseasesAccordingToOnset(this.omim);
        LOGGER.trace("Getting ORPHANET diseases according to onset");
        filterDiseasesAccordingToOnset(this.orphanet);
        LOGGER.trace("Getting DECIPER diseases according to onset");
        filterDiseasesAccordingToOnset(this.decipher);
        */

        qcInheritanceModesForDiseases();
        countDiseasesWithAndWithoutAssociatedGenes();

    }


    private void countDiseasesWithAndWithoutAssociatedGenes() {
        String geneInfoFile = this.downloadDirectory + File.separator + "Homo_sapiens_gene_info.gz";
        String mim2genemedgenFile = this.downloadDirectory + File.separator + "mim2gene_medgen";
        HpoAssociationParser assocParser = new HpoAssociationParser(geneInfoFile,
                mim2genemedgenFile,
                //orphafilePlaceholder,
                // annotpath,
                hpoOntology);
        final Multimap<TermId,TermId> disease2geneIdMultiMap=assocParser.getDiseaseToGeneIdMap();
        final Map<TermId,String> geneId2SymbolMap = assocParser.getGeneIdToSymbolMap();
        Map<TermId, HpoDisease> diseaseMap = HpoDiseaseAnnotationParser.loadDiseaseMap(annotpath, hpoOntology);
        int disease_without_gene = 0;
        int disease_with_gene = 0;
        final Set<TermId> geneset = new HashSet<>();
        for (TermId diseaseId : diseaseMap.keySet()) {
            if (! diseaseId.getValue().contains("OMIM")) {
                continue;
            }
            if (disease2geneIdMultiMap.containsKey(diseaseId)) {
                disease_with_gene++;
                geneset.addAll(disease2geneIdMultiMap.get(diseaseId));
            } else {
                disease_without_gene++;
            }
        }
        System.out.printf("Diseases with associated gene: %d. Without associated gene: %d. Total %d Total genes: %d\n\n",
                disease_with_gene, disease_without_gene, disease_with_gene+disease_without_gene, geneset.size());
    }

    private void qcInheritanceModesForDiseases() {
        String geneInfoFile = this.downloadDirectory + File.separator + "Homo_sapiens_gene_info.gz";
        String mim2genemedgenFile = this.downloadDirectory + File.separator + "mim2gene_medgen";

        String orphafilePlaceholder = null;//we do not need this for now
        if (annotpath == null || annotpath.isEmpty()) {
            throw new RuntimeException("phenotype.hpoa path was not initialized");
        }
        HpoAssociationParser assocParser = new HpoAssociationParser(geneInfoFile,
                mim2genemedgenFile,
                //orphafilePlaceholder,
               // annotpath,
                hpoOntology);
        final Multimap<TermId,TermId> disease2geneIdMultiMap=assocParser.getDiseaseToGeneIdMap();
        final Map<TermId,String> geneId2SymbolMap = assocParser.getGeneIdToSymbolMap();
        for (TermId diseaseId : disease2geneIdMultiMap.keys()) {
            HpoDisease disease = this.diseaseMap.get(diseaseId);
            if (disease==null) {
                continue;
            }
            if (disease.getModesOfInheritance().isEmpty()) {
                String diseasename = disease.getName();
                String url = "https://omim.org/entry/" + disease.getDiseaseDatabaseId().getId();
                List<String> genes = new ArrayList<>();
                for (TermId geneId : disease2geneIdMultiMap.get(diseaseId)) {
                    String symbol = geneId2SymbolMap.get(geneId);
                    genes.add(symbol);
                }
                System.out.println(" - [ ] " + diseasename + "(" + url +"). Genes:" + String.join("; ",genes));
            }
        }
    }






    private void inputHPOdata() {
        File f = new File(hpopath);
        if (! f.exists()) {
            LOGGER.error(String.format("Could not find hpo ontology file at\"%s\". Terminating program...", hpopath ));
            System.exit(1);
        }
        f=new File(annotpath);
        if (! f.exists()) {
            LOGGER.error(String.format("Could not find phenotype annotation file at\"%s\". Terminating program...", annotpath ));
            System.exit(1);
        }
        LOGGER.trace(String.format("inputting data with files %s and %s",hpopath,annotpath));
        HPOParser parser = new HPOParser(hpopath);
        hpoOntology=parser.getHPO();
        diseaseMap = HpoDiseaseAnnotationParser.loadDiseaseMap(annotpath, hpoOntology);
        LOGGER.trace("Diseases imported: " + diseaseMap.size());
    }

    private void getDescendentsOfTermOfInterest() {
        String name = String.format("%s [%s]",hpoOntology.getTermMap().get(termOfInterest).getName(),termOfInterest.getValue() );
        relationsUnderTermOfInterest=new HashSet<>();
        descendentsOfTheTermOfInterest = countDescendentsAndSubclassRelations(hpoOntology,termOfInterest);
        int n_textual_def = getNumberOfTermsWithDefinition(hpoOntology,descendentsOfTheTermOfInterest);
        int n_synonyms = getTotalNumberOfSynonyms(hpoOntology,descendentsOfTheTermOfInterest);
        LOGGER.trace(String.format("We found a total of %d terms annotated to %s or descendents", descendentsOfTheTermOfInterest.size(), name));
        LOGGER.trace(String.format("Of these terms, %d has a textual definition. There were a total of %d synonyms.",n_textual_def,n_synonyms));
        LOGGER.trace(String.format("We found a total of %d subclass relations beneath the term of interest",relationsUnderTermOfInterest.size()));
    }



    /**
     * Find all of the descendents of parentTermId (including direct children and more distant
     * descendents)
     *
     * @param ontology The ontology to which parentTermId belongs
     * @param parentTermId The term whose descendents were are seeking
     * @return A set of all descendents of parentTermId (including the parentTermId itself)
     */
    private Set<TermId> countDescendentsAndSubclassRelations(
            Ontology ontology, TermId parentTermId) {
        ImmutableSet.Builder<TermId> descset = new ImmutableSet.Builder<>();
        Stack<TermId> stack = new Stack<>();
        stack.push(parentTermId);
        while (!stack.empty()) {
            TermId tid = stack.pop();
            descset.add(tid);
            Set<TermId> directChildrenSet = countChildTermsAndSubclassRelations(ontology, tid);
            directChildrenSet.forEach(stack::push);
        }
        return descset.build();
    }

    /**
     * Find all of the direct children of parentTermId (do not include "grandchildren" and other
     * descendents).
     *
     * @param ontology The ontology to which parentTermId belongs
     * @param parentTermId The term whose children were are seeking
     * @return A set of all child terms of parentTermId
     */
    public Set<TermId> countChildTermsAndSubclassRelations(
            Ontology ontology,
            TermId parentTermId) {
        ImmutableSet.Builder<TermId> kids = new ImmutableSet.Builder<>();
        //if (includeOriginalTerm) kids.add(parentTermId);
        for (IdLabeledEdge edge : ontology.getGraph().incomingEdgesOf(parentTermId)) {
            TermId sourceId = (TermId) edge.getSource();
            kids.add(sourceId);
            this.relationsUnderTermOfInterest.add(edge);
        }
        return kids.build();
    }




    private int getTotalNumberOfSynonyms(Ontology ontology, Set<TermId> terms) {
        int n=0;
        for (TermId tid : terms) {
            n+=ontology.getTermMap().get(tid).getSynonyms().size();
        }
        return n;
    }

    private int getNumberOfTermsWithDefinition(Ontology ontology, Set<TermId> terms) {
        int n=0;
        for (TermId tid : terms) {
            String def=ontology.getTermMap().get(tid).getDefinition();
            if (def!=null && def.length()>0) n++;
        }
        return n;
    }



    private void initializeAdultOnsetTerms() {
        adultOnset=new HashSet<>();
        adultOnset.add(HpoOnsetTermIds.ADULT_ONSET);
        adultOnset.add(HpoOnsetTermIds.LATE_ONSET);
        adultOnset.add(HpoOnsetTermIds.MIDDLE_AGE_ONSET);
        adultOnset.add(HpoOnsetTermIds.YOUNG_ADULT_ONSET);
        LOGGER.trace(String.format("We found a total of %d adult onset terms",adultOnset.size()));
    }

    private void initializeChildhoodOnsetTerms() {
        childhoodOnset = new HashSet<>();
        childhoodOnset.add(HpoOnsetTermIds.CHILDHOOD_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.ANTENATAL_ONSET);
        childhoodOnset.add( HpoOnsetTermIds.CONGENITAL_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.EMBRYONAL_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.FETAL_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.INFANTILE_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.JUVENILE_ONSET);
        childhoodOnset.add(HpoOnsetTermIds.NEONATAL_ONSET);
    }


    private boolean diseaseAnnotatedToTermOfInterest(HpoDisease d) {
        List<HpoAnnotation> tiwmlist= d.getPhenotypicAbnormalities();
        for (HpoAnnotation id:tiwmlist) {
          if (this.descendentsOfTheTermOfInterest.contains(id.getTermId()))
              return true;
        }
        return false;
    }


    private void filterDiseasesAccordingToDatabase() {
        int n_omim_annot=0;
        int n_orpha_annot=0;
        int n_decipher_annot=0;
        if (diseaseMap==null) {
            LOGGER.error("Disease map was not properly initialized. Terminating program...");
            System.exit(1);
        }
        for (HpoDisease d:this.diseaseMap.values()) {
            if (!diseaseAnnotatedToTermOfInterest(d)) {
                continue;
            }
            int n_annot=0;
            for (HpoAnnotation annot : d.getPhenotypicAbnormalities() ){
                TermId hpoId=annot.getTermId();
                if (existsPath(hpoOntology,hpoId,termOfInterest)) {
                    n_annot++;
                }
            }
            String database=d.getDatabase();
            if (database.startsWith("OMIM")) {
                omim.add(d);
                n_omim_annot+=n_annot;
            } else if (database.startsWith("ORPHA")){
                orphanet.add(d);
                n_orpha_annot+=n_annot;
            } else if (database.startsWith("DECIPHER")) {
                decipher.add(d);
                n_decipher_annot+=n_annot;
            } else {
                LOGGER.error("Did not recognize data base"+ database);
            }
        }
        String termname=hpoOntology.getTermMap().get(termOfInterest).getName();
        LOGGER.trace(String.format("We found %d diseases in OMIM annotated to %s or descendents with %d total annotations for the term of interest",omim.size(),termname,n_omim_annot));
        LOGGER.trace(String.format("We found %d diseases in Orphanet annotated to %s or descendents with %d total annotations for the term of interest",orphanet.size(),termname,n_orpha_annot));
        LOGGER.trace(String.format("We found %d diseases in DECIPHER annotated to %s or descendents with %d total annotations for the term of interest",decipher.size(),termname,n_decipher_annot));
    }


    private boolean hasAdultOnset(HpoDisease d) {
        List<HpoAnnotation> ids=d.getPhenotypicAbnormalities();
        for (HpoAnnotation id:ids) {
            if (this.adultOnset.contains(id.getTermId()))
                return  true;
        }
        return false;
    }

    private boolean hasChildhoodOnset(HpoDisease d) {
        List<HpoAnnotation> ids=d.getPhenotypicAbnormalities();
        for (HpoAnnotation id:ids) {
            if (this.childhoodOnset.contains(id.getTermId()))
                return  true;
        }
        return false;
    }



    private void filterDiseasesAccordingToOnset(List<HpoDisease> diseases) {
        int no_onset=0;
        int early_onset=0;
        int adult_onset=0;
        for (HpoDisease d:diseases) {
            if (hasAdultOnset(d))
                adult_onset++;
            else if (hasChildhoodOnset(d))
                early_onset++;
            else
                no_onset++;
        }

        LOGGER.trace("ONSET CATEGORIES");
        LOGGER.trace(String.format("\tChildhood: %d",early_onset));
        LOGGER.trace(String.format("\tAdult: %d",adult_onset));
        LOGGER.trace(String.format("\tNo data: %d",no_onset));

    }


    @Override
    public String getName() {return "stats";}

}
