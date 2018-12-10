package org.monarchinitiative.hpoworkbench.exception;

public class BadResponseException extends HPOWorkbenchException {
    private static long SerialVersionUID=1L;
    public BadResponseException(){ super();}
    public BadResponseException(String msg) { super(msg);}
}
