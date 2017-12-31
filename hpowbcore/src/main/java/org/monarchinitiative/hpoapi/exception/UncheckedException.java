package org.monarchinitiative.hpoapi.exception;



/**
 * Base class for unchecked exceptions in Jannovar
 *
 * @author <a href="mailto:Peter.Robinson@jax.org">Peter N Robinson</a>
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 */
public class UncheckedException extends RuntimeException {

    public static final long serialVersionUID = 1L;

    public UncheckedException() {
        super();
    }

    public UncheckedException(String msg) {
        super(msg);
    }

    public UncheckedException(String msg, Throwable cause) {
        super(msg, cause);
    }

}