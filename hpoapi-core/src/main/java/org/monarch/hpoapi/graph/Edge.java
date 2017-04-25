package org.monarch.hpoapi.graph;

/*
 * Created on 21.08.2005
 *
 * @author Sebastian Bauer
 */


public class Edge<Type>
{
    private Type source;
    private Type dest;

    public Edge(Type source, Type dest)
    {
        this.source = source;
        this.dest = dest;
    }

    /**
     * @return the edge's destination.
     */
    public final Type getDest()
    {
        return dest;
    }

    /**
     * @return the edge's source.
     */
    public final Type getSource()
    {
        return source;
    }

    /**
     * Returns the weight of an edge. The default implementation
     * returns always 1 and hence must be overwritten by subclasses
     * in order to return different weights.
     *
     * @return the weight
     */
    public int getWeight()
    {
        return 1;
    }

    void setSource(Type source) {
        this.source = source;
    }

    void setDest(Type dest) {
        this.dest = dest;
    }
}