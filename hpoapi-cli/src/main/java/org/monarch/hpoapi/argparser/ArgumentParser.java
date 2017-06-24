package org.monarch.hpoapi.argparser;

import net.sourceforge.argparse4j.annotation.Arg;
import org.monarch.hpoapi.cmd.HPOCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 24.06.17.
 */
public class ArgumentParser {

    private String programName=null;
    private String version=null;
    private Map<String,HPOCommand> commandmap=null;
    private HPOCommand runnableCommand=null;


    private List<Argument> arglist;

    private Map<String,Argument> argmap;

    public ArgumentParser(String program) {
        this.programName=program;
        arglist = new ArrayList<>();
        argmap = new HashMap<>();
        commandmap=new HashMap<>();
    }


    public void setVersion(String version) {
        this.version=version;
    }

    public Argument addArgument(String s) {
        Argument a = new Argument(s);
        arglist.add(a);
        return a;
    }

    private Argument getArg(String name) {
        return argmap.get(name);
    }


    public ArgumentMap parseArgs(String args[]) throws ArgumentParserException {
        ArgumentMap am =new ArgumentMap();
        for (int i=0;i<args.length;i++) {
            String s = args[i];
            if (s.startsWith("--")) {
                Argument arg = getArg(s.substring(2));
                if (arg==null) throw new ArgumentParserException("Did not recognize argument: "+s);
                if (arg.needsArgument() ) {
                    if (i==(args.length-1)) {
                        throw new ArgumentParserException(String.format("--%s requires argument: ", arg.getName()));
                    } else if (args[i+1].startsWith("-")) {
                        throw new ArgumentParserException(String.format("--%s requires argument: ", arg.getName()));
                    }
                    String value = args[i+1];
                    am.addArgWithValue(arg,value);
                    i++;
                } else {
                    am.addArgWithoutValue(arg);
                }
            } else {
                // must be a command if it does not belong to a flagged argument and does not start with "-" or "--"
                setRunnableCommand(s);
            }
        }

        if (true)

        for (Argument arg : arglist) {
            if (arg.hasAction()) {
                arg.run();
            }
        }

        return am;
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
}
