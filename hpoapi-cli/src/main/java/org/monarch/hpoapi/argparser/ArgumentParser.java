package org.monarch.hpoapi.argparser;

import net.sourceforge.argparse4j.annotation.Arg;

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


    private List<Argument> arglist;

    private Map<String,Argument> argmap;

    public ArgumentParser(String program) {
        this.programName=program;
        arglist = new ArrayList<>();
        argmap = new HashMap<>();
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
                } else {
                    am.addArgWithoutValue(arg);
                }
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


    public void handleError(ArgumentParserException e) {
        e.printStackTrace();
        System.err.println("TODO -- more");
    }



}
