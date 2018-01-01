package org.monarchinitiative.hpoworkbench.rtf;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.graph.data.Edge;
import com.github.phenomics.ontolib.ontology.data.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.io.HPOParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This class coordinates the production and output of an RTF file that contains a table with all of the terms
 * that emanate from a certain term in the HPO, for instance, abnormal immune physiology. It is intended to produce
 * an RTF file that can be easily distributed as a Word file to collaborators who will enter corrections and additions
 * to a set of HPO terms in a specific area of medicine.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.0
 */
public class Hpo2Rtf {
    private static final Logger LOGGER = LogManager.getLogger();
    /** File handle to write to. */
    BufferedWriter writer=null;
    /** Number of unique terms we have output in this file. */
    private int n_terms_output=0;
    /** HPO Ontology object. */
    private HpoOntology hpoOntology=null;
    private static String DEFAULT_START_TERM="HP:0000118";

    private static final TermPrefix HPPREFIX = new ImmutableTermPrefix("HP");



    private String startTerm="HP:0002715"; // immunology




    public Hpo2Rtf(String filename) throws  IOException {
        writer=new BufferedWriter(new FileWriter(filename));
        inputHPOdata(null);
    }


    public void writeRtfHeader() throws IOException {
        String s = String.format("{\\rtf1\\ansi\\deff0 {\\fonttbl {\\f0 Courier;}}\n" +
                "{\\colortbl;\\red0\\green0\\blue0;\\red255\\green0\\blue0;}\n" +
                "\\landscape\n" +
                "\\paperw15840\\paperh12240\\margl720\\margr720\\margt720\\margb720\n" +
                "\\tx720\\tx1440\\tx2880\\tx5760\n" +
                "This table shows all of descendant terms of %s\\line \\line \n \\par\\pard\\tx1440\\tx2880",startTerm);
        writer.write(s);
    }


    public void writeTableFromTerm(String id) throws IOException {
        startTerm=id;
        String tab = createTable().table();
        writer.write(tab);
    }

    public void close() throws IOException {
        //writer.write("done");
        writer.close();
    }


    /**
     * Get all the direct children terms of a term
     * @param tid HPO term for which we want to get the children
     * @return set of children term ids of tid.
     */
    private Set<TermId> getChildren(TermId tid) {
        Set<TermId> st = new HashSet<>() ;
        Iterator it = hpoOntology.getGraph().inEdgeIterator(tid);
        while (it.hasNext()) {
            Edge<TermId> egde = (Edge<TermId>) it.next();
            TermId source = egde.getSource();
            st.add(source);
        }
        return st;
    }

    /**
     * Create a set of rows that will be displayed as an RTF table. Noting that the HPO has multiple parentage,
     * only show any one subhierarchy once.
     * @return
     */
    private RtfTable createTable() {
        Map<TermId,HpoTerm> termmap = hpoOntology.getTermMap();
        Set<TermId> previouslyseen=new HashSet<>();
        String id = startTerm;
        Stack<Pair<TermId,Integer>> stack = new Stack<>();
        if (id==null) {
            LOGGER.error("Attempt to create pretty format HPO Term with null id");
            return null;
        }
        if (id.startsWith("HP:")) {
            id = id.substring(3);
        }
        TermId tid = new ImmutableTermId(HPPREFIX,id);
        stack.push(new Pair<>(tid,1));
        ArrayList<HpoRtfTableRow> rtfrows = new ArrayList<>();

        while (! stack.empty() ) {
            Pair<TermId,Integer> pair = stack.pop();
            TermId termId=pair.first;
            Integer level=pair.second;
            HpoTerm hterm = termmap.get(termId);
            if (previouslyseen.contains(termId)) {
                // we have already output this term!
                HpoRtfTableRow hrow = new HpoRtfTableRow(level, hterm,
                        "\\b Term previously shown (dependent on another parent)\\b0");
                rtfrows.add(hrow);
                continue;
            } else {
                previouslyseen.add(termId);
            }
            Set<TermId> children = getChildren(termId);
            for (TermId t:children) {
                stack.push(new Pair<>(t,level+1));
            }
            HpoRtfTableRow hrow = new HpoRtfTableRow(level, hterm);
            rtfrows.add(hrow);
        }

        RtfTable table = new RtfTable(rtfrows);
        return table;

    }


    /** Input the HPO ontologuy file into {@link #hpoOntology}. */
    private void inputHPOdata(String hpo) {
        if (hpo==null)hpo="data/hp.obo";
        LOGGER.trace(String.format("inputting HPO ontology from file %s.",hpo));
        HPOParser parser = new HPOParser(hpo);
        hpoOntology=parser.getHPO();

    }



}
