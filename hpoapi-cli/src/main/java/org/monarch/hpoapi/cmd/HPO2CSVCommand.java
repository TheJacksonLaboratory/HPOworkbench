package org.monarch.hpoapi.cmd;

import ontologizer.io.obo.OBOParser;
import ontologizer.io.obo.OBOParserException;
import ontologizer.io.obo.OBOParserFileInput;
import ontologizer.ontology.*;
import ontologizer.types.ByteString;
import org.monarch.hpoapi.data.DataSource;
import org.monarch.hpoapi.data.DataSourceFactory;
import org.monarch.hpoapi.data.DatasourceOptions;
import org.monarch.hpoapi.data.PhenotypeData;
import org.monarch.hpoapi.exception.HPOException;
import org.monarch.hpoapi.util.PathUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.monarch.hpoapi.argparser.ArgumentParserException;

/**
 * Make a CSV file representing the HPO hp.obo file
 * Created by robinp on 6/23/17.
 */
public class HPO2CSVCommand extends HPOCommand {
    /** name of this command */
    private final static String name = "csv";
    private Map<String,String> hpoName2IDmap=null;

    public String getName() { return name; }

    private String pathToHpObo=null;


    /**
     *
     */
    public HPO2CSVCommand()  {
        //this.options = new PhenotypeDownloadOptions();
       // this.options.setFromArgs(args);
    }

    /** This function passes the options to the command
     * and makes sure we have everything we need. It checks
     * the default values if it doesnt find the values here.
     * If it is missing something, it throws and exception.
     * @param mp
     */
    public void setOptions(Map<String,String> mp) throws ArgumentParserException {
        System.out.println("setOptions CSV");
        for (String s:mp.keySet()) {
            System.out.println(s+":"+mp.get(s));
        }
        if (mp.containsKey("input")) {
            pathToHpObo=mp.get("input");
        } else if (this.defaults.containsKey("input")) {
            pathToHpObo=defaults.get("input");
        } else {
            throw new ArgumentParserException("--input option must be provided to run csv");
        }
        //todo -- output file path
    }


    /**
     * id: HP:3000067
     name: Abnormality of lateral crico-arytenoid
     def: "An abnormality of a lateral crico-arytenoid." [GOC:TermGenie]
     synonym: "Abnormality of lateral cricoarytenoid muscle" EXACT []
     xref: UMLS:C4073274
     is_a: HP:0000464 ! Abnormality of the neck
     is_a: HP:0003011 ! Abnormality of the musculature
     is_a: HP:0025423 ! Abnormal larynx morphology
     TODO 	def not working,
     */
    public static final String header="#id\tname\tsynonyms\tis_a";


    private String join(ByteString[] syn) {
        if (syn==null || syn.length==0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(syn[0]);
        for (int i=1;i<syn.length;i++) {
            sb.append(";"+syn[i]);
        }
        return sb.toString();
    }

    private String join( TermXref[] xrefs) {
        if (xrefs==null || xrefs.length==0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(xrefs[0]);
        for (int i=1;i<xrefs.length;i++) {
            sb.append(";"+xrefs[i]);
        }
        return sb.toString();
    }

    private String join(ParentTermID [] parents) {
        if (parents==null || parents.length==0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(parents[0].termid.toByteString());
        for (int i=1;i<parents.length;i++) {
            ParentTermID ptid = parents[i];
            sb.append(";"+ptid.termid.toByteString());
        }
        return sb.toString();
    }

    /**
     * Perform the downloading.
     */
    @Override
    public void run()  {
        Ontology ontology=null;
        String obopath=pathToHpObo;
        try {
            OBOParser parser = new OBOParser(new OBOParserFileInput(obopath));

            String parseResult = parser.doParse();

            System.err.println("Information about parse result:");
            System.err.println(parseResult);
            TermContainer termContainer =
                    new TermContainer(parser.getTermMap(), parser.getFormatVersion(), parser.getDate());
            ontology = Ontology.create(termContainer);
        } catch (IOException e) {
            System.err.println(
                    "ERROR: Problem reading input file. See below for technical information\n\n");
            e.printStackTrace();
            System.exit(1);
        } catch (OBOParserException e) {
            System.err.println(
                    "ERROR: Problem parsing OBO file. See below for technical information\n\n");
            e.printStackTrace();
            System.exit(1);
        }
        TermMap tmap = ontology.getTermMap();
        Iterator<Term> it = tmap.iterator();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("hp.tsv"));
            bw.write(header+"\n");
            while (it.hasNext()) {
                Term t = it.next();
                //System.out.println(t);
                String label = t.getName().toString();
                String id = t.getIDAsString();
                ByteString def=t.getDefinition();

                String defString=def!=null?def.toString():"";
               // System.out.println("DEF="+defString+"for t.toString()="+t.toString());
                ByteString[] syn = t.getSynonyms();
                String synString=join(syn);
                TermXref[] xrefs=t.getXrefs();
                String xrefString=join(xrefs);
                ParentTermID [] parents = t.getParents();
                String parentsString=join(parents);
                bw.write(String.format("%s\t%s\t%s\t%s\n",id,label,synString,parentsString));
                //System.out.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\n",label,id,defString,synString,xrefString,parentsString));
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


}
