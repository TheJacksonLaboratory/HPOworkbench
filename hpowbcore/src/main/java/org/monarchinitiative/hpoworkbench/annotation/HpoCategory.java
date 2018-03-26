package org.monarchinitiative.hpoworkbench.annotation;


import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a category of HPO terms that we would like to display or treat as a group. Roughly, it
 * corresponds to the major organ abnormality categories, but it allows subcategoires to be added, for instance,
 * gastrointestinal can have the subcategory liver
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.12
 */
public class HpoCategory {
  private static final Logger logger = LogManager.getLogger();
  /** The TermId of the HPO term that corresponds to this category. For example, the HPO Term Abnormality of the
   * voice would correspond to the category called "Voice". We display the stirng "Voice" in a browser (for instance)
   * when we show terms fromthe soubontloguy that descends from Abnormality of voice. */
  private final TermId tid;
  /** THe display label for this category (e.g., "Voice"--see {@link #tid}). */
  private final String label;
  /** List of the HPO terms from the disease we want to display that are children of this category. Note that we
   * try to put the TermsIds in the {@link #subcatlist} if possible, and then they do not appear here.
   */
  private List<TermId> annotatedTerms;
  /** This can be used for second (or higher) level categories that function as subcategories in a Browser display. */
  private List<HpoCategory> subcatlist=new ArrayList<>();


  private HpoCategory(TermId id, String labl) {
    tid=id;
    label=labl;
    annotatedTerms=new ArrayList<>();
  }

  String getAnnotationString() {
      StringBuilder sb = new StringBuilder();
      sb.append(label +"\n");
      for (TermId tid : annotatedTerms) {
          sb.append("\t"+tid.getIdWithPrefix() + "\n");
      }
      return sb.toString();
  }



  private void setSubcategoryList(List<HpoCategory> sublist) {
    this.subcatlist=sublist;
  }
  /** @return true if at least one annotated term belongs to this category. */
  boolean hasAnnotation() { return annotatedTerms.size()>0;}

  public TermId getTid() {
    return tid;
  }

  public String getLabel() {
    return label;
  }

  public List<TermId> getAnnotatingTermIds() {
      return annotatedTerms;
  }

  public int getNumberOfAnnotations() { return annotatedTerms.size(); }


  void addAnnotatedTerm(TermId tid, Ontology ontology){
        annotatedTerms.add(tid);
  }

    @Override
    public boolean equals(Object that) {
        if (that == null) return false;
        if (!(that instanceof HpoCategory)) return false;
        HpoCategory otherHpoCategory = (HpoCategory) that;

        return this.tid.equals(otherHpoCategory.tid);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + tid.hashCode();
        return result;
    }



    public static class Builder {

    private final TermId tid;
    private String label;
    private ImmutableList.Builder<HpoCategory> builder=new ImmutableList.Builder<>();



    Builder(TermId id, String labl) {
      this.tid=id;
      this.label=labl;
    }

    Builder subcategory(TermId id , String label) {
      HpoCategory subcat=new HpoCategory(id,label);
      builder.add(subcat);
      return this;
    }

    public HpoCategory build() {
      HpoCategory cat = new HpoCategory(this.tid,this.label);
      cat.setSubcategoryList(builder.build());
      return cat;

    }



  }


}
