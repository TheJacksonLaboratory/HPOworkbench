package org.monarchinitiative.hpoworkbench.html;

class Css {

    // "tbody tr:nth-child(odd) {\n" +
    //"  background: #eee;\n" +
    private static final String CSS = """
            body {
              font: normal medium/1.4 sans-serif;
            }
            table {
              border-collapse: collapse;
              width: 100%;
            }
            th, td {
              padding: 0.25rem;
              text-align: left;
              border: 1px solid #ccc;
            }
            tr.myheader {background:#fff} tr.shared { background:#3ff} tr.subclazz { background:#f3f}
            tr.unrelated { background:#ff3} }""";
    public static String getCSS() {
        return CSS;
    }
}
