package org.monarchinitiative.hpoworkbench.cmd;


import com.beust.jcommander.Parameter;


import java.util.HashMap;
import java.util.Map;

/**
 * Super class for all commands, i.e. the classes implementing one HpoWorkbench execution step.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public abstract class HPOCommand {

    public abstract String getName();

    @Parameter(names={"-d","--download"},description = "directory to download HPO data")
    protected String downloadDirectory="data";
    @Parameter(names={"-a", "--annot"},description = "path to phenol.hpoa")
    protected String annotpath="data/phenotype.hpoa";
    @Parameter(names={"-h", "--hpo"}, description = "path to hp.obo")
    protected String hpopath ="data/hp.obo";

    protected Map<String,String> defaults=new HashMap<>();

    /**
     * Function for the execution of the command.
     */
    public abstract void run();



    public HPOCommand setDefaultValue(String key,String value) {
        defaults.put(key,value);
        return this;
    }

    public String toString() {
        return getName();
    }

}