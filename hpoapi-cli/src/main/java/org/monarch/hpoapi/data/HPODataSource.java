package org.monarch.hpoapi.data;


import org.ini4j.Profile.Section;

import com.google.common.collect.ImmutableList;


/**
 * {@link DataSource} implementation for io from UCSC.
 *
 * @author Peter Robinson, adapted from Jannovar code by <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 */
final class HPODataSource extends DataSource {

    /** expected keys in io source configuration file. For the HPO phenotype, this is obo and annotation */
    private final ImmutableList<String> requiredKeys = ImmutableList.of("obo","annotation");

    HPODataSource(DatasourceOptions options, Section iniSection) throws InvalidDataSourceException {
        super(options, iniSection);

        checkURLs();
    }

    @Override
    public PhenotypeDataFactory getDataFactory() {
        return new HPODataFactory(options, this, iniSection, true);
    }

    @Override
    protected ImmutableList<String> getRequiredKeys() {
        return requiredKeys;
    }


    @Override
    public String toString() {
        String s = String.format("HPODataSource: %s",iniSection);
        return s;
    }
}