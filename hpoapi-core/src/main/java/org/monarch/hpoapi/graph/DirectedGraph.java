package org.monarch.hpoapi.graph;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map.Entry;

final class VertexAttributes<VertexType>
{
    /** All edges where the vertex is appearing as dest */
    public ArrayList<Edge<VertexType>> inEdges = new ArrayList<Edge<VertexType>>();

    /** All edges where the vertex is appearing as source */
    public ArrayList<Edge<VertexType>> outEdges = new ArrayList<Edge<VertexType>>();
};

/**
 * This class represents holds the structure of a directed graph.
 *
 * @author Sebastian Bauer
 * @author sebastiankohler
 *
 */
public class DirectedGraph<VertexType> extends AbstractGraph<VertexType> implements Iterable<VertexType>
{
    /** Contains the vertices associated to meta information (edges) */
    private LinkedHashMap<VertexType,VertexAttributes<VertexType>> vertices;

    public interface IDistanceVisitor<VertexType>
    {
        /**
         * @param vertex
         * @param path
         * @param distance
         * @return false if visit should be never called again.
         */
        boolean visit(VertexType vertex, List<VertexType> path, int distance);
    }

    /**
     * Constructs the directed graph.
     */
    public DirectedGraph()
    {
        vertices = new LinkedHashMap<VertexType,VertexAttributes<VertexType>>();
    }

    /**
     * Adds the given vertex to the graph. Nothing happens if the graph
     * already contains the vertex.
     *
     * @param vertex
     */
    public void addVertex(VertexType vertex)
    {
        if (!vertices.containsKey(vertex))
        {
            VertexAttributes<VertexType> va = new VertexAttributes<VertexType>();
            vertices.put(vertex,va);
        }
    }

    /**
     * Removed the given vertex and all edges associated to it.
     *
     * @param vertex
     */
    public void removeVertex(VertexType vertex)
    {
        VertexAttributes<VertexType> va = vertices.get(vertex);
        if (va != null)
        {
			/* Remove each in edge */
            while (va.inEdges.size() > 0)
            {
                int lastPos = va.inEdges.size() - 1;
                Edge<VertexType> last = va.inEdges.get(lastPos);
                removeConnections(last.getSource(), last.getDest());
            }

			/* Remove each out edge */
            while (va.outEdges.size() > 0)
            {
                int lastPos = va.outEdges.size() - 1;
                Edge<VertexType> last = va.outEdges.get(lastPos);
                removeConnections(last.getSource(), last.getDest());
            }

            vertices.remove(vertex);
        }
    }

    /**
     * Removed the given vertex and all edges associated to it.
     *
     * @param vertex
     */
    private void removeVertexMaintainConnectivity(VertexType vertex)
    {
        VertexAttributes<VertexType> va = vertices.get(vertex);
        if (va != null)
        {
			/* Connect each the source of each in edges to the dest of each out edge */
            for (Edge<VertexType> i : va.inEdges)
            {
                for (Edge<VertexType> o : va.outEdges)
                {
                    if (!hasEdge(i.getSource(),o.getDest()))
                        addEdge(new Edge<VertexType>(i.getSource(),o.getDest()));
                }
            }

            removeVertex(vertex);
        }
    }

    /**
     * @return the vertices as an iterable object.
     */
    public Iterable<VertexType> getVertices()
    {
        return vertices.keySet();
    }

    /**
     * Returns a copy of the graph.
     *
     * @return the duplicated graph.
     */
    public DirectedGraph<VertexType> copyGraph()
    {
        DirectedGraph<VertexType> copy = new DirectedGraph<VertexType>();
        Iterator<VertexType> nodeIt = this.getVertexIterator();
        while (nodeIt.hasNext())
        {
            copy.addVertex(nodeIt.next());
        }

        nodeIt = this.getVertexIterator();
        while (nodeIt.hasNext())
        {
            VertexType node = nodeIt.next();
            Iterator<VertexType> descIt = this.getChildNodes(node);
            while (descIt.hasNext())
            {
                copy.addEdge(new Edge<VertexType>(node,descIt.next()));
            }
        }
        return copy;
    }

    public int getNumberEdges()
    {
        int sum = 0;
        Iterator<VertexType> nodeIt = this.getVertexIterator();
        while (nodeIt.hasNext()){
            VertexType node = nodeIt.next();
            sum += vertices.get(node).outEdges.size();
        }
        return sum;
    }

    /**
     * Add a new edge into the graph.
     *
     * @param edge the edge which links two vertices.
     *
     * @throws IllegalArgumentException if the edge
     * 			is a link between two vertices which
     * 			haven't been added to the graph.
     */
    public void addEdge(Edge<VertexType> edge)
    {
        VertexAttributes<VertexType> vaSource = vertices.get(edge.getSource());
        VertexAttributes<VertexType> vaDest = vertices.get(edge.getDest());

		/* Ensure that the arguments are valid, i.e. both source
		 * and destination must be vertices within the graph  */
        if (vaSource == null || vaDest == null)
            throw new IllegalArgumentException("Error when trying to add edge between source: "+vaSource+" and destination: "+vaDest+".");

        vaSource.outEdges.add(edge);
        vaDest.inEdges.add(edge);
    }

    /**
     * Returns true if there is a directed edge between source and dest.
     *
     * @param source
     * @param dest
     * @return true if the edge exists, else false
     */
    public boolean hasEdge(VertexType source, VertexType dest)
    {
        VertexAttributes<VertexType> vaSource = vertices.get(source);
        for (Edge<VertexType> e : vaSource.outEdges)
        {
            if (e.getDest().equals(dest))
                return true;
        }
        return false;
    }

    public void removeConnections(VertexType source, VertexType dest)
    {
        VertexAttributes<VertexType> vaSource = vertices.get(source);
        VertexAttributes<VertexType> vaDest = vertices.get(dest);
//		System.out.println("remove all edges between "+source + " and "+ dest);
        if (vaSource == null || vaDest == null)
            throw new IllegalArgumentException();

//		System.out.println("start removing -->  ");
        HashSet<Edge<VertexType>> deleteMe = new HashSet<Edge<VertexType>>();
        for (Edge<VertexType> edge : vaSource.outEdges){
            if (edge.getDest().equals(dest)){
//				System.out.println(" --> added edge "+edge.getSource()+" -> "+edge.getDest());
                deleteMe.add(edge);
            }
        }
//		System.out.println("edges..."+deleteMe);
        if (deleteMe.size() > 1)
            throw new RuntimeException(" found more than one edge to delete ("+deleteMe.size()+") --> "+deleteMe);
        for (Edge<VertexType> edge : deleteMe){
//			System.out.print("rem: "+edge.getSource()+" -> "+edge.getDest());
            vaSource.outEdges.remove(edge);
        }
//		System.out.print(" ok 1 - ");
        deleteMe.clear();

        for (Edge<VertexType> edge : vaSource.inEdges){
            if (edge.getSource().equals(dest)){
                deleteMe.add(edge);
            }
        }
        if (deleteMe.size() > 1)
            throw new RuntimeException(" found more than one edge to delete ("+deleteMe.size()+") --> "+deleteMe);
        for (Edge<VertexType> edge : deleteMe){
            System.out.print("rem: "+edge.getSource()+" -> "+edge.getDest());
            vaSource.inEdges.remove(edge);
        }
//		System.out.print(" ok 2 - ");
        deleteMe.clear();
        for (Edge<VertexType> edge : vaDest.outEdges){
            if (edge.getDest().equals(source)){
                deleteMe.add(edge);
            }
        }
        if (deleteMe.size() > 1)
            throw new RuntimeException(" found more than one edge to delete ("+deleteMe.size()+") --> "+deleteMe);
        for (Edge<VertexType> edge : deleteMe){
//			System.out.print("rem: "+edge.getSource()+" -> "+edge.getDest());
            vaDest.outEdges.remove(edge);
        }
//		System.out.print(" ok 3 - ");
        deleteMe.clear();


        for (Edge<VertexType> edge : vaDest.inEdges){
            if (edge.getSource().equals(source)){
                deleteMe.add(edge);
            }
        }
        if (deleteMe.size() > 1)
            throw new RuntimeException(" found more than one edge to delete! ("+deleteMe.size()+") --> "+deleteMe);
        for (Edge<VertexType> edge : deleteMe){
//			System.out.print("rem: "+edge.getSource()+" -> "+edge.getDest());
            vaDest.inEdges.remove(edge);
        }
//		System.out.println(" ok 4 ");
    }

    /**
     * Returns the iterator which can be used for conveniently
     * iterating over all vertices contained within the graph.
     *
     * @return the iterator.
     */
    public Iterator<VertexType> getVertexIterator()
    {
        return vertices.keySet().iterator();
    }


    /**
     * Returns the edge connecting source to dest. As multi graphs are not
     * supported this is unique.
     *
     * @param source
     * @param dest
     * @return the edge or null if there is no edge between the specified nodes.
     */
    public Edge<VertexType> getEdge(VertexType source, VertexType dest)
    {
        VertexAttributes<VertexType> va = vertices.get(source);

        for (Edge<VertexType> e : va.outEdges)
        {
            if (e.getDest().equals(dest))
                return e;
        }
        return null;
    }

    /**
     * Returns the number of in-edges of the given vertex.
     *
     * @param v
     * @return the number of in-edges
     */
    public int getNumberOfInEdges(VertexType v)
    {
        VertexAttributes<VertexType> va = vertices.get(v);
        assert(va != null);
        return va.inEdges.size();
    }

    /**
     * Returns the iterator to iterate through all edges going
     * into the given object (i.e. all edges where the object
     * is the edge's destination)
     *
     * @param v the vertex for which to return the in edges.
     *
     * @return the iterator
     */
    public Iterator<Edge<VertexType>> getInEdges(VertexType v)
    {
        VertexAttributes<VertexType> va = vertices.get(v);
        assert(va != null);
        return va.inEdges.iterator();
    }

    public Iterator<VertexType> getParentNodes(VertexType vt)
    {
        final Iterator<Edge<VertexType>> iter = getInEdges(vt);

        return new Iterator<VertexType>()
        {
            @Override
            public boolean hasNext()
            {
                return iter.hasNext();
            }

            @Override
            public VertexType next()
            {
                return iter.next().getSource();
            }

            @Override
            public void remove()
            {
            }
        };
    }

    /**
     * Returns the number of in-edges of the given vertex.
     *
     * @param v
     * @return the number of out-edges
     */
    public int getNumberOfOutEdges(VertexType v)
    {
        VertexAttributes<VertexType> va = vertices.get(v);
        assert(va != null);
        return va.outEdges.size();
    }

    /**
     * Returns clustering coefficient as described in the paper
     * by Watts and Strogatz (1998 Nature).
     *
     * @param v
     * @return The clustering coefficient of the vertex.
     */
    public double getClusteringCoefficient(VertexType v)
    {
        VertexAttributes<VertexType> va = vertices.get(v);
        assert(va != null);

		/*
		 * Determine the neighborhood of provided vertex.
		 */
        HashSet<VertexType> neighborhood  = new HashSet<VertexType>();

        Iterator<VertexType> neighborsIt = getChildNodes(v);
        while (neighborsIt.hasNext())
            neighborhood.add( neighborsIt.next() );

        int numberNeighbors = neighborhood.size();

		/*
		 * For isolated nodes we set the CC to 0. This is kind of standard,
		 * even though in Brandes & Erlebach (Network Analysis) 2005 these
		 * are set to one. A discussion of this can be found in:
		 * Marcus Kaiser; New Journal of Physics (2008); "Mean clustering coefficients:
		 * the role of isolated nodes and leafs on clustering measures for small-world
		 * networks"
		 */
        if (numberNeighbors < 2)
            return 0;
		/*
		 * Now determine the number of links
		 * inside the neighborhood.
		 */
        int numEdgesNeighborhood = 0;
        for (VertexType neighbor : neighborhood){
			/*
			 * Get all nodes reachable from this node
			 */
            Iterator<VertexType> neighborsNeighborsIt = getChildNodes(neighbor);
            while (neighborsNeighborsIt.hasNext()){
                VertexType neighborsNeighbor = neighborsNeighborsIt.next();

                if (neighborsNeighbor == v)
                    continue;
                if (neighborsNeighbor == neighbor)
                    continue;

                if (neighborhood.contains(neighborsNeighbor))
                    ++numEdgesNeighborhood;
            }
        }

		/*
		 * Calculate clustering coefficient
		 */
        double denominator 	= (double)(numberNeighbors*(numberNeighbors-1));
        double C 			= (double)numEdgesNeighborhood / denominator;
        return C;
    }


    /**
     * Returns the iterator to iterate through all edges going
     * out of given object. (i.e. all edges where the object
     * is the edge's source)
     *
     * @param v the vertex for which all outgoing edges shall be returned
     *
     * @return the iterator for all outgoing edges
     */
    public Iterator<Edge<VertexType>> getOutEdges(VertexType v)
    {
        VertexAttributes<VertexType> va = vertices.get(v);
        assert(va != null);
        return va.outEdges.iterator();
    }

    /* (non-Javadoc)
     * @see sonumina.math.graph.AbstractGraph#getChildNodes(java.lang.Object)
     */
    public Iterator<VertexType> getChildNodes(VertexType vt)
    {
        final Iterator<Edge<VertexType>> iter = getOutEdges(vt);

        return new Iterator<VertexType>()
        {
            @Override
            public boolean hasNext()
            {
                return iter.hasNext();
            }

            @Override
            public VertexType next()
            {
                return iter.next().getDest();
            }

            @Override
            public void remove()
            {
            }
        };
    }

    /**
     * Returns the vertices in an Iterable that are connected by the given node.
     *
     * FIXME: Check if this is really returning descendants
     *
     * @param v the vertex for which the descendants shall be returned
     * @return the iterable
     */
    public Iterable<VertexType> getDescendantVertices(VertexType v)
    {
        VertexAttributes<VertexType> va = vertices.get(v);
        assert(va != null);

        List<VertexType> descendant = new ArrayList<VertexType>(va.outEdges.size());
        for (Edge<VertexType> e : va.outEdges)
            descendant.add(e.getDest());
        return descendant;
    }

    /**
     * Calculates the shortest path from the given vertex to all vertices. Note that
     * negative weights are not supported!
     *
     * @param vertex defines the source
     * @param againstFlow if specified the path is walked against the direction of the graph
     * @param visitor object implementing IDistanceVisitor which can be used to process the
     *        results
     */
    public void singleSourceShortestPath(VertexType vertex, boolean againstFlow, IDistanceVisitor<VertexType> visitor)
    {
        /**
         * This class implements some meta information needed by Dijkstra's
         * algorithm.
         *
         * @author Sebastian Bauer
         */
        class VertexExtension implements Comparable<VertexExtension>
        {
            /** The vertex */
            public VertexType vertex;

            /** The current distance of the vertex (to the source vertex) */
            public int distance;

            /** The current parent of the vertex */
            public VertexType parent;

            VertexExtension(VertexType vertex, int distance, VertexType parent)
            {
                this.vertex = vertex;
                this.distance = distance;
                this.parent = parent;
            }

            public int compareTo(VertexExtension arg0)
            {
                return distance - arg0.distance;
            }

            public int hashCode()
            {
                return vertex.hashCode();
            }
        }

        if (!vertices.containsKey(vertex))
            throw new IllegalArgumentException(vertex + " not found.");

		/* This is the implementation of the Dijkstra algorithm */

		/* Within the priority queue we maintain the vertices which has been already
		 * discovered by the algorithm.
		 *
		 * TODO: Get rid of Java's PriorityQueue by using a better suited io structure */
        PriorityQueue<VertexExtension> queue = new PriorityQueue<VertexExtension>();
        HashMap<VertexType,VertexExtension> map = new HashMap<VertexType,VertexExtension>();

		/* Place the starting node into the priorty queue. It has a distance of 0 and no parent */
        VertexExtension ve = new VertexExtension(vertex,0,null);
        queue.offer(ve);
        map.put((VertexType)ve.vertex,ve);

        while (!queue.isEmpty())
        {
			/* Take a node which has minimal distance to the starting node */
            VertexExtension next = queue.poll();

			/* We iterate over the edges of the chosen node to find the neighbours */
            Iterator<Edge<VertexType>> edgeIter;
            if (againstFlow) edgeIter = getInEdges((VertexType)next.vertex);
            else edgeIter = getOutEdges((VertexType)next.vertex);

            while (edgeIter.hasNext())
            {
                Edge<VertexType> edge = edgeIter.next();
                VertexType neighbour;

                if (againstFlow) neighbour = edge.getSource();
                else neighbour = edge.getDest();

				/* Relax the neighbour (or add it if it is not available) */
                VertexExtension neighbourExt = map.get(neighbour);
                if (neighbourExt == null)
                {
                    neighbourExt = new VertexExtension(neighbour, next.distance + edge.getWeight(), next.vertex);
                    map.put(neighbour,neighbourExt);
                    queue.offer(neighbourExt);
                } else
                {
					/* Would the edge from the current vertex to the neighbour
					 * make the path to the neighbour shorter? */
                    if (neighbourExt.distance > next.distance + edge.getWeight())
                    {
                        queue.remove(neighbourExt);
                        neighbourExt.distance = next.distance + edge.getWeight();
                        neighbourExt.parent = next.vertex;
                        queue.offer(neighbourExt);
                    }
                }
            }
        }

		/* Now throw out the results */
        for (Entry<VertexType,VertexExtension> v : map.entrySet())
        {
			/* Build the path by successively traversing the path from
			 * the current destination through the stored ancestors
			 * (parents) */
            LinkedList<VertexType> ll = new LinkedList<VertexType>();
            VertexExtension curVe = v.getValue();
            do
            {
                ll.addFirst((VertexType)curVe.vertex);
                curVe = map.get(curVe.parent);
            } while (curVe != null);

            if (!visitor.visit((VertexType)v.getValue().vertex,ll,v.getValue().distance))
                return;
        }
    }

    /**
     * The bellman-ford algorithm (computes single-source shortest paths in a weighted digraph)
     *
     * @param source
     * @param weightMultiplier multiplies the weights by the given factor.
     * @param visitor
     */
    public void bf(VertexType source, int weightMultiplier, IDistanceVisitor<VertexType> visitor)
    {
        /**
         * This class implements some meta information needed by the BF algorithm.
         *
         * @author Sebastian Bauer
         */
        class VertexExtension implements Comparable<VertexExtension>
        {
            public VertexType vertex;
            public int distance;
            public VertexType parent;

            public VertexExtension(VertexType vertex, int distance, VertexType parent)
            {
                this.vertex = vertex;
                this.distance = distance;
                this.parent = parent;
            }

            public int compareTo(VertexExtension arg0)
            {
                return distance - arg0.distance;
            }

            public int hashCode()
            {
                return vertex.hashCode();
            }
        }

        HashMap<VertexType,VertexExtension> map = new HashMap<VertexType,VertexExtension>();
        map.put(source, new VertexExtension(source,0,null));

		/* Vertices loop */
        for (int i=0;i<vertices.size();i++)
        {
            boolean changed = false;

			/* Edge loop */
            for (Entry<VertexType, VertexAttributes<VertexType>> ent : vertices.entrySet())
            {
                VertexType u = ent.getKey();

                VertexExtension uExt = map.get(u);
                if (uExt == null) continue;

                for (Edge<VertexType> edge : ent.getValue().outEdges)
                {
                    VertexType v = edge.getDest();


                    VertexExtension vExt = map.get(v);
                    if (vExt == null)
                    {
                        vExt = new VertexExtension(v, uExt.distance + edge.getWeight()*weightMultiplier, u);
                        map.put(v,vExt);
                        changed = true;
                    } else
                    {
                        if (vExt.distance > uExt.distance + edge.getWeight() * weightMultiplier)
                        {
                            vExt.distance = uExt.distance + edge.getWeight() * weightMultiplier;
                            vExt.parent = u;
                            changed = true;
                        }
                    }
                }
            }

			/* If this iteration doesn't affect a change, the next own won't change anything either */
            if (!changed)
                break;
        }

		/* Now throw out the results */
        for (Entry<VertexType,VertexExtension> v : map.entrySet())
        {
			/* Build the path by successively traversing the path from
			 * the current destination through the stored ancestors
			 * (parents) */
            LinkedList<VertexType> ll = new LinkedList<VertexType>();
            VertexExtension curVe = v.getValue();
            do
            {
                ll.addFirst((VertexType)curVe.vertex); /* FIXME: (VertexType) is for the java compiler */
                curVe = map.get(curVe.parent);
            } while (curVe != null);

            if (!visitor.visit((VertexType)v.getValue().vertex,ll,v.getValue().distance))  /* FIXME: (VertexType) is for the java compiler */
                return;
        }
    }

    /**
     * Calculates the shortest path from the given vertex to all vertices. Supports negative weights.
     *
     * @param source defines the source
     * @param visitor object implementing IDistanceVisitor which can be used to process the
     *        results
     */
    public void singleSourceShortestPathBF(VertexType source, IDistanceVisitor<VertexType> visitor)
    {
        bf(source,1,visitor);
    }

    /**
     * Calculates the longest path from the given vertex to all vertices.
     *
     * @param source defines the source
     * @param visitor object implementing IDistanceVisitor which can be used to process the
     *        results
     */
    public void singleSourceLongestPath(VertexType source, final IDistanceVisitor<VertexType> visitor)
    {
        bf(source,-1,new IDistanceVisitor<VertexType>()
        {
            public boolean visit(VertexType vertex, java.util.List<VertexType> path, int distance)
            {
                return visitor.visit(vertex, path, distance * -1);
            };
        });
    }

    /**
     * Returns the number of distinct paths from source to dest.
     *
     * @param source where to start.
     * @param dest where to end.
     * @return the number of paths.
     */
    public int getNumberOfPaths(VertexType source, VertexType dest)
    {
        if (source.equals(dest))
            return 1;

        int paths = 0;
        for (VertexType next : getDescendantVertices(source))
            paths += getNumberOfPaths(next,dest);
        return paths;
    }

    /**
     * Returns all paths from source to dest.
     *
     * @param source where to start.
     * @param dest where to end.
     * @return the number of paths.
     */
    public ArrayList<VertexType> getAllPathes(VertexType source, VertexType dest, ArrayList<VertexType> pathes)
    {
        if (source.equals(dest)){
            ArrayList<VertexType> ret = new ArrayList<VertexType>();
            ret.add(dest);
            return ret;
        }

        for (VertexType next : getDescendantVertices(source)){
            ArrayList<VertexType> rec = getAllPathes(next, dest, pathes);
            System.out.println("recur: "+rec);
            pathes.addAll(rec);
        }
        return pathes;
    }

    /**
     * @return an arbitrary node of the graph
     */
    public VertexType getArbitaryNode()
    {
        return vertices.entrySet().iterator().next().getKey();
    }

    /**
     * @return the number of vertices.
     */
    public int getNumberOfVertices()
    {
        return vertices.size();
    }

    /**
     * Allows convenient iteration.
     */
    public Iterator<VertexType> iterator()
    {
        return vertices.keySet().iterator();
    }

    /**
     * Get the in-degree of the given vertex.
     *
     * @param v vertex for which the in-degree shall be determined
     * @return the in-degree
     */
    public int getInDegree(VertexType v)
    {
        VertexAttributes<VertexType> va = vertices.get(v);
        if (va == null) return -1;
        return va.inEdges.size();
    }

    /**
     * Get the out-degree of the given vertex.
     *
     * @param v vertex for which the out-degree shall be determined
     * @return the out-degree
     */
    public int getOutDegree(VertexType v)
    {
        VertexAttributes<VertexType> va = vertices.get(v);
        if (va == null) return -1;
        return va.outEdges.size();
    }

    public int getMaxDegree()
    {
        int max = Integer.MIN_VALUE;
        for (VertexType vertex : vertices.keySet())
        {
            int degreeOut = this.getNumberOfOutEdges(vertex);
            int degreeIn = this.getNumberOfInEdges(vertex);
            if (degreeIn != degreeOut)
                throw new RuntimeException("Vertex "+vertex+" has indegree:"+degreeIn+" and outdegree:"+degreeOut);
            if (degreeOut > max)
                max = degreeOut;
        }
        return max;
    }

    public boolean areNeighbors(VertexType node1, VertexType node2)
    {

        if (node1.equals(node2))
            return true;

        VertexAttributes<VertexType> va = vertices.get(node1);
        for (Edge<VertexType> e : va.inEdges){
            VertexType ancestor = e.getSource();
            if (ancestor.equals(node2))
                return true;
        }
        for (Edge<VertexType> e : va.outEdges){
            VertexType desc = e.getDest();
            if (desc.equals(node2))
                return true;
        }
        return false;
    }

    /**
     * Returns a subgraph of the graph that includes all given vertices.
     *
     * @param verticesToBeIncluded
     * @return the subgraph
     */
    public DirectedGraph<VertexType> subGraph(Collection<VertexType> verticesToBeIncluded)
    {
        return subGraph(new HashSet<VertexType>(verticesToBeIncluded));
    }

    /**
     * Returns a subgraph of the graph that includes all given vertices. Edges are included
     * only, if it is spanned between two vertices in the given set.
     *
     * @param verticesToBeIncluded
     * @return the subgraph
     */
    public DirectedGraph<VertexType>subGraph(Set<VertexType> verticesToBeIncluded)
    {
        DirectedGraph<VertexType> graph = new DirectedGraph<VertexType>();

		/* Add vertices that should be contained in the subgraph */
        for (VertexType v : verticesToBeIncluded)
            graph.addVertex(v);

		/* Add edges (only one class of edges needs to be added) */
        for (VertexType v : verticesToBeIncluded)
        {
            Iterator<Edge<VertexType>> edges = getInEdges(v);
            while (edges.hasNext())
            {
                Edge<VertexType> e = edges.next();
                if (verticesToBeIncluded.contains(e.getSource()))
                    graph.addEdge(e);
            }
        }

        return graph;
    }

    /**
     * Returns the path-transitivity-maintaining transitive closure of
     * a subgraph that contains the given vertices.
     *
     * @param verticesToBeIncluded
     * @return the transitive closure of the subgraph
     */
    public DirectedGraph<VertexType> transitiveClosureOfSubGraph(final Set<VertexType> verticesToBeIncluded)
    {
		/* This is a naive implementation */
        final DirectedGraph<VertexType> graph = new DirectedGraph<VertexType>();

		/* Add vertices that should be contained in the subgraph */
        for (VertexType v : verticesToBeIncluded)
            graph.addVertex(v);

        for (final VertexType v1 : verticesToBeIncluded)
        {
            bfs(v1,false,new IVisitor<VertexType>() {
                public boolean visited(VertexType vertex)
                {
                    if (verticesToBeIncluded.contains(vertex))
                    {
                        graph.addEdge(new Edge<VertexType>(v1,vertex));
                    }
                    return true;
                };
            });
        }

        return graph;
    }

    /**
     *
     * Returns a sub graph with selected vertices, in which path relationships are
     * maintained. Basic version.
     *
     * @return
     */
    private DirectedGraph<VertexType> compactedSubgraph(final Set<VertexType> verticesToBeIncluded)
    {
		/* This is a naive implementation */
        DirectedGraph<VertexType> graph = copyGraph();

		/* Note that we iterate here over the nodes the this instance and
		 * not over the duplicated graph. We will remove nodes from there
		 * therefore iterating over those nodes is node safe.
		 */
        for (VertexType v : this)
        {
            if (!verticesToBeIncluded.contains(v))
                graph.removeVertexMaintainConnectivity(v);
        }

        return graph;
    }

    /**
     * Returns a sub graph with selected vertices, in which path relationships are
     * maintained.
     *
     * @param verticesToBeIncluded
     * @return the path maintaining subgraph
     */
    public DirectedGraph<VertexType> pathMaintainingSubGraph(Set<VertexType> verticesToBeIncluded)
    {
        DirectedGraph<VertexType> transitiveClosure = compactedSubgraph(verticesToBeIncluded);//transitiveClosureOfSubGraph(verticesToBeIncluded);
        DirectedGraph<VertexType> transitivitySubGraph;
        boolean reducedInIteration;


		/* Here we also want to ensure that no redunancies are included */

        do
        {
            reducedInIteration = false;

			/* Here, the reduced graph structure is stored */
            transitivitySubGraph = new DirectedGraph<VertexType>();
            for (VertexType v : verticesToBeIncluded)
                transitivitySubGraph.addVertex(v);

			/* Now add edges to the reduced graph structure, i.e., leave out
			 * edges that are redundant */
            for (VertexType v : verticesToBeIncluded)
            {
                Set<VertexType> vUpperVertices = transitiveClosure.getVerticesOfUpperInducedGraph(null,v);
                LinkedList<VertexType> parents = new LinkedList<VertexType>();
                Iterator<VertexType> parentIterator = transitiveClosure.getParentNodes(v);
                while (parentIterator.hasNext())
                    parents.add(parentIterator.next());

				/* Construct the upper graph by using only the parents. Always
				 * leave out a single parent. If that edge is redundant, the number
				 * of nodes in this newly created graph differs only by one.
				 */
                for (VertexType p : parents)
                {
                    HashSet<VertexType> pUpperVertices = new HashSet<VertexType>();

                    for (VertexType p2 : parents)
                    {
						/* Skip parent that should be left out */
                        if (p.equals(p2)) continue;

                        pUpperVertices.addAll(transitiveClosure.getVerticesOfUpperInducedGraph(null,p2));
                    }

                    if (pUpperVertices.size() != vUpperVertices.size() - 1)
                    {
						/* Here we know that the edge from p to v was relevant */
                        transitivitySubGraph.addEdge(new Edge<VertexType>(p,v));
                    } else
                    {
                        reducedInIteration = true;
                    }
                }
            }
            transitiveClosure = transitivitySubGraph;
        } while (reducedInIteration);

        return transitivitySubGraph;
    }

    /**
     * Returns a set of induced terms that are the terms of the induced graph.
     *
     * @param root the root term (all terms up to this are included)
     * @param termID the inducing term.
     * @return the set of vertices defining the upper induced graph
     */
    public Set<VertexType> getVerticesOfUpperInducedGraph(final VertexType root, VertexType termID)
    {
        /**
         * Visitor which simply add all nodes to the nodeSet.
         *
         * @author Sebastian Bauer
         */
        class Visitor implements IVisitor<VertexType>
        {
            public HashSet<VertexType> nodeSet = new HashSet<VertexType>();

            public boolean visited(VertexType vertex)
            {
                if (root != null)
                {
                    if (vertex.equals(root) || existsPath(root, vertex))
                        nodeSet.add(vertex);
                } else
                    nodeSet.add(vertex);

                return true;
            }
        };

        Visitor visitor = new Visitor();
        bfs(termID,true,visitor);

        return visitor.nodeSet;
    }

    /**
     * Merge equivalent vertices. First vertex given will be
     * the representative and thus we inherit all the edges
     * of the other equivalent vertices.
     *
     * @param vertex1
     * @param eqVertices
     */
    public void mergeVertices(VertexType vertex1, Iterable<VertexType> eqVertices)
    {
        for (VertexType vertex2 : eqVertices)
        {
            if (!vertices.containsKey(vertex2))
                return;

            VertexAttributes<VertexType> vertexTwoAttributes = vertices.get(vertex2);
            for (Edge<VertexType> e : vertexTwoAttributes.inEdges)
                e.setDest(vertex1);

            for (Edge<VertexType> e : vertexTwoAttributes.outEdges)
                e.setSource(vertex1);

            vertices.remove(vertex2);
        }
    }

    public boolean containsVertex(VertexType vertex){
        return vertices.containsKey(vertex);
    }
}