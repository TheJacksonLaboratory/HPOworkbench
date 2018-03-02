package org.monarchinitiative.hpoworkbench.annotation;

import com.github.phenomics.ontolib.formats.hpo.HpoDiseaseWithMetadata;
import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.TermIdWithMetadata;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiseaseProfile {
    private final HpoOntology ontology;

    private final HpoDiseaseWithMetadata disease;

    private final HpoCategoryMap hpocatmap;


    public DiseaseProfile(HpoOntology onto,HpoDiseaseWithMetadata hdwm){
        this.ontology=onto;
        this.disease=hdwm;
        hpocatmap = new HpoCategoryMap();
        List<TermId> ids = getTermIdsForAllAnnotatedPhenotypes();
        for (TermId tid : ids) {
            hpocatmap.addAnnotatedTerm(tid,ontology);
        }
    }





    public List<TermId> getTermIdsForAllAnnotatedPhenotypes() {
        return disease.getPhenotypicAbnormalities().
                stream().
                map(TermIdWithMetadata::getTermId).
                collect(Collectors.toList());
    }


    public Map<String,List<String>> getAllAnnotatedTermsByCategory() {
        ImmutableMap.Builder<String,List<String>> mapbuilder = new ImmutableMap.Builder<>();
        for (HpoCategory cat : hpocatmap.getActiveCategoryList()) {
            ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
            for (TermId tid : cat.getAnnotatingTermIds()) {
                String label = this.ontology.getTermMap().get(tid).getName();
                String l2 = String.format("%s [%s]",label,tid.getIdWithPrefix() );
                builder.add(l2);
            }
            mapbuilder.put(cat.getLabel(),builder.build());
        }
       return mapbuilder.build();
    }



    public Map<String,List<String>> getAllAnnotatedTermsByCategoryFrequencyThreshold(double threshold) {
        ImmutableMap.Builder<String,List<String>> mapbuilder = new ImmutableMap.Builder<>();
        for (HpoCategory cat : hpocatmap.getActiveCategoryList()) {
            ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
            for (TermId tid : cat.getAnnotatingTermIds()) {
                double f = disease.getFrequencyOfTermInDisease(tid);
                if (f<threshold) continue; // skip if the feature freqeuncy does not meet the threshold
                String label = this.ontology.getTermMap().get(tid).getName();
                String l2 = String.format("%s [%s]",label,tid.getIdWithPrefix() );
                builder.add(l2);
            }
            mapbuilder.put(cat.getLabel(),builder.build());
        }
        return mapbuilder.build();
    }


    public void dumpProfileToShell() {
        System.out.println(disease.toString());
        int n_total_terms=disease.getNumberOfPhenotypeAnnotations();
        System.out.println("#########   Show all features ###########");
        Map<String,List<String>> allTerms =  getAllAnnotatedTermsByCategory();
        for (String cat : allTerms.keySet()) {
            List<String> termsInCategory = allTerms.get(cat);
            int n_terms_in_category = termsInCategory.size();
            double proportionAmongAllTerms = (double)n_terms_in_category/n_total_terms;
            System.out.println(String.format("%s [%.1f%%]",cat,100*proportionAmongAllTerms));
            for (String t : termsInCategory) {
                System.out.println("\t" + t);
            }
        }
        System.out.println("#########   Show all features with frequency > 50% ###########");
        double threshold=0.50D; // 50% frequency threshold
        allTerms =  getAllAnnotatedTermsByCategoryFrequencyThreshold(threshold);
        int n_above_threshold_terms = allTerms.values().stream().mapToInt(lst-> lst.size()).sum();
        for (String cat : allTerms.keySet()) {
            List<String> termsInCategory = allTerms.get(cat);
            int n_terms_in_category = termsInCategory.size();
            double proportionAmongAllTerms = (double)n_terms_in_category/n_above_threshold_terms;
            System.out.println(String.format("%s [%.1f%%]",cat,100*proportionAmongAllTerms));
            for (String t : termsInCategory) {
                System.out.println("\t" + t);
            }
        }
    }







}
