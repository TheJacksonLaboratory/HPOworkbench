package org.monarchinitiative.hpoworkbench.cmd;


import com.beust.jcommander.Parameter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

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
    protected String hpopath ="data/hpo.obo";

    protected Map<String,String> defaults=new HashMap<>();


    /**
     * Set log level, depending on this.verbosity.
     */
    protected void setLogLevel(int verbosity) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();

        if (verbosity <= 1)
            conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.INFO);
        else if (verbosity <= 2)
            conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.DEBUG);
        else
            conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.TRACE);

        ctx.updateLoggers(conf);
    }

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