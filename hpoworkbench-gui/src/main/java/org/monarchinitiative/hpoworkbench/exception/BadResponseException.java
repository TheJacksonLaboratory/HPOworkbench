package org.monarchinitiative.hpoworkbench.exception;

public class BadResponseException extends HPOWorkbenchException {
    private static final long serialVersionUID=1L;
    public BadResponseException(){ super();}
    public BadResponseException(String msg) { super(msg);}
}
