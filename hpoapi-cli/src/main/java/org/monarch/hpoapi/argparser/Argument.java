package org.monarch.hpoapi.argparser;

/**
 * Created by peter on 24.06.17.
 */
public class Argument {

    private String longflag=null;
    private String shortflag=null;
    private String help=null;
    private String value=null;
    private ArgumentAction action=null;
    private boolean required=false;

    public Argument(String arg) {
        setLongFlag(arg);
    }

    public Argument help(String h) {
        this.help=h;
        return this;
    }

    public boolean hasAction() { System.out.println("Checking hasAction");return action !=null; }
    /** This flag requires an argument (default:false).*/
    public Argument required() { this.required=true; return this;}

    public boolean needsArgument() { return this.required; }

    public void setValue(String v) {this.value=v;}

    public String getValue() { return  value; }

    public void run() {
        action.run(this);
    }

    public Argument action(ArgumentAction action) {
        this.action=action;
        System.out.println("Adding action"+action);
        return this;
    }

    public Argument setShortFlag(String arg) {
        if (! arg.startsWith("-") && arg.length()==2) {
            System.err.println("[WARNING] malformed long argument: "+arg+"\nShort flags must start with \"-\"");
            System.exit(1);
        } else {
            this.shortflag=arg.substring(1);
        }
        return this;
    }



    /** set a flag like --version. Requirement. Two dashes and rest of string has to have at least two letters. */
    private void setLongFlag(String arg)  {
        if (! arg.startsWith("--") && arg.length()>3) {
            System.err.println("[WARNING] malformed long argument: "+arg +"\nLong flags must start with \"--\"");
            System.exit(1);
        } else {
            this.longflag=arg.substring(2);
        }
    }


    public String getName() {
        return longflag;
    }

    @Override
    public String toString() {
        return String.format("%s [long:%s;short:%s;help:%s]",getName(),longflag,shortflag,help);
    }
}
