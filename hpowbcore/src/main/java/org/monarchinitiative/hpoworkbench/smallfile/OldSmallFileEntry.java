package org.monarchinitiative.hpoworkbench.smallfile;

import com.github.phenomics.ontolib.formats.hpo.HpoFrequency;
import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.formats.hpo.HpoTermRelation;
import com.github.phenomics.ontolib.graph.data.Edge;
import com.github.phenomics.ontolib.ontology.data.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.exception.HPOException;

import java.util.*;
import java.util.stream.Collectors;

import static org.monarchinitiative.hpoworkbench.smallfile.DiseaseDatabase.DECIPHER;
import static org.monarchinitiative.hpoworkbench.smallfile.DiseaseDatabase.OMIM;
import static org.monarchinitiative.hpoworkbench.smallfile.DiseaseDatabase.ORPHANET;
import static org.monarchinitiative.hpoworkbench.util.DateUtil.convertToCanonicalDateFormat;

/**
 * Created by peter on 1/20/2018.
 * This class is inteded to take data from a single line of an "old" small file entry.
 */
public class OldSmallFileEntry {
    private static final Logger LOGGER = LogManager.getLogger();
    private DiseaseDatabase database=null;
    private String diseaseID=null;
    private String diseaseName=null;
    /** gene ID. We will delete this field for the new version. */
    private String geneID=null;
    /** Gene symbol. We will delete this field for the new version */
    private String geneName=null;
    /** We will delete the genotype field. */
    private String genotype=null;
    /** We will delete the gene symbol. */
    private String genesymbol=null;
    /** THe HPO id */
    private TermId phenotypeId=null;
    /** THe HPO label */
    private String phenotypeName=null;
    /** THis should be an HPO Id */
    private TermId ageOfOnsetId=null;
    /** Name of HPO age of onset term. */
    private String ageOfOnsetName=null;

    private String evidenceID=null;

    private String evidenceName=null;

    private String frequencyString =null;

    private TermId frequencyId= null;

    // Frequency Ids
    private static final TermPrefix HP_PREFIX=new ImmutableTermPrefix("HP");
    private static final TermId FrequencyRoot=new ImmutableTermId(HP_PREFIX,"0040279");
    private static final TermId FREQUENT= HpoFrequency.FREQUENT.toTermId();
    private static final TermId VERY_FREQUENT = HpoFrequency.VERY_FREQUENT.toTermId();
    private static final TermId OBLIGATE = new ImmutableTermId(HP_PREFIX,"0040280");
    private static final TermId OCCASIONAL = HpoFrequency.OCCASIONAL.toTermId();
    private static final TermId EXCLUDED = HpoFrequency.EXCLUDED.toTermId();
    private static final TermId VERY_RARE= HpoFrequency.VERY_RARE.toTermId();
    /** If present, a limitation to MALE or FEMALE. */
    private String sexID=null;
    /** Redundant with {@link #sexID}. */
    private String sexName=null;

    private String sex=null;

    private final static String MALE_CODE="Male";
    private final static String FEMALE_CODE="Female";
    /** If present, "NOT" */
    private String negationID=null;
    /** Redundant with {@link #negationID}. */
    private String negationName=null;

    private String description=null;
    /** This was not present in the old small file but will be created here if possible from the Description field. */
    private Set<TermId> modifierset=new HashSet<>();
    /** The source of the assertion, often a string such as PMID:123 or OMIM:100123 */
    private String pub=null;
    /** The biocurator */
    private String assignedBy=null;
    /* The date the annotation was first created. */
    private String dateCreated=null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String entityId=null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String entityName=null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String qualityId=null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String qualityName=null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String addlEntityName=null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String addlEntityId=null;
    /** Some entries have just evidence rather than evidenceId and evidenceName. We do the best we can to get one evidence code but
     * looking at all three fields one after the other, with evidenceId being prefered, then evidenceName, then evidence */
    private String evidence=null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String abnormalId=null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String abnormalName=null;
    private String othologs=null;

     private static HpoOntology ontology=null;
     private static Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology=null;
     private static Ontology<HpoTerm, HpoTermRelation> abnormalPhenoSubOntology=null;

     private static Map<String, TermId> modifier2TermId=new HashMap<>();


     public OldSmallFileEntry(){
    }

    public static void setOntology(HpoOntology ont,Ontology<HpoTerm, HpoTermRelation> inh,Ontology<HpoTerm, HpoTermRelation> phe ){
         ontology=ont;
         inheritanceSubontology=inh;
         abnormalPhenoSubOntology=phe;
         findModifierTerms();
    }

    /** Creates a map for all terms in the Clinical modifier subhierarchy (which
     * starts from HP:0012823). The keys are lower-case versions of the Labels,
     * and the values are the corresponding TermIds. See {@link #modifier2TermId}.
     */
    private static void findModifierTerms() {
         TermId modifier = new ImmutableTermId(HP_PREFIX,"0012823");
         Stack<TermId> stack = new Stack<>();
         Set<TermId> descendents=new HashSet<>();
         stack.push(modifier);
         while (! stack.empty()) {
             TermId parent = stack.pop();
             descendents.add(parent);
             Set<TermId> kids = getChildren(parent);
             kids.stream().forEach(k -> stack.push(k));
         }

         for (TermId tid : descendents) {
             String label = ontology.getTermMap().get(tid).getName().toLowerCase();
             modifier2TermId.put(label,tid);
         }
    }

    private static Set<TermId> getChildren(TermId parent) {
        Set<TermId> kids = new HashSet<>();
        Iterator it = ontology.getGraph().inEdgeIterator(parent);
        while (it.hasNext()) {
            Edge<TermId> sourceEdge=(Edge<TermId>)it.next();
            TermId source=sourceEdge.getSource();
            kids.add(source);
        }
        return kids;
    }




    public void addDiseaseId(String id) {
        if (id.startsWith("OMIM")) {
            this.database=OMIM;
            this.diseaseID=id;
        } else if (id.startsWith("ORPHA")) {
            this.database=ORPHANET;
            this.diseaseID=id;
        } else if (id.startsWith("DECIPHER")) {
            database=DECIPHER;
            this.diseaseID=id;
        } else {
            LOGGER.fatal("Did not recognize disease database for " + id);
            System.exit(1);
        }
    }

    public void addDiseaseName(String n) {
        this.diseaseName=n;
        if (diseaseName.length()<1) {
            LOGGER.trace("Error zero length name ");
            System.exit(1);
        }
    }


    public void addGeneId(String id) {
        if (id==null) return;
        LOGGER.trace("Adding gene id: " + id);
        geneID=id;
    }

    public void setGeneName(String name) { geneName=name;}
    public void setGenotype(String gt) { genotype=gt;}
    public void setGenesymbol(String gs) { genesymbol=gs;}
    public void setPhenotypeId(String id) throws HPOException {
        if (! id.startsWith("HP:") ) {
            throw new HPOException("Bad phenotype id prefix: " + id);
        }
        if (! (id.length()==10) ) {
            throw new HPOException("Bad length for phenotype id:  "+id);
        }
        if (! checkTermValid(id)) {
            throw new HPOException("Term not valid TODO");
        }
        this.phenotypeId=getHpoTermId(id);
    }
    public void setPhenotypeName(String name) { phenotypeName=name;}

    public void setAgeOfOnsetId(String id) throws HPOException {
        if (id==null || id.length()==0) { return; }// no age of onset
        if (! id.startsWith("HP:") ) {
            LOGGER.fatal("Bad phenotype id prefix: " + id);
            System.exit(1);
        }
        if (! (id.length()==10) ) {
            LOGGER.fatal("Bad length for phenotype id:  "+id);
            System.exit(1);
        }
        if (! isValidInheritanceTerm(id)) {
            LOGGER.fatal("Not a valid inheritance term....terminating program");
            System.exit(1);
        }
        ageOfOnsetId=getHpoTermId(id);
    }
    public void setAgeOfOnsetName(String name) {
        if (name==null || name.length()==0) return; // no age of onset (not required)
        this.ageOfOnsetName=name;
    }

    private boolean isValidInheritanceTerm(String id) {
        if (!id.startsWith("HP:")) {
            LOGGER.fatal("Invalid inhertiance term \"" + id +"\"");
            System.exit(1);
        }
        id = id.substring(3);
        TermId tid = new ImmutableTermId(HP_PREFIX,id);
        if (tid==null) {
            LOGGER.fatal("Could not create inheritance termid");
            System.exit(1);
        }
        if (ontology==null) {
            LOGGER.fatal("Ontology is null");
            System.exit(1);
        }
        // TODO run with inheritance obnlyu
        if (! ontology.getTermMap().containsKey(tid)) {
            LOGGER.fatal("Term " + tid.getIdWithPrefix() + " was not a valid inheritance term");
            System.exit(1);
        }
        return  true;
    }

    private boolean checkTermValid(String id) {
        //TODO
        return true;
    }

    private void checkEvidence(String evi) throws
    HPOException{
        if ( (! evi.equals("IEA") ) && (!evi.equals("PCS")) &&
                (!evi.equals("TAS"))) {
            throw new HPOException("Bad evidence ID: " + evi);
        }
    }

    public void setEvidenceId(String id) throws HPOException {this.evidenceID=id; checkEvidence(evidenceID); }
    public void setEvidenceName(String name) throws HPOException { this.evidenceName=name; checkEvidence(evidenceName);}


    public void setFrequencyString(String freq) throws HPOException {
        if (freq==null || freq.length()==0) return; // not required!
        this.frequencyString =freq.trim();
        if (frequencyString.length()==0) return; //it ewas just a whitespace
        if (frequencyString.startsWith("HP:")) {
            LOGGER.fatal("NEVER HAPPENS, FREQUENCY WITH TERM");
            System.exit(1);
        } else if (Character.isDigit(frequencyString.charAt(0))) {
            // ok no op
        } else if (frequencyString.equalsIgnoreCase("very rare")) {
            this.frequencyId=VERY_RARE;
        } else if (frequencyString.equalsIgnoreCase("rare")) {
            this.frequencyId=VERY_RARE; //TODO IS THIS OK?
        } else if (frequencyString.equalsIgnoreCase("frequent")) {
            this.frequencyId=FREQUENT;
        } else if (frequencyString.equalsIgnoreCase("occasional")) {
            this.frequencyId=OCCASIONAL;
        } else if (frequencyString.equalsIgnoreCase("variable")) {
            this.frequencyId=FrequencyRoot; //TODO OK -- ?????
        } else if (frequencyString.equalsIgnoreCase("typical")) {
            this.frequencyId = FREQUENT; // TODO OK????????????
        } else if (frequencyString.equalsIgnoreCase("very frequent")) {
            this.frequencyId=VERY_FREQUENT;
        } else if (frequencyString.equalsIgnoreCase("common")) {
            this.frequencyId=FREQUENT; //OK ?????????????????????
         } else if (frequencyString.equalsIgnoreCase("hallmark")) {
            this.frequencyId=VERY_FREQUENT; // OK ?????????????
        } else if (frequencyString.equalsIgnoreCase("obligate")) {
            this.frequencyId=OBLIGATE;
        } else {
            LOGGER.fatal("BAD FREQ ID \"" + freq + "\"");
            System.exit(1);
            //throw new HPOException("Malformed frequencyString: \"" + freq + "\"");
        }
    }



    private TermId getHpoTermId(String id) throws HPOException {
        if (! (id.startsWith("HP:") && id.length()==10) ) {
            throw new HPOException("Malformed HPO id \""+id+"\"");
        }
        TermId tid = new ImmutableTermId(HP_PREFIX,id.substring(3));
        if (! ontology.getTermMap().containsKey(tid)) {
            throw new HPOException("Could not find tid in map "+ tid.getIdWithPrefix());
        }
        return tid;
    }



    public void setSexID(String id) throws HPOException {
        if (id==null || id.length()==0) return;//oik, not required
        if (id.equalsIgnoreCase("MALE"))
            sexID=MALE_CODE;
        else if  (id.equalsIgnoreCase("FEMALE"))
            sexID=FEMALE_CODE;
        else
            throw new HPOException("Did not recognize Sex ID: " + id);
    }

    public void setSexName(String name) throws HPOException {
        if (name==null || name.length()==0) return;//oik, not required
        if (name.equalsIgnoreCase("MALE"))
            sexID=MALE_CODE;
        else if  (name.equalsIgnoreCase("FEMALE"))
            sexID=FEMALE_CODE;
        else
            throw new HPOException("Did not recognize Sex Name: " + name);
    }

    public void setNegationID(String id) throws HPOException {
        if (id==null||id.length()==0) return;
        if (id.equalsIgnoreCase("NOT")) { negationID="NOT"; }
        else  throw new HPOException("Malformed negation ID: \""+ id +"\"");
    }

     public void setNegationName(String name) throws HPOException {
         if (name == null || name.length() == 0) return;
         if (name.equalsIgnoreCase("NOT")) {
             negationID = "NOT";
         } else throw new HPOException("Malformed negation Name: \"" + name + "\"");
     }

    /** In some case, the Description field will contain a modifer such as mild. If we get an exact text match, then
     * add it to the modifer field but remove it from the description
     * @param d
     */
     public void setDescription(String d) {
         List<String> descriptionList=new ArrayList<>();
         if (d.indexOf(";")>0) {
             String A[]=d.split(";");
             for (String a: A) {
                 if (a.startsWith("MODIFIER:")) {
                     String candidateModifier=a.substring(9).toLowerCase();
                     if (modifier2TermId.containsKey(candidateModifier)) {
                         modifierset.add(modifier2TermId.get(candidateModifier));
                     } else {
                         LOGGER.fatal("Could not identify modifer for "+ candidateModifier + ", terminating program....");
                         //residual.append(a + ";");
                         System.exit(1);
                     }
                 } else {
                     descriptionList.add(a);
                 }
             }
         }
         LOGGER.info("Size of descriptionList "+ descriptionList.size());
         LOGGER.info("Size of modifierset "+ modifierset.size());
        for (String s : modifier2TermId.keySet()) {
            if (s.equalsIgnoreCase(d)) {
                modifierset.add( modifier2TermId.get(d));
            }
        }
        // if we get here, we could not find a modifier
        description=descriptionList.stream().collect(Collectors.joining(";"));
    }
     public void setPub(String p) { pub=p;}

     public void setAssignedBy(String ab) { this.assignedBy=ab;}
     public void setDateCreated(String dc) {
        // TODO make all dates look like 2018-01-23
         this.dateCreated=dc;
     }

     public void setAddlEntityName(String n) { addlEntityName=n;}
     public void setAddlEntityId(String id) { addlEntityId=id;}
     public void setEntityId(String id) {entityId=id;}
     public void setEntityName(String name) {entityName=name;}
     public void setQualityId(String id) { qualityId=id;}
     public void setQualityName(String name) { qualityName=name;}
     public void setEvidence(String e) { evidence=e;}
     public void setAbnormalId(String id) { abnormalId=id;}
     public void setAbnormalName(String name) { abnormalName=name;}
     public void setSex(String s) throws HPOException{
        if (s==null) return;
        if (s.equalsIgnoreCase("MALE")) this.sex=MALE_CODE;
        else if (s.equalsIgnoreCase("FEMALE")) this.sex=FEMALE_CODE;
        else throw new HPOException("Did not recognize sex code "+ s);
     }

    public DiseaseDatabase getDatabase() {
        return database;
    }

    public String getDiseaseID() {
        return diseaseID;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public String getGeneID() {
        return geneID;
    }

    public String getGeneName() {
        return geneName;
    }

    public String getGenotype() {
        return genotype;
    }

    public String getGenesymbol() {
        return genesymbol;
    }

    public TermId getPhenotypeId() {
        return phenotypeId;
    }

    public String getPhenotypeName() {
        return phenotypeName;
    }

    public TermId getAgeOfOnsetId() {
        return ageOfOnsetId;
    }

    public String getAgeOfOnsetName() {
        return ageOfOnsetName;
    }

    public String getEvidenceID() {
        return evidenceID;
    }

    public String getEvidenceName() {
        return evidenceName;
    }

    public String getEvidence(){return evidence;}

    public String getFrequencyString() {
        return frequencyString;
    }

    public TermId getFrequencyId() {
        return frequencyId;
    }

    public String getSex() {
        if (sexID!=null) return sexID;
        else if (sexName!=null) return sexName;
        else if (sex!=null) return sex;
        else return "";
    }

    public String getNegation() {
        if (negationID!=null) return negationID;
        else if (negationName != null) return negationName;
        else return "";
    }
    public Set<TermId> getModifierSet() { return modifierset; }
    public String getModifierString(){
        if (modifierset==null || modifierset.isEmpty()) return "";
        else return modifierset.stream().map(TermId::getIdWithPrefix).collect(Collectors.joining(";"));
    }
    public String getDescription(){ return description;}
    public String getPub(){ return pub;}
    public String getAssignedBy() { return assignedBy;}
    /** Returns the date created, and transforms the date format to YYYY-MM-DD, e.g., 2009-03-23. */
    public String getDateCreated() { return convertToCanonicalDateFormat(dateCreated); }


}