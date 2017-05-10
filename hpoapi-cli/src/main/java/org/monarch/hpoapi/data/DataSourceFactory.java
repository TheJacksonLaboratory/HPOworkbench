package org.monarch.hpoapi.data;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

import com.google.common.collect.ImmutableList;

/**
 * Factory class that allows the construction of {@link DataSource} objects as configured in INI files.
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>, <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
final public class DataSourceFactory {

    /**
     * {@link DatasourceOptions} object for proxy settings
     */
    private final DatasourceOptions options;
    /**
     * {@link Ini} object to use for loading io
     */
    private final ImmutableList<Ini> inis;

    /**
     * @param options      for proxy configuration
     * @param iniFilePaths path to INI file to load the io source config from
     * @throws InvalidDataSourceException on problems with the io source config file
     */
    public DataSourceFactory(DatasourceOptions options, List<String> iniFilePaths) throws InvalidDataSourceException {
        this.options = options;
        if (iniFilePaths==null){
            System.err.println("DataSourceFacotry.java: inifile paths null");
            System.exit(1);
        } else {
            System.err.println("Size of iniFIlePaths=" + iniFilePaths.size() + "\n");
            for (String s : iniFilePaths) {
                System.err.println("\t"+s);
            }
        }

        ImmutableList.Builder<Ini> inisBuilder = new ImmutableList.Builder<Ini>();
        for (String iniFilePath : iniFilePaths) {
            InputStream is;
            final String BUNDLE_PREFIX = "bundle://";
            if (iniFilePath.startsWith(BUNDLE_PREFIX)) {
                String strippedPath = iniFilePath.substring(BUNDLE_PREFIX.length());
                is = this.getClass().getResourceAsStream(strippedPath);
                if (is == null)
                    throw new InvalidDataSourceException("BUG: bundled file " + strippedPath + " not in JAR!");
            } else {
                try {
                    is = new FileInputStream(iniFilePath);
                } catch (FileNotFoundException e) {
                    throw new InvalidDataSourceException(
                            "Problem opening io source file " + iniFilePath + ": " + e.getMessage());
                }
            }
            Ini ini = new Ini();
            try {
                ini.load(is);
            } catch (InvalidFileFormatException e) {
                throw new InvalidDataSourceException("Problem loading io source file.", e);
            } catch (IOException e) {
                throw new InvalidDataSourceException("Problem loading io source file.", e);
            }
            inisBuilder.add(ini);
        }
        this.inis = inisBuilder.build();
    }

    /**
     * @return list of io source names
     */
    public ImmutableList<String> getNames() {
        ImmutableList.Builder<String> builder = new ImmutableList.Builder<String>();
        for (Ini ini : inis)
            for (String name : ini.keySet())
                if (ini.get(name).get("type") != null)
                    builder.add(name);
        return builder.build();
    }

    /**
     * Construct {@link DataSource}
     *
     * @param name key of the INI section to load the io source from
     * @return {@link DataSource} with io from the file
     * @throws InvalidDataSourceException if <code>name</code> could not be found in any io source config file
     */
    public DataSource getDataSource(String name) throws InvalidDataSourceException {
        for (Ini ini : inis) {
            if (!ini.keySet().contains(name))
                continue; // not found in io source
            Section section = ini.get(name);
            String type = section.fetch("type");
            if (type == null)
                throw new InvalidDataSourceException("Data source config does not have \"type\" key.");
            else if (type.equals("hpo"))
                return new HPODataSource(options, section);
            /*else if (type.equals("mpo"))

                // not implemented yet for Mammalian Phenotype Ontology

                */
            else
                throw new InvalidDataSourceException("Data source config has invalid \"type\" key: " + type);
        }

        throw new InvalidDataSourceException("Could not find io source " + name + " in any io source file.");
    }
}