package org.monarchinitiative.hpoworkbench.annotation;


import com.google.common.collect.ImmutableList;


import org.monarchinitiative.phenol.annotations.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.category.HpoCategory;
import org.monarchinitiative.phenol.annotations.formats.hpo.category.HpoCategoryMap;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(AnnotationMerger.class);
    private final Ontology ontology;

    private final HpoDisease disease1;
    private final HpoDisease disease2;
    private final String diseaseName1;
    private final String diseaseName2;

    /** TermId's for annotations that are identical between OMIM and Orphanet. */
    private List<TermId> sharedTerms;


    private final Map<HpoCategory,Set<TermId>> disease1ByCategory;
    private final Map<HpoCategory,Set<TermId>> disease2ByCategory;
    private final Set<HpoCategory> allCategorySet;

    private final Map<HpoCategory,CategoryMerge> mergedCategoryMap;


    public AnnotationMerger(HpoDisease d1, HpoDisease d2, Ontology honto) {
        disease1=d1;
        disease2=d2;
        if (disease1==null) {
            diseaseName1="";
            logger.trace("disease 1 == null");
        } else {
            diseaseName1=disease1.getName();
            logger.trace("disease 1 ok, {}" , disease1.toString());
        }
        if (disease2==null) {
            logger.trace("disease 2 == null");
            diseaseName2="";
        } else {
            diseaseName2=disease2.getName();
            logger.trace("disease 2 ok, {}" , disease2.toString());
        }

        ontology=honto;
        disease1ByCategory=new HashMap<>();
        disease2ByCategory=new HashMap<>();
        allCategorySet=new HashSet<>();
        mergedCategoryMap =new HashMap<>();
    }


    public void merge() {
        mergeByCategory();
    }

    public void printToShell() {
        System.out.print("Disease 1: ");
        printDisease(disease1);
        System.out.print("Disease 2: ");
        printDisease(disease2);
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
            System.out.printf("Only %s contains terms from category %s:%n",diseaseName2,hcat.getLabel());
            printTerms(catmerge.getDisease1onlyTerms());
        } else if (catmerge.onlyDisease2()) {
            System.out.printf("Only %s contains terms from category %s:%n",diseaseName1,hcat.getLabel());
            printTerms(catmerge.getDisease1onlyTerms());
        } else {
            List<TermId> commonterms = catmerge.getCommonTerms();
            for (TermId ctid : commonterms) {
                String label = ontology.getTermMap().get(ctid).getName();
                System.out.println("Both diseases: " + label + "[" + ctid.getValue() + "]");
            }
            List<SubClassTermPair> sclasspairs = catmerge.getD1subclassOfd2();
            for (SubClassTermPair tscp : sclasspairs) {
                TermId t1=tscp.getSubTid();
                TermId t2=tscp.getSuperTid();
                String label1 = ontology.getTermMap().get(t1).getName();
                String label2 = ontology.getTermMap().get(t2).getName();
                System.out.printf("%s [%s] (%s)is subclass of %s [%s] (%s)%n",
                        label1, t1.getValue(), diseaseName1,
                        label2, t2.getValue(), diseaseName2);
            }
            sclasspairs = catmerge.getD2subclassOfd1();
            for (SubClassTermPair tscp : sclasspairs) {
                TermId t2=tscp.getSubTid();
                TermId t1=tscp.getSuperTid();
                String label1 = ontology.getTermMap().get(t1).getName();
                String label2 = ontology.getTermMap().get(t2).getName();
                System.out.printf("%s [%s] (%s) is subclass of %s [%s] (%s)%n",
                        label2, t2.getValue(), diseaseName2,
                        label1, t1.getValue(), diseaseName1);
            }
            for (TermId t1 : catmerge.getDisease1onlyTerms()) {
                String label = ontology.getTermMap().get(t1).getName();
                System.out.printf("%s only: %s [%s]%n",diseaseName1,label,t1.getValue());
            }
            for (TermId t2 : catmerge.getDisease2onlyTerms()) {
                String label = ontology.getTermMap().get(t2).getName();
                System.out.printf("%s only: %s [%s]%n",diseaseName2,label,t2.getValue());
            }
        }
    }


    private void printTerms(List<TermId> tidlist) {
        for (TermId tid : tidlist) {
            String label = ontology.getTermMap().get(tid).getName();
            System.out.println("\t" + label + "[" + tid.getValue() +"]");
        }
    }



    private void mergeByCategory() {
        List<TermId> lst1;
        if (disease1==null) {
            lst1= ImmutableList.of();
        } else {
            lst1 = disease1.getPhenotypicAbnormalities().stream().map(HpoAnnotation::getTermId).collect(Collectors.toList());
        }
        List<TermId> lst2;
        if (disease2==null) {
            lst2=ImmutableList.of();
        } else {
            lst2 = disease2.getPhenotypicAbnormalities().stream().map(HpoAnnotation::getTermId).collect(Collectors.toList());
        }
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
        CategoryMerge catmerge = new CategoryMerge(hcat.getLabel(), disease1,disease2);
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
                if (tidl2.contains(t1)) {  // Both disease have same annotation
                    accountedFor.add(t1);
                    catmerge.addCommonTerm(t1);
                } else {
                    for (TermId t2 : tidl2) {
                        if (existsPath(ontology, t1, t2)) {
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


    public Map<HpoCategory,CategoryMerge> getMergedCategoryMap() {
        return mergedCategoryMap;
    }



    private void printDisease(HpoDisease d) {
        System.out.println(d.getName());
        for (HpoAnnotation ann : d.getPhenotypicAbnormalities()) {
            TermId tid = ann.getTermId();
            String label = ontology.getTermMap().get(tid).getName();
            System.out.println("\t" + label + "[" + tid.getValue() +"]");
        }
    }




}
