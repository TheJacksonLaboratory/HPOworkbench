package org.monarch.hpoapi.ontology;

/**
 * An exception which may be thrown by the OBOParser class.
 *
 * @see OBOParser
 * @author Sebastian Bauer
 *
 */
public class OBOParserException extends Exception
{
    /** Serial UID */
    private static final long serialVersionUID = 1L;

    protected int linenum;
    protected String line;
    protected String filename;

    public OBOParserException(String message, String line, int linenum)
    {
        super(message);
        this.line = line;
        this.linenum = linenum;
        this.filename = "<tempfile>";
    }

    public OBOParserException(String message)
    {
        super(message);
        this.line = "";
        this.linenum = -1;
        this.filename = "<tempfile>";
    }

    public String getLine()
    {
        return line;
    }

    public int getLineNum()
    {
        return linenum;
    }

    public String toString()
    {
        String loc;

        if (linenum >= 0) loc = filename + ":" + linenum;
        else loc = filename;

        if (line != null)
            return loc + " obo parser error: " + getMessage() + " in \""+ line + "\".";
        return loc + " obo parser error: " + getMessage() + ".";
    }
}