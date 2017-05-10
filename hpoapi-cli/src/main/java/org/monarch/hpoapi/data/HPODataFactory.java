package org.monarch.hpoapi.data;

import org.ini4j.Profile.Section;

import org.monarch.hpoapi.association.AssociationContainer;
import org.monarch.hpoapi.data.DatasourceOptions;
import org.monarch.hpoapi.data.HPODataSource;
import org.monarch.hpoapi.data.PhenotypeDataFactory;
import org.monarch.hpoapi.io.OBOParserException;
import org.monarch.hpoapi.ontology.Ontology;


/**
 * Creation of {@link HPOData} objects from a {@link HPODataSource}.
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 */
final class HPODataFactory extends PhenotypeDataFactory {

    /**
     * Construct the factory with the given {@link HPODataSource}.
     *
     * @param options
     *            configuration for proxy settings
     * @param dataSource
     *            the io source to use.
     * @param iniSection
     *            {@link Section} with configuration from INI file
     */
    public HPODataFactory(DatasourceOptions options, HPODataSource dataSource, Section iniSection,
                          boolean printProgressBars) {
        super(options, dataSource, iniSection);
    }

    /**
     * @param targetDir
     *            path where the downloaded files are
     * @return {@link Ontology} object representing a phenotype ontology
     * * @throws OBOParserException
     *             on problems with parsing the obo file
     */
    protected Ontology parseOntology(String targetDir)
            throws OBOParserException {
        return null;

    }

    /**
     * @param targetDir
     *            path where the downloaded files are
     * @return {@link AssociationContainer} object representing list of associations to a phenotype ontology
     * @throws OBOParserException
     *             on problems with parsing the obo file
     */
    protected AssociationContainer parseAssociations(String targetDir)
            throws OBOParserException {
        return null;
    }

}