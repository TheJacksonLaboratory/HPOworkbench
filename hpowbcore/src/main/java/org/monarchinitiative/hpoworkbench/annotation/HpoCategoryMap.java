package org.monarchinitiative.hpoworkbench.annotation;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.graph.algo.BreadthFirstSearch;
import com.github.phenomics.ontolib.graph.data.DirectedGraph;
import com.github.phenomics.ontolib.graph.data.Edge;
import com.github.phenomics.ontolib.graph.data.ImmutableEdge;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.Ontology;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Model of the upper-level HPO classes. This allows a canonical view of HPO Annotation per disease
 * according to categories such as Heart, Eye, Brain etc.
 * <p>
 * <p>
 * The main function takes a list of HPO terms and returns a compatible list but sorted according to the
 * categories
 * </p>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.5
 */
public class HpoCategoryMap {
    private static final Logger logger = LogManager.getLogger();
    /**
     * Key: the HPO TermId, value: HpoCategory for this term. For instance, the HPO TermId HP:0001626
     * (Abnormality of the cardiovascular system) would corresspond to the category Cardiovascular and
     * several subcategories.
     */
    private ImmutableMap<TermId, HpoCategory> categorymap;

    private static final TermId INHERITANCE_ID = ImmutableTermId.constructWithPrefix("HP:0000005");
    private static final TermId ABNORMAL_CELLULAR_ID = ImmutableTermId.constructWithPrefix("HP:0025354");
    private static final TermId BLOOD_ID = ImmutableTermId.constructWithPrefix("HP:0001871");
    private static final TermId CONNECTIVE_TISSUE_ID = ImmutableTermId.constructWithPrefix("HP:0003549");
    private static final TermId HEAD_AND_NECK_ID = ImmutableTermId.constructWithPrefix("HP:0000152");
    private static final TermId LIMBS_ID =ImmutableTermId.constructWithPrefix("HP:0040064");
    private static final TermId METABOLISM_ID = ImmutableTermId.constructWithPrefix("HP:0001939");
    private static final TermId PRENATAL_ID = ImmutableTermId.constructWithPrefix("HP:0001197");
    private static final TermId BREAST_ID = ImmutableTermId.constructWithPrefix("HP:0000769");
    private static final TermId CARDIOVASCULAR_ID = ImmutableTermId.constructWithPrefix("HP:0001626");
    private static final TermId DIGESTIVE_ID = ImmutableTermId.constructWithPrefix("HP:0025031");
    private static final TermId EAR_ID = ImmutableTermId.constructWithPrefix("HP:0000598");
    private static final TermId ENDOCRINE_ID = ImmutableTermId.constructWithPrefix("HP:0000818");
    private static final TermId EYE_ID = ImmutableTermId.constructWithPrefix("HP:0000478");
    private static final TermId GENITOURINARY_ID = ImmutableTermId.constructWithPrefix("HP:0000119");
    private static final TermId IMMUNOLOGY_ID = ImmutableTermId.constructWithPrefix("HP:0002715");
    private static final TermId INTEGUMENT_ID = ImmutableTermId.constructWithPrefix("HP:0001574");
    private static final TermId MUSCLE_ID = ImmutableTermId.constructWithPrefix("HP:0003011");
    private static final TermId NERVOUS_SYSTEM_ID = ImmutableTermId.constructWithPrefix("HP:0000707");
    private static final TermId RESPIRATORY_ID = ImmutableTermId.constructWithPrefix("HP:0002086");
    private static final TermId SKELETAL_ID = ImmutableTermId.constructWithPrefix("HP:0000924");
    private static final TermId THORACIC_CAVITY_ID = ImmutableTermId.constructWithPrefix("HP:0045027");
    private static final TermId VOICE_ID = ImmutableTermId.constructWithPrefix("HP:0001608");
    private static final TermId CONSTITUTIONAL_ID = ImmutableTermId.constructWithPrefix("HP:0025142");
    private static final TermId GROWTH_ID = ImmutableTermId.constructWithPrefix("HP:0001507");
    private static final TermId NEOPLASM_ID = ImmutableTermId.constructWithPrefix("HP:0002664");

    public HpoCategoryMap() {
        initializeMap();
        initializeOrderedArray();
    }

    private TermId[] termIdList;


    private void initializeOrderedArray() {
        termIdList = new TermId[]{INHERITANCE_ID,ABNORMAL_CELLULAR_ID,BLOOD_ID,CONNECTIVE_TISSUE_ID,HEAD_AND_NECK_ID,
                LIMBS_ID,METABOLISM_ID,PRENATAL_ID,BREAST_ID,CARDIOVASCULAR_ID,DIGESTIVE_ID,
                EAR_ID,ENDOCRINE_ID,EYE_ID,GENITOURINARY_ID,IMMUNOLOGY_ID,INTEGUMENT_ID,
                MUSCLE_ID,NERVOUS_SYSTEM_ID,RESPIRATORY_ID,SKELETAL_ID,THORACIC_CAVITY_ID,
                VOICE_ID,GROWTH_ID,CONSTITUTIONAL_ID,NEOPLASM_ID};
    }




    private void initializeMap() {
        ImmutableMap.Builder<TermId, HpoCategory> mapbuilder = new ImmutableMap.Builder<>();
        // Inheritance
        HpoCategory inheritanceCategory = new HpoCategory.Builder(INHERITANCE_ID,"Inheritance").build();
        mapbuilder.put(INHERITANCE_ID,inheritanceCategory);
        // Abn cellular phenotype
        HpoCategory abnCellCategory = new HpoCategory.Builder(ABNORMAL_CELLULAR_ID, "Cellular phenotype").build();
        mapbuilder.put(ABNORMAL_CELLULAR_ID, abnCellCategory);
        // Blood
        HpoCategory abnBlood = new HpoCategory.Builder(BLOOD_ID, "Blood and blood-forming tissues").build();
        mapbuilder.put(abnBlood.getTid(), abnBlood);
        // Connective tissue
        HpoCategory abnConnTiss = new HpoCategory.Builder(CONNECTIVE_TISSUE_ID,"Connective tissue").build();
        mapbuilder.put(abnConnTiss.getTid(), abnConnTiss);
        // head or neck
        HpoCategory headNeckCat = new HpoCategory.Builder(HEAD_AND_NECK_ID, "Head and neck").
                subcategory(ImmutableTermId.constructWithPrefix("HP:0000234"), "Head").
                subcategory(ImmutableTermId.constructWithPrefix("HP:0000464"), "Neck").build();
        mapbuilder.put(HEAD_AND_NECK_ID, headNeckCat);
        // limbs
        HpoCategory limbCat = new HpoCategory.Builder(LIMBS_ID, "Limbs").build();
        mapbuilder.put(LIMBS_ID,limbCat);
        // metabolism
        HpoCategory metabolismCat = new HpoCategory.Builder(METABOLISM_ID, "Metabolism/Laboratory abnormality").build();
        mapbuilder.put(METABOLISM_ID, metabolismCat);
        //prenatal
        HpoCategory prenatalCat = new HpoCategory.Builder(PRENATAL_ID, "Prenatal and Birth").build();
        mapbuilder.put(PRENATAL_ID, prenatalCat);
        //breast
        HpoCategory breastCat = new HpoCategory.Builder(BREAST_ID, "Breast").build();
        mapbuilder.put(BREAST_ID, breastCat);
        //cardiovascular
        HpoCategory cardiovascularCat = new HpoCategory.Builder(CARDIOVASCULAR_ID, "Cardiovascular").
                subcategory(ImmutableTermId.constructWithPrefix("HP:0002597"), "Vascular").
                subcategory(ImmutableTermId.constructWithPrefix("HP:0010948"), "fetal cardiovascular").
                build(); // ToDo extend!!!
        mapbuilder.put(CARDIOVASCULAR_ID, cardiovascularCat);
        //digestive
        HpoCategory digestiveCat = new HpoCategory.Builder(DIGESTIVE_ID, "Digestive System").build();
        mapbuilder.put(DIGESTIVE_ID, digestiveCat);
        // ear
        HpoCategory earCat = new HpoCategory.Builder(EAR_ID, "Ear").build();
        mapbuilder.put(EAR_ID, earCat);
        //endocrine
        HpoCategory endocrineCat = new HpoCategory.Builder(ENDOCRINE_ID, "Endocrine").build();
        mapbuilder.put(ENDOCRINE_ID, endocrineCat);
        // eye
        HpoCategory eyeCat = new HpoCategory.Builder(EYE_ID, "Eye").build();
        mapbuilder.put(EYE_ID, eyeCat);
        //genitourinary
        HpoCategory guCat = new HpoCategory.Builder(GENITOURINARY_ID, "Genitourinary system").
                subcategory(ImmutableTermId.constructWithPrefix("HP:0000078"), "Genital system").
                subcategory(ImmutableTermId.constructWithPrefix("HP:0000079"), "Urinary system").
                build();
        mapbuilder.put(GENITOURINARY_ID, guCat);
        // Immune
        HpoCategory immuneCat = new HpoCategory.Builder(IMMUNOLOGY_ID, "Immunology").build();
        mapbuilder.put(IMMUNOLOGY_ID, immuneCat);
        //integument
        HpoCategory integumentCat = new HpoCategory.Builder(INTEGUMENT_ID, "Skin, Hair, and Nails").build();
        mapbuilder.put(INTEGUMENT_ID, integumentCat);
        //muscle
        HpoCategory muscleCat = new HpoCategory.Builder(MUSCLE_ID, "Musculature").build();
        mapbuilder.put(MUSCLE_ID, muscleCat);
        //Nervous system
        HpoCategory nervousCat = new HpoCategory.Builder(NERVOUS_SYSTEM_ID, "Nervous System").build();
        mapbuilder.put(NERVOUS_SYSTEM_ID, nervousCat);
        //respiratory system
        HpoCategory respiratoryCat = new HpoCategory.Builder(RESPIRATORY_ID, "Repiratory System").build();
        mapbuilder.put(RESPIRATORY_ID, respiratoryCat);
        // skeletal
        HpoCategory skeletalCat = new HpoCategory.Builder(SKELETAL_ID, "Skeletal system").build();
        mapbuilder.put(SKELETAL_ID, skeletalCat);
        //thoracic cavity
        HpoCategory thoracicCat = new HpoCategory.Builder(THORACIC_CAVITY_ID, "Thoracic cavity").build();
        mapbuilder.put(THORACIC_CAVITY_ID, thoracicCat);
        //voice
        HpoCategory voiceCat = new HpoCategory.Builder(VOICE_ID, "Voice").build();
        mapbuilder.put(VOICE_ID, voiceCat);
        //consistutiotnal symtpom
        HpoCategory constitutionalCat = new HpoCategory.Builder(CONSTITUTIONAL_ID, "Constitutional Symptom").build();
        mapbuilder.put(CONSTITUTIONAL_ID, constitutionalCat);
        // growth
        HpoCategory growthCat = new HpoCategory.Builder(GROWTH_ID, "Growth").build();
        mapbuilder.put(GROWTH_ID, growthCat);
        // neoplasm
        HpoCategory neoplasmCat = new HpoCategory.Builder(NEOPLASM_ID, "Neoplasm").build();
        mapbuilder.put(NEOPLASM_ID, neoplasmCat);
        // Finally, build the map!
        categorymap = mapbuilder.build();
    }


    public void addAnnotatedTerm(TermId tid, HpoOntology ontology) {
        try {
            HpoCategory cat = getCategory(tid, ontology);
            if (cat==null) {
                logger.error("Could not get category for " + ontology.getTermMap().get(tid).getName());
                return;
            }
            cat.addAnnotatedTerm(tid, ontology);
        } catch (Exception e) {
            logger.error(String.format("Exception trying to find category for %s",ontology.getTermMap().get(tid).getName()));
            e.printStackTrace();
        }
    }



    /** Find all of the direct parents of childTermId (do not include "grandchildren" and other descendents).
     * @param ontology The ontology to which parentTermId belongs
     * @param childTermId The term whose parents were are seeking
     * @return A set of all parent terms of childTermId
     */
    private Set<TermId> getParentTerms(HpoOntology ontology, TermId childTermId) {
        ImmutableSet.Builder<TermId> anccset = new ImmutableSet.Builder<>();
        Iterator it =  ontology.getGraph().outEdgeIterator(childTermId);
        while (it.hasNext()) {
            Edge<TermId> edge = (Edge<TermId>) it.next();
            TermId destId=edge.getDest();
            anccset.add(destId);
        }
        return anccset.build();
    }

    private Set<TermId> getAncestorCategories(HpoOntology ontology, TermId childTermId) {
        ImmutableSet.Builder<TermId> anccset = new ImmutableSet.Builder<>();
        Stack<TermId> stack = new Stack<>();
        stack.push(childTermId);
        while (! stack.empty()) {
            TermId tid = stack.pop();
            Set<TermId> parents = getParentTerms(ontology,tid);
            for (TermId p : parents) {
                if (this.categorymap.containsKey(p)) {
                    anccset.add(p);
                } else {
                    stack.add(p);
                }
            }
        }
        return anccset.build();
    }




//
//    public static boolean existsPath2(Ontology ontology, final TermId sourceID, TermId destID){
//        // special case -- a term cannot have a path to itself in an ontology (DAG)
//        if (sourceID.equals(destID)) return false;
//        final DirectedGraph<TermId, ImmutableEdge<TermId>> graph=ontology.getGraph();
//        List<TermId> visited = new ArrayList<>();
//        BreadthFirstSearch<TermId, ImmutableEdge<TermId>> bfs = new BreadthFirstSearch<>();
//        bfs.startFromForward(graph, sourceID, (g, termId) -> {
//            visited.add(termId);
//            System.out.println("visited "+termId.getIdWithPrefix());
//            return true;
//        });
//        return visited.contains(destID);
//    }
//



    /** Identify the category that best matches the term id (usually just fine the category that represents a parent term. */
    private HpoCategory getCategory(TermId tid, HpoOntology ontology) {
        //logger.trace(String.format("GetCategory for %s[%s]",ontology.getTermMap().get(tid).getName(),tid.getIdWithPrefix()));
        List<HpoCategory> activeCategoryList = new ArrayList<>();
        if (tid==null) {
            logger.error("Warning, tid was null...");
            return null;
        }
        Set<TermId> ancs = getAncestorCategories(ontology,tid);
        return getPrioritizedCategory(ancs);
    }


    private HpoCategory getPrioritizedCategory(Set<TermId> catlist) {
        for (TermId hpocat : catlist) {
            if (hpocat.equals(NEOPLASM_ID))
                return categorymap.get(hpocat);
        }
        // if we get here there was no prioritized category
        for (TermId c : catlist) {
            return  categorymap.get(c);
        }
        return null;
    }


    /**
     * Return neoplasm with higher priority than the other categories. ToDO Anything else?
     *
     * @return the primariy termId if we have more than one.
     */
    private TermId getPrioritizedTid(Set<TermId> ancestors) {
        for (TermId id : ancestors) {
            if (id.equals(NEOPLASM_ID)) {
                return NEOPLASM_ID; // we always want this
            }
        }
        return ancestors.iterator().next();
    }


    public void outputCategories() {
        for (HpoCategory cat : categorymap.values()) {
            if (cat.hasAnnotation()) {
                System.out.println(cat.getAnnotationString());
            }
        }
    }

    /**
     *
     * @return List of all categories that have at least one HPO Term annotated
     */
    public List<HpoCategory> getActiveCategoryList() {
        List<HpoCategory> lst = new ArrayList<>();
        for (HpoCategory cat : categorymap.values()) {
            if (cat.hasAnnotation()) {
                lst.add(cat);
            }
        }
        return lst;
    }


}
