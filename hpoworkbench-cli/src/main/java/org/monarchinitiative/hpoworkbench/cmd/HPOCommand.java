package org.monarchinitiative.hpoworkbench.cmd;


import picocli.CommandLine;


import java.util.HashMap;
import java.util.Map;

/**
 * Super class for all commands, i.e. the classes implementing one HpoWorkbench execution step.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public abstract class HPOCommand {


    @CommandLine.Option(names={"-d","--download"},description = "directory to download HPO data")
    protected String downloadDirectory="data";
    @CommandLine.Option(names={"-a", "--annot"},description = "path to phenol.hpoa")
    protected String annotpath="data/phenotype.hpoa";
    @CommandLine.Option(names={"--hpo"}, description = "path to hp.obo")
    protected String hpopath ="data/hp.obo";

    protected Map<String,String> defaults=new HashMap<>();


    public HPOCommand setDefaultValue(String key,String value) {
        defaults.put(key,value);
        return this;
    }


}