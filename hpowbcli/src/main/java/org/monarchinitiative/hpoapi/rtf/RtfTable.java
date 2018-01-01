package org.monarchinitiative.hpoapi.rtf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A convenience class for creating an RTF-formatted table.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.0
 */
public class RtfTable {
    private static final Logger LOGGER = LogManager.getLogger();
    /** A list of rows of the RTF table. */


    List<HpoRtfTableRow> rows;

    public RtfTable(List<HpoRtfTableRow> rowlist) {
        rows=rowlist;
    }



    private String header() {
        return " {\\rtf1\\ansi\\deff0";
    }


    private String footer() {
        return "}\n";
    }

    public String table() {
        String middle = rows.stream().map(r ->r.row()).collect(Collectors.joining("\n"));
        String t = String.format("%s%s%s",HpoRtfTableRow.header(),middle,footer());
        return t;
    }

}
