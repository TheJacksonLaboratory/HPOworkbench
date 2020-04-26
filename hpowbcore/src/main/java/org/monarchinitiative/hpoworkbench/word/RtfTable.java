package org.monarchinitiative.hpoworkbench.word;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A convenience class for creating an RTF-formatted table.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.0
 */
class RtfTable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RtfTable.class);
    /** A list of rows of the RTF table. */


    private final List<HpoRtfTableRow> rows;

    RtfTable(List<HpoRtfTableRow> rowlist) {
        rows=rowlist;
    }



    private String header() {
        return " {\\rtf1\\ansi\\deff0";
    }


    private String footer() {
        return "}\n";
    }

    public String table() {
        String middle = rows.stream().map(HpoRtfTableRow::row).collect(Collectors.joining("\n"));
        return String.format("%s%s%s",HpoRtfTableRow.header(),middle,footer());
    }

}
