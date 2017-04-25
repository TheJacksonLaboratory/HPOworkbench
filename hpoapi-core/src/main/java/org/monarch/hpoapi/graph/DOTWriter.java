package org.monarch.hpoapi.graph;


import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.monarch.hpoapi.graph.AbstractGraph.DotAttributesProvider;

public class DOTWriter
{
    /**
     * Writes out a graph as a dot file.
     *
     * @param g the graph that should be written out
     * @param os for the output.
     * @param nodeSet defines the subset of nodes to be written out.
     * @param provider specifies the attributes provider.
     * @param nodeSep defines the space between nodes of the same rank.
     * @param rankSep defines the space between two nodes of subsequent ranks.
     */
    public static <V> void write(AbstractGraph<V> g, OutputStream os, Iterable<V> nodeSet, final DotAttributesProvider<V> provider, final double nodeSep, final double rankSep)
    {
        DotAttributesProvider<V> newProvider = new DotAttributesProvider<V>()
        {
            @Override
            public String getDotGraphAttributes()
            {
                StringBuilder attrs = new StringBuilder();
                attrs.append(String.format(Locale.US, "nodesep=%f; ranksep=%f;", nodeSep, rankSep));

                if (provider.getDotGraphAttributes() != null)
                    attrs.append(provider.getDotGraphAttributes());
                return attrs.toString();
            }

            @Override
            public String getDotNodeAttributes(V vt)
            {
                return provider.getDotNodeAttributes(vt);
            }

            @Override
            public String getDotEdgeAttributes(V src, V dest)
            {
                return provider.getDotEdgeAttributes(src, dest);
            }

            @Override
            public String getDotHeader()
            {
                return provider.getDotHeader();
            }
        };
        DOTWriter.write(g,os,nodeSet,newProvider);
    }

    /**
     * Writes out a graph as a dot file.
     *
     * @param g the graph that should be written out
     * @param os for the output.
     * @param nodeSet defines the subset of nodes to be written out.
     * @param provider specifies the attributes provider.
     */
    public static <V> void write(AbstractGraph<V> g, OutputStream os, Iterable<V> nodeSet, DotAttributesProvider<V> provider)
    {
        PrintWriter out = new PrintWriter(os);
        String graphHeader = provider.getDotHeader();
        String graphAttributes = provider.getDotGraphAttributes();

        if (graphHeader != null)
        {
            out.append(graphHeader);
            out.append("\n");
        }

        out.append("digraph G {");
        if (graphAttributes != null)
        {
            out.append(graphAttributes);
            out.append('\n');
        }

		/* Write out all nodes, call the given interface. Along the way, remember the indices. */
        HashMap<V,Integer> v2idx = new HashMap<V,Integer>();
        int i = 0;
        for (V v : nodeSet)
        {
            String attributes = provider.getDotNodeAttributes(v);
            String name = provider.getDotNodeName(v);
            if (name == null) name = Integer.toString(i);

            out.write(name);
            if (attributes != null)
                out.write("[" + attributes + "]");
            out.write(";\n");

            v2idx.put(v,i++);
        }

		/* Now write out the edges. Write out only the edges which are linking nodes within the node set. */
        for (V s : nodeSet)
        {
            Iterator<V> ancest = g.getChildNodes(s);
            while (ancest.hasNext())
            {
                V d = ancest.next();

                if (v2idx.containsKey(d))
                {
                    String sName = provider.getDotNodeName(s);
                    String dName = provider.getDotNodeName(d);
                    if (sName == null) sName = Integer.toString(v2idx.get(s));
                    if (dName == null) dName = Integer.toString(v2idx.get(d));

                    out.write(sName + " -> " + dName);

                    String attributes = provider.getDotEdgeAttributes(s,d);
                    if (attributes != null)
                        out.write("[" + attributes + "]");

                    out.println(";\n");
                }
            }
        }

        out.write("}\n");
        out.flush();
        out.close();
    }


}