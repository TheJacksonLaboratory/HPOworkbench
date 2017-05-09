package org.monarch.hpoapi.cmd;


import org.ini4j.Profile.Section;

import com.google.common.collect.ImmutableList;


/**
 * {@link DataSource} implementation for data from UCSC.
 *
 * @author Peter Robinson, adapted from Jannovar code by <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 */
final class OBODataSource extends DataSource {

    /** expected keys in data source configuration file */
    private final ImmutableList<String> urlKeys = ImmutableList.of("url");

    OBODataSource(DatasourceOptions options, Section iniSection) throws InvalidDataSourceException {
        super(options, iniSection);

        checkURLs();
    }

    @Override
    public HPODataFactory getDataFactory() {
        return new OBODataFactory(options, this, iniSection);
    }

    @Override
    protected ImmutableList<String> getURLKeys() {
        return urlKeys;
    }
}