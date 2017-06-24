package org.monarch.hpoapi.cmd;

import ontologizer.io.obo.OBOParser;
import ontologizer.io.obo.OBOParserException;
import ontologizer.io.obo.OBOParserFileInput;
import ontologizer.ontology.Ontology;
import ontologizer.ontology.Term;
import ontologizer.ontology.TermContainer;
import ontologizer.ontology.TermMap;
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
        String path=null;
        if (mp.containsKey("input")) {
            path=mp.get("input");
        } else if (this.defaults.containsKey("input")) {
            path=defaults.get("input");
        } else {
            throw new ArgumentParserException("--input option must be provided to run csv");
        }
        //todo -- output file path
    }



    /**
     * Perform the downloading.
     */
    @Override
    public void run()  {
        Ontology ontology=null;
        String obopath="io/hp.obo"; // TODO - make flexible
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
            BufferedWriter bw = new BufferedWriter(new FileWriter("hp.csv"));
            while (it.hasNext()) {
                Term t = it.next();
                //System.out.println(t);
                String label = t.getName().toString();
                String id = t.getIDAsString();
                bw.write(String.format("%s\t%s\n",label,id));
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


}
