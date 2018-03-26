package org.monarchinitiative.hpoworkbench.annotation;


import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class 'merges' two sets of disease annotations. THe intended use case is that we have annotation from
 * Orphanet and from OMIM, and we would like to automatically merge them into one combined MONDO
 * annotation set.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */

public class AnnotationMerger {

    private final HpoDisease disease1;
    private final HpoDisease disease2;
    private final HpoOntology ontology;


    private List<TermId> sharedTerms;

    private final HpoCategoryMap categoryMap;

    private Map<HpoCategory,Set<TermId>> disease1ByCategory;
    private Map<HpoCategory,Set<TermId>> disease2ByCategory;
    private Set<HpoCategory> allCategorySet;

    private Map<HpoCategory,CategoryMerge> mergedCategoryMap;


    public AnnotationMerger(HpoDisease d1, HpoDisease d2, HpoOntology honto) {
        disease1=d1;
        disease2=d2;
        ontology=honto;
        categoryMap=new HpoCategoryMap();
        disease1ByCategory=new HashMap<>();
        disease2ByCategory=new HashMap<>();
        allCategorySet=new HashSet<>();
        mergedCategoryMap =new HashMap<>();
    }


    public void merge() {
        System.out.print("Disease 1: ");
        printDisease(disease1);
        System.out.print("Disease 2: ");
        printDisease(disease2);
        mergeByCategory();
        outputByCategory();
        System.out.println();
        System.out.println();
    }


    private void outputByCategory() {
        for (HpoCategory hcat: allCategorySet) {
            outputCategory(hcat);
        }

    }

    private void outputCategory(HpoCategory hcat) {
        System.out.println("############## " + hcat.getLabel() + " ##############");
        CategoryMerge catmerge = mergedCategoryMap.get(hcat);
        if (catmerge.onlyDisease1()) {
            System.out.println(String.format("Only %s contains terms from category %s:",diseaseName(disease2),hcat.getLabel()));
            printTerms(catmerge.getDisease1onlyTerms());
        } else if (catmerge.onlyDisease2()) {
            System.out.println(String.format("Only %s contains terms from category %s:",diseaseName(disease1),hcat.getLabel()));
            printTerms(catmerge.getDisease1onlyTerms());
        } else {
            List<TermId> commonterms = catmerge.getCommonTerms();
            for (TermId ctid : commonterms) {
                String label = ontology.getTermMap().get(ctid).getName();
                System.out.println("Both diseases: " + label + "[" + ctid.getIdWithPrefix() + "]");
            }
            List<TermSubClassPair> sclasspairs = catmerge.getD1subclassOfd2();
            for (TermSubClassPair tscp : sclasspairs) {
                TermId t1=tscp.getSubTid();
                TermId t2=tscp.getSuperTid();
                String label1 = ontology.getTermMap().get(t1).getName();
                String label2 = ontology.getTermMap().get(t2).getName();
                System.out.println(String.format("%s [%s] (%s)is subclass of %s [%s] (%s)",
                        label1, t1.getIdWithPrefix(), diseaseName(disease1),
                        label2, t2.getIdWithPrefix(),diseaseName(disease2)));
            }
            sclasspairs = catmerge.getD2subclassOfd1();
            for (TermSubClassPair tscp : sclasspairs) {
                TermId t2=tscp.getSubTid();
                TermId t1=tscp.getSuperTid();
                String label1 = ontology.getTermMap().get(t1).getName();
                String label2 = ontology.getTermMap().get(t2).getName();
                System.out.println(String.format("%s [%s] (%s)is subclass of %s [%s] (%s)",
                        label2, t2.getIdWithPrefix(), diseaseName(disease2),
                        label1, t1.getIdWithPrefix(),diseaseName(disease1)));
            }
            for (TermId t1 : catmerge.getDisease1onlyTerms()) {
                String label = ontology.getTermMap().get(t1).getName();
                System.out.println(String.format("%s only: %s [%s]",diseaseName(disease1),label,t1.getIdWithPrefix()));
            }
            for (TermId t2 : catmerge.getDisease2onlyTerms()) {
                String label = ontology.getTermMap().get(t2).getName();
                System.out.println(String.format("%s only: %s [%s]",diseaseName(disease2),label,t2.getIdWithPrefix()));
            }
        }
    }


    private void printTerms(List<TermId> tidlist) {
        for (TermId tid : tidlist) {
            String label = ontology.getTermMap().get(tid).getName();
            System.out.println("\t" + label + "[" + tid.getIdWithPrefix() +"]");
        }
    }



    private void mergeByCategory() {
        List<TermId> lst1 = disease1.getPhenotypicAbnormalities().stream().map(HpoAnnotation::getTermId).collect(Collectors.toList());
        List<TermId> lst2 = disease2.getPhenotypicAbnormalities().stream().map(HpoAnnotation::getTermId).collect(Collectors.toList());
        HpoCategoryMap catmap1 =  new HpoCategoryMap();
        catmap1.addAnnotatedTerms(lst1,ontology);
        List<HpoCategory> catlist1=catmap1.getActiveCategoryList();
        for (HpoCategory hcat : catlist1) {
            List<TermId> annotatingTerms = hcat.getAnnotatingTermIds();
            this.disease1ByCategory.put(hcat,new HashSet<>(annotatingTerms));
            allCategorySet.add(hcat);
        }
        HpoCategoryMap catmap2 =  new HpoCategoryMap();
        catmap2.addAnnotatedTerms(lst2,ontology);
        List<HpoCategory> catlist2=catmap2.getActiveCategoryList();
        for (HpoCategory hcat : catlist2) {
            List<TermId> annotatingTerms = hcat.getAnnotatingTermIds();
            this.disease2ByCategory.put(hcat,new HashSet<>(annotatingTerms));
            allCategorySet.add(hcat);
        }
        for (HpoCategory hcat : allCategorySet) {
            MergeOneCategory(hcat);
        }
    }


    private void MergeOneCategory(HpoCategory hcat) {
        CategoryMerge catmerge = new CategoryMerge(hcat.getLabel(), diseaseName(disease1),diseaseName(disease2));
        if (disease2ByCategory.containsKey(hcat) && ! disease1ByCategory.containsKey(hcat)) {
            catmerge.addDisease2OnlyTermIdSet(disease2ByCategory.get(hcat));
        }  else if (disease1ByCategory.containsKey(hcat) && ! disease2ByCategory.containsKey(hcat)) {
            catmerge.addDisease1OnlyTermIdSet(disease1ByCategory.get(hcat));
        }  else {
            // both diseases have terms
            Set<TermId> tidl1 = disease1ByCategory.get(hcat);
            Set<TermId> tidl2 = disease2ByCategory.get(hcat);
            Set<TermId> accountedFor = new HashSet<>();
            for (TermId t1 : tidl1) {
                if (tidl2.contains(t1)) {
                    accountedFor.add(t1);
                    catmerge.addCommonTerm(t1);
                } else {
                    for (TermId t2 : tidl2) {
                        if (existsPath(ontology, t1, t1)) {
                            // t1 is a subclass of t2
                            accountedFor.add(t1);
                            accountedFor.add(t2);
                            catmerge.addTermId1SubclassOfubOfTermId2(t1,t2);
                        }
                    }
                }
            }
            // now try from other direction. No need to look for identical terms here
            for (TermId t2 : tidl2) {
                for (TermId t1 : tidl1) {
                    if (existsPath(ontology, t2, t1)) {
                        // t2 is a subclass of t1
                        accountedFor.add(t1);
                        accountedFor.add(t2);
                        catmerge.addTermId2SubclassOfubOfTermId1(t2,t1);
                    }
                }
            }
            // If we get here, there may be unaccounted terms that are not subclasses
            for (TermId t1 : tidl1) {
                if (accountedFor.contains(t1)) continue;
                catmerge.addDisease1OnlyTermId(t1);
            }
            for (TermId t2 : tidl2) {
                if (accountedFor.contains(t2)) continue;
                catmerge.addDisease2OnlyTermId(t2);
            }
        }
        mergedCategoryMap.put(hcat,catmerge);
    }



    private String diseaseName(HpoDisease d) {
        return d.getName() +"(" + d.getDatabase()+":"+ d.getDiseaseDatabaseId() +")";
    }

    private void printDisease(HpoDisease d) {
        System.out.println(diseaseName(d));
        for (HpoAnnotation ann : d.getPhenotypicAbnormalities()) {
            TermId tid = ann.getTermId();
            String label = ontology.getTermMap().get(tid).getName();
            System.out.println("\t" + label + "[" + tid.getIdWithPrefix() +"]");
        }
    }




}
