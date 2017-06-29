package org.monarch.hpoapi.io;

import ontologizer.io.obo.OBOParser;
import ontologizer.io.obo.OBOParserException;
import ontologizer.io.obo.OBOParserFileInput;
import ontologizer.ontology.*;
import ontologizer.types.ByteString;
import org.monarch.hpoapi.exception.HPOException;

import java.io.IOException;

/**
 * Created by peter on 6/28/2017.
 */
public class HPO_OBOParser {

    private String obopath=null;
    /** A bitfield that controls the parser options. */
    private int parserOptions=0;

    public HPO_OBOParser(String path) {
        this.obopath=path;
        setDefaultOBOParserOptions();
    }

    private void setDefaultOBOParserOptions() {
        this.parserOptions = OBOParser.PARSE_DEFINITIONS | OBOParser.PARSE_XREFS;
    }

    /** Set the behavior of the parser with respect to parsing cross referneces (xrefs).
     *
     * @param x if true - parse xrefs; if false-do not parse xrefs., leave null.
     */
    public void parseXRefs(boolean x){
        if (x) {
            this.parserOptions=this.parserOptions | OBOParser.PARSE_XREFS;
        } else {
            this.parserOptions = this.parserOptions & ~OBOParser.PARSE_XREFS;
        }
    }


    /** Uses Ontologizerlib to parse the hp.obo file.
     * @return An {@link Ontology} object representing the HP.
     * @throws HPOException
     */
    public Ontology parserOntologyFile() throws HPOException {
        Ontology ontology=null;
        if (obopath==null)
            throw new HPOException("[ERROR] attempt to create Ontology with filepath not set (null)");
     try {

        OBOParser parser = new OBOParser(new OBOParserFileInput(obopath),OBOParser.PARSE_DEFINITIONS|this.parserOptions);

        String parseResult = parser.doParse();

        System.err.println("Information about parse result:");
        System.err.println(parseResult);
        TermContainer termContainer =
                new TermContainer(parser.getTermMap(), parser.getFormatVersion(), parser.getDate());
        ontology = Ontology.create(termContainer);
    } catch (IOException e) {
        String err = String.format("[ERROR]: Problem reading input file (%s). \n %s",this.obopath,e.toString());
        throw new HPOException(err);
    } catch (OBOParserException e) {
         String err = String.format("[ERROR]:  Problem parsing OBO file (%s) \n%s",this.obopath,e.toString());
        throw new HPOException(err);
    }
    return ontology;
    }



}
