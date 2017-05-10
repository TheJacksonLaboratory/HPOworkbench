package org.monarch.hpoapi.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Configuration for database-related commands in HPOAPI
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a> closely based on code by Manuel Holtgrewe for Jannovar.
 */
public class PhenotypeDBOptions extends PhenotypeBaseOptions {

    /**
     * paths to INI files to use for parsing
     */
    public List<String> dataSourceFiles = new ArrayList<>();

    public List<String> getDataSourceFiles() {
        return dataSourceFiles;
    }

    public void setDataSourceFiles(List<String> dataSourceFiles) {
        this.dataSourceFiles = dataSourceFiles;
    }

    /** See {@link PhenotypeDBListOptions} to understand how  {@link #dataSourceFiles} is being initialized. */
    @Override
    public void setFromArgs(Namespace args) throws CommandLineParsingException {
        super.setFromArgs(args);
        dataSourceFiles = args.getList("data_source_list");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("dataSourceFiles:");
        if (dataSourceFiles.size()>0){
            sb.append("\t" + dataSourceFiles.get(0) + "\n");
            for (int i=1;i<dataSourceFiles.size();++i) {
                sb.append("\t" + dataSourceFiles.get(i)+ "\n");
            }
        } else {
            sb.append("\tnone\n");
        }
        return "JannovarDBOptions [dataSourceFiles=" + sb.toString() + ", isReportProgress()=" + isReportProgress()
                + ", getHttpProxy()=" + getHttpProxy() + ", getHttpsProxy()=" + getHttpsProxy() + ", getFtpProxy()="
                + getFtpProxy() + "]";
    }

}
