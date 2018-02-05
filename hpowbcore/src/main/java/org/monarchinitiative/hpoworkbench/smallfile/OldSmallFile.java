package org.monarchinitiative.hpoworkbench.smallfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.exception.HPOException;

import static org.monarchinitiative.hpoworkbench.smallfile.SmallFileQCCode.*;

/**
 * The HPO asnotations are currently distribued acorss roughly 7000 "small files", which were created between 2009 and 2017.
 * We want to unify and extend the format for these files. Thiu class represents a single "old" small file. THe app will
 * transform these objects into {@link V2SmallFile} objects. Note that the "logic" for transformung small files has been
 * coded in the {@link OldSmallFileEntry} class, and this class basically just identifies the column indices and splits up the
 * lines into corresponding fields. THere is some variability in the nameing of columns (e.g., Sex and SexID), and this
 * class tries to figure that out.
 * @author Peter Robinson
 * Created by peter on 1/20/2018.
 */
public class OldSmallFile {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int UNINITIALIZED=-42;
    private int n_fields;
    private int DISEASE_ID_INDEX=UNINITIALIZED;
    private int DISEASE_NAME_INDEX=UNINITIALIZED;
    private int GENE_ID_INDEX=UNINITIALIZED;
    private int GENE_NAME_INDEX=UNINITIALIZED;
    private int GENOTYPE_INDEX=UNINITIALIZED;
    private int GENE_SYMBOL_INDEX=UNINITIALIZED;
    private int PHENOTYPE_ID_INDEX=UNINITIALIZED;
    private int PHENOTYPE_NAME_INDEX=UNINITIALIZED;
    private int AGE_OF_ONSET_ID_INDEX=UNINITIALIZED;
    private int AGE_OF_ONSET_NAME_INDEX=UNINITIALIZED;
    private int EVIDENCE_ID_INDEX=UNINITIALIZED;
    private int EVIDENCE_NAME_INDEX=UNINITIALIZED;
    private int FREQUENCY_INDEX=UNINITIALIZED;
    private int SEX_ID_INDEX=UNINITIALIZED;
    private int SEX_NAME_INDEX=UNINITIALIZED;
    /** Some entries have just "Sex" with no ID/Name */
    private int SEX_INDEX=UNINITIALIZED;
    private int NEGATION_ID_INDEX=UNINITIALIZED;
    private int NEGATION_NAME_INDEX=UNINITIALIZED;
    private int DESCRIPTION_INDEX=UNINITIALIZED;
    private int PUB_INDEX=UNINITIALIZED;
    private int ASSIGNED_BY_INDEX=UNINITIALIZED;
    private int DATE_CREATED_INDEX=UNINITIALIZED;
    private int ENTITY_ID_INDEX=UNINITIALIZED;
    private int ENTITY_NAME_IDX=UNINITIALIZED;
    /** SOme entries just have "Evidence"??? */
    private int EVIDENCE_INDEX=UNINITIALIZED;
    private int QUALITY_ID_INDEX=UNINITIALIZED;
    private int QUALITY_NAME_INDEX=UNINITIALIZED;
    private int ADDL_ENTITY_ID_INDEX=UNINITIALIZED;
    private int ADDL_ENTITY_NAME_INDEX=UNINITIALIZED;
    private int ABNORMAL_ID_INDEX=UNINITIALIZED;
    private int ABNORMAL_NAME_INDEX=UNINITIALIZED;
    private int ORTHOLOGS_INDEX=UNINITIALIZED;

    private BiMap<FieldType,Integer> fields2index;
    /** A list of {@link org.monarchinitiative.hpoworkbench.smallfile.OldSmallFileEntry} objects, each of which corresponds
     * to a line in the old small file (except for the header). */
    private List<OldSmallFileEntry> entrylist=new ArrayList<>();
    private final String pathToOldSmallFile;

    public OldSmallFile(String path) {
        pathToOldSmallFile=path;
        parse();
    }

    public String getBasename() {
        return new File(pathToOldSmallFile).getName();
    }



    private void parse() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(pathToOldSmallFile));
            String line;
            line=br.readLine();// the header
            processHeader(line); // identify the indices
            while ((line=br.readLine())!=null ){
                if (line.trim().isEmpty()) continue; // skip empty lines
                try {
                    processContentLine(line);
                } catch (HPOException e) {
                    LOGGER.error(e.getMessage() + "\nOffending line:\n"+line);
                    //System.exit(1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<OldSmallFileEntry> getEntrylist() {
        return entrylist;
    }

    public int getN_corrected_date() {
        return n_corrected_date;
    }

    public int getN_no_evidence() {
        return n_no_evidence;
    }

    public int getN_gene_data() {
        return n_gene_data;
    }

    public int getN_alt_id() {
        return n_alt_id;
    }

    public int getN_update_label() {
        return n_update_label;
    }

    public int getN_created_modifier() {
        return n_created_modifier;
    }

    public int getN_EQ_item() {
        return n_EQ_item;
    }

    private void processContentLine(String line) throws HPOException {
        String F[]=line.split("\t");
        if (F.length != n_fields) {
            throw new HPOException("We were expecting " + n_fields + " fields but got only " + F.length + "for line:\n"+line);
        }
        OldSmallFileEntry entry = new OldSmallFileEntry();
        //System.out.print(line);
        for (int i=0;i<F.length;i++) {
            FieldType typ = this.fields2index.inverse().get(i);
           // LOGGER.trace("findingh typ="+typ.toString());
            switch (typ) {
                case DISEASE_ID:
                    entry.addDiseaseId(F[i]);
                    break;
                case DISEASE_NAME:
                    entry.addDiseaseName(F[i]);
                    break;
                case GENE_ID:
                    entry.addGeneId(F[i]);
                    break;
                case GENE_NAME:
                    entry.setGeneName(F[i]);
                    break;
                case GENOTYPE:
                    entry.setGenotype(F[i]);
                    break;
                case GENE_SYMBOL:
                    entry.setGenesymbol(F[i]);
                    break;
                case PHENOTYPE_ID:
                    entry.setPhenotypeId(F[i]);
                    break;
                case PHENOTYPE_NAME:
                    entry.setPhenotypeName(F[i]);
                    break;
                case AGE_OF_ONSET_ID:
                    entry.setAgeOfOnsetId(F[i]);
                    break;
                case AGE_OF_ONSET_NAME:
                    entry.setAgeOfOnsetName(F[i]);
                    break;
                case EVIDENCE_ID:
                    entry.setEvidenceId(F[i]);
                    break;
                case EVIDENCE_NAME:
                    entry.setEvidenceName(F[i]);
                    break;
                case FREQUENCY:
                    entry.setFrequencyString(F[i]);
                    break;
                case SEX_ID:
                    entry.setSexID(F[i]);
                    break;
                case SEX_NAME:
                    entry.setSexName(F[i]);
                    break;
                case NEGATION_ID:
                    entry.setNegationID(F[i]);
                    break;
                case NEGATION_NAME:
                    entry.setNegationName(F[i]);
                    break;
                case DESCRIPTION:
                    entry.setDescription(F[i]);
                    break;
                case PUB:
                    entry.setPub(F[i]);
                    break;
                case ASSIGNED_BY:
                    entry.setAssignedBy(F[i]);
                    break;
                case DATE_CREATED:
                    entry.setDateCreated(F[i]);
                    break;
                case ADDL_ENTITY_ID:
                    entry.setAddlEntityId(F[i]);
                    break;
                case ADDL_ENTITY_NAME:
                    entry.setAddlEntityName(F[i]);
                    break;
                case ENTITY_ID:
                    entry.setEntityId(F[i]);
                    break;
                case ENTITY_NAME:
                    entry.setEntityName(F[i]);
                    break;
                case QUALITY_ID:
                    entry.setQualityId(F[i]);
                    break;
                case QUALITY_NAME:
                    entry.setQualityName(F[i]);
                    break;
                case EVIDENCE:
                    entry.setEvidence(F[i]);
                    break;
                case ABNORMAL_ID:
                    entry.setAbnormalId(F[i]);
                    break;
                case ABNORMAL_NAME:

                    entry.setAbnormalName(F[i]);
                    break;
                case SEX:
                    entry.setSex(F[i]);
                    break;
                default:
                    System.err.println("Need to add for id="+typ);
                    System.exit(1);
            }
        }
        // When we get here, we have added all of the fields of the OLD file. We will do a Q/C check and
        // record any "repair" jobs that needed to be performed.
        Set<SmallFileQCCode> qcItemList = entry.doQCcheck();

        tallyQCitems(qcItemList,line);
        if (entry.hasQCissues()) {
            // if there was a QC issue, then the old line will have been output to the LOG together
            // with an indication of the issue. Therefore, we output the corresponding new line to LOG
            // so we can perform checking.
            // Note that the actual output of the new lines is done by thbe V2SmallFile class and not here.
            V2SmallFileEntry v2entry = new V2SmallFileEntry(entry);
            LOGGER.trace("V2 entry: " + v2entry.getRow());

        }
        entrylist.add(entry);
    }


    private boolean hasQCissue=false;

    private int n_corrected_date=0;
    private int n_no_evidence=0;
    private int n_gene_data=0;
    private int n_alt_id=0;
    private int n_update_label=0;
    private int n_created_modifier=0;
    private int n_EQ_item=0;


    public boolean hasQCissue() {
        return hasQCissue;
    }

    private void tallyQCitems(Set<SmallFileQCCode> qcitems, String line) {
        if (qcitems.size()==0)return;
        for (SmallFileQCCode qcode : qcitems) {
            if (! qcode.equals(UPDATED_DATE_FORMAT)) this.hasQCissue=true;

            switch (qcode) {
                case UPDATED_DATE_FORMAT:
                    n_corrected_date++;
                    break;// do not output log entry about date format
                case DID_NOT_FIND_EVIDENCE_CODE:
                   n_no_evidence++;
                    LOGGER.error(String.format("%s:%s",DID_NOT_FIND_EVIDENCE_CODE.name(),line));
                    break;
                case GOT_GENE_DATA:
                    n_gene_data++;
                    LOGGER.trace(String.format("%s:%s",GOT_GENE_DATA.name(),line));
                    break;
                case UPDATING_ALT_ID:
                    n_alt_id++;
                    LOGGER.trace(String.format("%s:%s",UPDATING_ALT_ID.name(),line));
                    break;
                case UPDATING_HPO_LABEL:
                    n_update_label++;
                    LOGGER.trace(String.format("%s:%s",UPDATING_HPO_LABEL.name(),line));
                    break;
                case CREATED_MODIFER:
                    n_created_modifier++;
                    LOGGER.trace(String.format("%s:%s",CREATED_MODIFER.name(),line));
                    break;
                case GOT_EQ_ITEM:
                    n_EQ_item++;
                    LOGGER.trace(String.format("%s:%s",GOT_EQ_ITEM.name(),line));
                    break;
            }
        }


    }




    public int getN_fields() {
        return n_fields;
    }

    public int getDISEASE_ID_INDEX() {
        return DISEASE_ID_INDEX;
    }

    public int getDISEASE_NAME_INDEX() {
        return DISEASE_NAME_INDEX;
    }

    public int getGENE_ID_INDEX() {
        return GENE_ID_INDEX;
    }

    public int getGENE_NAME_INDEX() {
        return GENE_NAME_INDEX;
    }

    public int getGENOTYPE_INDEX() {
        return GENOTYPE_INDEX;
    }

    public int getGENE_SYMBOL_INDEX() {
        return GENE_SYMBOL_INDEX;
    }

    public int getPHENOTYPE_ID_INDEX() {
        return PHENOTYPE_ID_INDEX;
    }

    public int getPHENOTYPE_NAME_INDEX() {
        return PHENOTYPE_NAME_INDEX;
    }

    public int getAGE_OF_ONSET_ID_INDEX() {
        return AGE_OF_ONSET_ID_INDEX;
    }

    public int getAGE_OF_ONSET_NAME_INDEX() {
        return AGE_OF_ONSET_NAME_INDEX;
    }

    public int getEVIDENCE_ID_INDEX() {
        return EVIDENCE_ID_INDEX;
    }

    public int getEVIDENCE_NAME_INDEX() {
        return EVIDENCE_NAME_INDEX;
    }

    public int getFREQUENCY_INDEX() {
        return FREQUENCY_INDEX;
    }

    public int getSEX_ID_INDEX() {
        return SEX_ID_INDEX;
    }

    public int getSEX_NAME_INDEX() {
        return SEX_NAME_INDEX;
    }

    public int getNEGATION_ID_INDEX() {
        return NEGATION_ID_INDEX;
    }

    public int getNEGATION_NAME_INDEX() {
        return NEGATION_NAME_INDEX;
    }

    public int getDESCRIPTION_INDEX() {
        return DESCRIPTION_INDEX;
    }

    public int getPUB_INDEX() {
        return PUB_INDEX;
    }

    public int getASSIGNED_BY_INDEX() {
        return ASSIGNED_BY_INDEX;
    }

    public int getDATE_CREATED_INDEX() {
        return DATE_CREATED_INDEX;
    }

    public int getSEX_INDEX() {
        return SEX_INDEX;
    }

    public int getENTITY_ID_INDEX() {
        return ENTITY_ID_INDEX;
    }

    public int getENTITY_NAME_IDX() {
        return ENTITY_NAME_IDX;
    }

    public int getEVIDENCE_INDEX() {
        return EVIDENCE_INDEX;
    }

    public int getQUALITY_ID_INDEX() {
        return QUALITY_ID_INDEX;
    }

    public int getQUALITY_NAME_INDEX() {
        return QUALITY_NAME_INDEX;
    }

    public int getADDL_ENTITY_ID_INDEX() {
        return ADDL_ENTITY_ID_INDEX;
    }

    public int getADDL_ENTITY_NAME_INDEX() {
        return ADDL_ENTITY_NAME_INDEX;
    }

    public int getABNORMAL_ID_INDEX() {
        return ABNORMAL_ID_INDEX;
    }

    public int getABNORMAL_NAME_INDEX() {
        return ABNORMAL_NAME_INDEX;
    }

    public int getORTHOLOGS_INDEX() {
        return ORTHOLOGS_INDEX;
    }


    private void processHeader(String header) {
        String A[] = header.split("\t");
        n_fields=A.length;
        ImmutableBiMap.Builder builder = new ImmutableBiMap.Builder();
        for (int i=0;i<n_fields;i++) {
            FieldType fieldtype= FieldType.string2fields(A[i]);
            builder.put(fieldtype,i);
            switch (A[i]) {
                case "Disease ID":
                    DISEASE_ID_INDEX=i; break;
                case "Disease Name":
                    DISEASE_NAME_INDEX=i;break;
                case "Gene ID":
                    GENE_ID_INDEX=i;
                    break;
                case "Gene Name":
                    GENE_NAME_INDEX=i;break;
                case "Genotype":
                    GENOTYPE_INDEX=i;break;
                case "Gene Symbol(s)":
                    GENE_SYMBOL_INDEX=i;break;
                case "Phenotype ID":
                    PHENOTYPE_ID_INDEX=i;break;
                case "Phenotype Name":
                    PHENOTYPE_NAME_INDEX=i;break;
                case "Age of Onset ID":
                    AGE_OF_ONSET_ID_INDEX=i;break;
                case "Age of Onset Name":
                    AGE_OF_ONSET_NAME_INDEX=i; break;
                case "Evidence ID":
                    EVIDENCE_ID_INDEX=i;break;
                case "Evidence Name":
                    EVIDENCE_NAME_INDEX=i;break;
                case "Frequency":
                    FREQUENCY_INDEX=i;
                case "Sex ID":
                    SEX_ID_INDEX=i;break;
                case "Sex Name":
                    SEX_NAME_INDEX=i;break;
                case "Negation ID":
                    NEGATION_ID_INDEX=i;break;
                case "Negation Name":
                    NEGATION_NAME_INDEX=i;break;
                case "Description":
                    DESCRIPTION_INDEX=i;break;
                case "Pub":
                    PUB_INDEX=i;break;
                case "Assigned by":
                    ASSIGNED_BY_INDEX=i;break;
                case "Date Created":
                    DATE_CREATED_INDEX=i;break;
                case "Entity ID":
                    ENTITY_ID_INDEX=i;break;
                case "Entity Name":
                    ENTITY_NAME_IDX=i;break;
                case "Add'l Entity ID":
                    ADDL_ENTITY_ID_INDEX=i;break;
                case "Add'l Entity Name":
                    ADDL_ENTITY_NAME_INDEX=i;break;
                case "Quality ID":
                    QUALITY_ID_INDEX=i;break;
                case "Quality Name":
                    QUALITY_NAME_INDEX=i;break;
                case "Evidence":
                    EVIDENCE_INDEX=i;break;
                case "Abnormal ID":
                    ABNORMAL_ID_INDEX=i;break;
                case "Abnormal Name":
                    ABNORMAL_NAME_INDEX=i;break;
                case "Sex":
                    SEX_INDEX=i;break;
                case "Orthologs":
                    ORTHOLOGS_INDEX=i;break;
                default:
                    LOGGER.fatal("Did not recognize header field \""+A[i]+"\"");
                    LOGGER.fatal("Terminating, please check OldSmallFile");
                    System.exit(1);
            }
            this.fields2index=builder.build();
        }
    }



}
