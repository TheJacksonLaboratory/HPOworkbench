package org.monarchinitiative.hpoapi.exception;

/**
 * Created by peter on 08.05.17.
 * Base class for HPOAPI exceptions
 */
public class HPOException extends Exception{
    public static final long serialVersionUID = 2L;

    public HPOException() {
        super();
    }

    public HPOException(String msg) {
        super(msg);
    }

    public HPOException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

