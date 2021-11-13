package org.monarchinitiative.hpoworkbench.io;

import org.monarchinitiative.hpoworkbench.exception.HPOException;

/**
 * Exception that can be called if something went wrong while downloading the transcript files.
 *
 * @author <a href="mailto:marten.jaeger@charite.de">Marten Jaeger</a>
 */
public class FileDownloadException extends HPOException {
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
