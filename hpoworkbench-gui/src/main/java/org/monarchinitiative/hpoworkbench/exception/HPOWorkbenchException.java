package org.monarchinitiative.hpoworkbench.exception;

public class HPOWorkbenchException extends Exception{
    public HPOWorkbenchException(){super();}
    public HPOWorkbenchException(String msg) { super(msg);}
    public HPOWorkbenchException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
