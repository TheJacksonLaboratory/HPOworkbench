package org.monarchinitiative.hpoworkbench.smallfile;

/**
 * Created by peter on 1/20/2018.
 */
public enum FieldType {
    DISEASE_ID("Disease ID"),
    DISEASE_NAME("Disease Name"),
     GENE_ID("Gene ID"),
     GENE_NAME("Gene Name"),
     GENOTYPE("Genotype"),
     GENE_SYMBOL("Gene Symbol"),
     PHENOTYPE_ID("Phenotype ID"),
     PHENOTYPE_NAME("Phenotype Name"),
     AGE_OF_ONSET_ID("Age of onset ID"),
     AGE_OF_ONSET_NAME("Age of onset Name"),
     EVIDENCE_ID("Evidence ID"),
     EVIDENCE_NAME("Evidence Name"),
     FREQUENCY("Frequency"),
     SEX_ID("Sex ID"),
     SEX_NAME("Sex Name"),
      SEX("Sex"),
     NEGATION_ID("Negation ID"),
     NEGATION_NAME("Negation Name"),
     DESCRIPTION("Description"),
     PUB("Pub"),
     ASSIGNED_BY("Assigned By"),
     DATE_CREATED("Date Created"),
     ENTITY_ID("Entity ID"),
     ENTITY_NAME("Entity Name"),
     EVIDENCE("Evidence"),
     QUALITY_ID("Quality ID"),
     QUALITY_NAME("Quality Name"),
     ADDL_ENTITY_ID("Add'l Entity ID"),
     ADDL_ENTITY_NAME("Add'l Entity Name"),
     ABNORMAL_ID("Abnormal ID"),
     ABNORMAL_NAME("Abnormal Name"),
     ORTHOLOGS("Orthologs");

    private final String name;

    FieldType(String n)  {
        this.name=n;
    }

    public static FieldType string2fields(String s) {
        switch (s) {
            case "Disease ID":
                return DISEASE_ID;
            case "Disease Name":
                return DISEASE_NAME;
            case "Gene ID":
                return GENE_ID;
            case "Gene Name":
                return GENE_NAME;
            case "Genotype":
                return GENOTYPE;
            case "Gene Symbol(s)":
                return GENE_SYMBOL;
            case "Phenotype ID":
                return PHENOTYPE_ID;
            case "Phenotype Name":
                return PHENOTYPE_NAME;
            case "Age of Onset ID":
                return AGE_OF_ONSET_ID;
            case "Age of Onset Name":
                return AGE_OF_ONSET_NAME;
            case "Evidence ID":
                return EVIDENCE_ID;
            case "Evidence Name":
                return EVIDENCE_NAME;
            case "Frequency":
                return FREQUENCY;
            case "Sex ID":
                return SEX_ID;
            case "Sex Name":
                return SEX_NAME;
            case "Sex":
                return SEX;
            case "Negation ID":
                return NEGATION_ID;
            case "Negation Name":
                return NEGATION_NAME;
            case "Description":
                return DESCRIPTION;
            case "Pub":
                return PUB;
            case "Assigned by":
                return ASSIGNED_BY;
            case "Date Created":
                return DATE_CREATED;
            case "Entity ID":
                return ENTITY_ID;
            case "Entity Name":
                return ENTITY_NAME;
            case "Evidence":
                return EVIDENCE;
            case "Quality ID":
                return QUALITY_ID;
            case "Quality Name":
                return QUALITY_NAME;
            case "Add'l Entity ID":
                return ADDL_ENTITY_ID;
            case "Add'l Entity Name":
                return ADDL_ENTITY_NAME;
            case "Abnormal ID":
                return ABNORMAL_ID;
            case "Abnormal Name":
                return ABNORMAL_NAME;
            case "Orthologs":
                return ORTHOLOGS;
            default:
                System.err.print("Did not recognize header field \"" + s + "\". Please see FieldType.java");
                System.exit(1);
        }
        return null; // never reached
    }

}
