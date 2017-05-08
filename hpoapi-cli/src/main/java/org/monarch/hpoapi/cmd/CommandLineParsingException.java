package org.monarch.hpoapi.cmd;


/**
 * Exception thrown on problems with the command line.
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 */
public class CommandLineParsingException extends Exception {

    public CommandLineParsingException() {
        super();
    }

    public CommandLineParsingException(String msg) {
        super(msg);
    }

    public CommandLineParsingException(String msg, Throwable cause) {
        super(msg, cause);
    }

    private static final long serialVersionUID = 1L;

}