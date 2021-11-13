package org.monarchinitiative.hpoworkbench.exception;

public class HPOWorkbenchException extends Exception{
    private static final long serialVersionUID=1;
    public HPOWorkbenchException(){super();}
    public HPOWorkbenchException(String msg) { super(msg);}
    public HPOWorkbenchException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
