package org.monarch.hpoapi.cmd;


import org.monarch.hpoapi.exception.HPOException;

/**
 * Thrown on problems with data source configuration files.
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 */
public class InvalidDataSourceException extends HPOException {

    private static final long serialVersionUID = 1L;

    public InvalidDataSourceException() {
        super();
    }

    public InvalidDataSourceException(String msg) {
        super(msg);
    }

    public InvalidDataSourceException(String msg, Throwable cause) {
        super(msg);
    }

}