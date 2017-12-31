package org.monarchinitiative.hpoapi.io;

import org.monarchinitiative.hpoapi.exception.HPOException;

/**
 * Exception that can be called if something went wrong while downloading the transcript files.
 *
 * @author <a href="mailto:marten.jaeger@charite.de">Marten Jaeger</a>
 */
public class FileDownloadException extends HPOException {
    private static final long serialVersionUID = 1L;

    public FileDownloadException() {
        super();
    }

    public FileDownloadException(String msg) {
        super(msg);
    }

    public FileDownloadException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
