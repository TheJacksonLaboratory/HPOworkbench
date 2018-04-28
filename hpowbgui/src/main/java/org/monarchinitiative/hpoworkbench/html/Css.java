package org.monarchinitiative.hpoworkbench.html;

public class Css {

    private static final String CSS = "body {\n" +
            "  font: normal medium/1.4 sans-serif;\n" +
            "}\n" +
            "table {\n" +
            "  border-collapse: collapse;\n" +
            "  width: 100%;\n" +
            "}\n" +
            "th, td {\n" +
            "  padding: 0.25rem;\n" +
            "  text-align: left;\n" +
            "  border: 1px solid #ccc;\n" +
            "}\n" +
            "tr.myheader {background:#fff} "+
            "tr.shared { background:#3ff} " +
            "tr.subclazz { background:#f3f} " +
            "tr.unrelated { background:#ff3} " +
            // "tbody tr:nth-child(odd) {\n" +
            //"  background: #eee;\n" +
            "}";
    public static String getCSS() {
        return CSS;
    }
}
