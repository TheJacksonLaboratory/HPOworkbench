package org.monarchinitiative.hpoapi.cmd;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import org.monarchinitiative.hpoapi.argparser.ArgumentParserException;
import org.monarchinitiative.hpoapi.exception.HPOException;

import java.util.HashMap;
import java.util.Map;

/**
 * Super class for all commands, i.e. the classes implementing one HpoWorkbench execution step.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public abstract class HPOCommand {

    public abstract String getName();

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
     *
     * @throws HPOException on problems executing the command.
     */
    public abstract void run();

    public abstract void setOptions(Map<String,String> mp) throws ArgumentParserException;


    public HPOCommand setDefaultValue(String key,String value) {
        defaults.put(key,value);
        return this;
    }

    public String toString() {
        return getName();
    }

}