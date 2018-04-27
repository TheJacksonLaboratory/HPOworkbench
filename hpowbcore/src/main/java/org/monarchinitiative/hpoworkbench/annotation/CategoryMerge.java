package org.monarchinitiative.hpoworkbench.annotation;

import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
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

    private final String db1;
    private final String db2;

    private List<HpoAnnotation> disease1allTerms;
    private List<HpoAnnotation> disease2allTerms;

    private List<TermId> disease1onlyTerms;
    private List<TermId> disease2onlyTerms;
    private List<TermId> commonTerms;
    private List<SubClassTermPair> d1subclassOfd2;
    private List<SubClassTermPair> d2subclassOfd1;

    public String getDb1() {
        return db1;
    }

    public String getDb2() {
        return db2;
    }

    public CategoryMerge(String label, HpoDisease d1, HpoDisease d2){
        categoryLabel=label;
        disease1name=diseaseName(d1);
        disease2name=diseaseName(d2);
        if (d1==null){
            db1="n/a";
        } else {
            db1 = d1.getDatabase();
        }
        if (d2==null) {
            db2="n/a";
        } else {
            db2 = d2.getDatabase();
        }
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
        SubClassTermPair tscp = new SubClassTermPair(t1,t2);
        d1subclassOfd2.add(tscp);
    }

    public List<SubClassTermPair> getD2subclassOfd1() {
        return d2subclassOfd1;
    }

    public void addTermId2SubclassOfubOfTermId1(TermId t2, TermId t1) {
        SubClassTermPair tscp = new SubClassTermPair(t2,t1);
        d2subclassOfd1.add(tscp);
    }

    public void addCommonTerm(TermId tid) {
     commonTerms.add(tid);
    }

    public boolean hasTermsUniqueToOnlyOneDisease() {
        return disease1onlyTerms.size()>0 || disease2onlyTerms.size()>0;
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

    public List<SubClassTermPair> getD1subclassOfd2() {
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

    private String diseaseName(HpoDisease d) {
        if (d==null) return "none";

        return d.getName() +" (" + d.getDiseaseDatabaseId() +")";
    }

    public String getCounts() {
        int total=0;
        int identical=0;
        int subclazz=0;
        int unrelated=0;
        identical=this.commonTerms.size();
        subclazz=this.d1subclassOfd2.size()+d2subclassOfd1.size();
        unrelated=this.disease1onlyTerms.size()+disease2onlyTerms.size();
        total=identical+subclazz+unrelated;
        return String.format("%d annotations [%d identical, %d subclass and %d unrelated",total,identical,subclazz,unrelated);
    }

}
