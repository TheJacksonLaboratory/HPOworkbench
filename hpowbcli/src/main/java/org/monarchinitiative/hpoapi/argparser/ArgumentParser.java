package org.monarchinitiative.hpoapi.argparser;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoapi.cmd.HPOCommand;

import java.util.*;

/**
 * Created by peter on 24.06.17.
 */
public class ArgumentParser {
    private static final Logger LOGGER = LogManager.getLogger();

    private String programName=null;
    private String version=null;
    /** This map will take all of the potential commands--the user needs to choose one of them. */
    private Map<String,HPOCommand> commandmap=null;
    /** This is the command chosen by the user that will be run with the passed options. */
    private HPOCommand runnableCommand=null;
    /** Key: name of argument (identical to longflag), value the {@link Argument} object.*/
    private Map<String,Argument> argmap;
    /** While the {@link #argmap} has all of the possible arguments,this has the arguments actually entered
     * by the user. The key is the name of the argument, and the value is the value. For instance,
     * if the user enters --input myfile.txt then the key is "input" and the value is "myfile.txt".
     */
    private Map<String,Argument> userArguments;

    private Map<String,Argument>  argwithoutvalueset;

    public ArgumentParser(String program) {
        this.programName=program;
        argmap = new HashMap<>();
        commandmap=new HashMap<>();
        this.userArguments=new HashMap<>();
    }


    public void setVersion(String version) {
        this.version=version;
    }

    /** This should be called with a String such as "--argumentname".
     * If the string does not start with two dashes, die with a warning message.
     * @param s
     * @return
     */
    public Argument addArgument(String s) {
        if (! s.startsWith("--")) {
            LOGGER.error("long argument string must start with \"--\"");
            System.exit(1);
        }
        Argument a = new Argument(s);
        argmap.put(a.getName(),a);
        return a;
    }

    private Argument getArg(String name) {
        return argmap.get(name);
    }


    public void  parseArgs(String args[]) throws ArgumentParserException {
        String argumentString;
        for (int i=0;i<args.length;i++) {
            String s = args[i];
            if (s.startsWith("--")) {
                String longArgString=s.substring(2);
                Argument arg = getArg(longArgString);
                if (arg==null) throw new ArgumentParserException("Did not recognize argument: "+longArgString);
                if (arg.needsArgument() ) {
                    if (i == (args.length - 1)) {
                        throw new ArgumentParserException(String.format("--%s requires argument: ", arg.getName()));
                    } else if (args[i + 1].startsWith("-")) {
                        throw new ArgumentParserException(String.format("--%s requires argument: ", arg.getName()));
                    }
                    i++;
                    String value = args[i];
                    arg.setValue(value);
                }
                this.userArguments.put(arg.getName(),arg);
            } else {
                // must be a command if it does not belong to a flagged argument and does not start with "-" or "--"
                setRunnableCommand(s);
            }
        }
        /** This will run arguments with actions such as Version */
        for (Argument arg : userArguments.values()) {
            if (arg.hasAction()) {
               arg.run();
            }
        }
    }

    private Map<String,String>  getOptionMap() {
        Map<String,String> options = new HashMap<>();
        for (Argument arg : argmap.values()) {
            options.put(arg.getName(),arg.getValue());
        }
        return  options;
    }




    /** This initializes the command object to the values in the parser and returns a fully runable and finished object. */
    public HPOCommand getCommand() throws ArgumentParserException {
        if (this.runnableCommand==null) {
            throw new ArgumentParserException("[ERROR] no command passed.");
        }
        Map<String,String> options = getOptionMap();
        this.runnableCommand.setOptions(options);
        return this.runnableCommand;
    }



    public void handleError(ArgumentParserException e) {
        e.printStackTrace();
        System.err.println("TODO -- more");
    }


    public HPOCommand addCommand(HPOCommand cmd) {
        commandmap.put(cmd.getName(),cmd);
        return cmd;
    }

    /** Sets the command that will be used to run the program. Rules:
     * only one command is allowed at a time.
     * @param arg String (name of command) from command line
     * @throws ArgumentParserException
     */
    private void setRunnableCommand(String arg) throws ArgumentParserException {
        HPOCommand cmd = this.commandmap.get(arg);
        if (cmd==null) {
            throw new ArgumentParserException("[ERROR] Did not recognize command: "+arg);
        }
        if (this.runnableCommand != null) {
            throw new ArgumentParserException(String.format("[ERROR] not allowed to run more than one command: (%s and %s)",arg,this.runnableCommand));
        }
        this.runnableCommand=cmd;
    }


    public void debugPrint() {
        System.out.println("ArgumentParser:"+
        "\n\tArguments:"+argmap.size());
        for (Argument a: argmap.values()) {
            System.out.println("\t\t"+a);
        }
        System.out.println("\tCommands:");
        for (HPOCommand hpoc: commandmap.values()) {
            System.out.println("\t\t"+hpoc);
        }
        System.out.println("\tVersion: " + version);
    }


}
