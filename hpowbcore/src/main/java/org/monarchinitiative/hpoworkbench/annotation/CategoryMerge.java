package org.monarchinitiative.hpoworkbench.annotation;

import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class represents the results of merging two diseases for one {@link HpoCategory}.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class CategoryMerge {

    private final String categoryLabel;

    private final String disease1name;
    private final String disease2name;

    private List<HpoAnnotation> disease1allTerms;
    private List<HpoAnnotation> disease2allTerms;

    private List<TermId> disease1onlyTerms;
    private List<TermId> disease2onlyTerms;
    private List<TermId> commonTerms;
    private List<TermSubClassPair> d1subclassOfd2;
    private List<TermSubClassPair> d2subclassOfd1;

    public CategoryMerge(String label,String d1name, String d2name){
        categoryLabel=label;
        disease1name=d1name;
        disease2name=d2name;
        disease1onlyTerms=new ArrayList<>();
        disease2onlyTerms=new ArrayList<>();
        commonTerms=new ArrayList<>();
        d1subclassOfd2 =new ArrayList<>();
        d2subclassOfd1=new ArrayList<>();
    }


    public void addDisease1OnlyTermIdSet(Set<TermId> tids) {
        disease1onlyTerms.addAll(tids);
    }

    public void addDisease1OnlyTermId(TermId tid) {
        disease1onlyTerms.add(tid);
    }

    public void addDisease2OnlyTermIdSet(Set<TermId> tids) {
        disease2onlyTerms.addAll(tids);
    }

    public void addDisease2OnlyTermId(TermId tid) {
        disease2onlyTerms.add(tid);
    }

    public void addTermId1SubclassOfubOfTermId2(TermId t1,TermId t2) {
        TermSubClassPair tscp = new TermSubClassPair(t1,t2);
        d1subclassOfd2.add(tscp);
    }

    public List<TermSubClassPair> getD2subclassOfd1() {
        return d2subclassOfd1;
    }

    public void addTermId2SubclassOfubOfTermId1(TermId t2, TermId t1) {
        TermSubClassPair tscp = new TermSubClassPair(t2,t1);
        d2subclassOfd1.add(tscp);
    }

    public void addCommonTerm(TermId tid) {
     commonTerms.add(tid);
    }


    public String getCategoryLabel() {
        return categoryLabel;
    }

    public String getDisease1name() {
        return disease1name;
    }

    public String getDisease2name() {
        return disease2name;
    }

    public List<HpoAnnotation> getDisease1allTerms() {
        return disease1allTerms;
    }

    public List<HpoAnnotation> getDisease2allTerms() {
        return disease2allTerms;
    }

    public List<TermId> getDisease1onlyTerms() {
        return disease1onlyTerms;
    }

    public List<TermId> getDisease2onlyTerms() {
        return disease2onlyTerms;
    }

    public List<TermId> getCommonTerms() {
        return commonTerms;
    }

    public List<TermSubClassPair> getD1subclassOfd2() {
        return d1subclassOfd2;
    }

    public boolean onlyDisease1() {
        return d1subclassOfd2.isEmpty() &&
                disease2onlyTerms.isEmpty() &&
                commonTerms.isEmpty() &&
                (! disease1onlyTerms.isEmpty());
    }

    public boolean onlyDisease2() {
        return d1subclassOfd2.isEmpty() &&
                disease1onlyTerms.isEmpty() &&
                commonTerms.isEmpty() &&
                (! disease2onlyTerms.isEmpty());
    }

}
