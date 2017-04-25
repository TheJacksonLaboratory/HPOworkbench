package org.monarch.hpoapi.graph;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.monarch.hpoapi.util.ObjectIntHashMap;
import org.monarch.hpoapi.graph.AbstractGraph.IVisitor;

/**
 * Instances of this class represent a slim view of a graph. Most attributes can be
 * accessed directly.
 *
 * @author Sebastian Bauer, Sebastian Koehler
 */
public final class SlimDirectedGraphView<VertexType>
{
    /** An array of all terms */
    private Object [] vertices;

    /** Map specific terms to the index in the allTerms array */
    private ObjectIntHashMap<VertexType> vertex2Index;

    /** Contains all the ancestors of the terms (and the terms itself).
     * Note that the array of ancestors is sorted. */
    public int [][] vertexAncestors;

    /** Contains the parents of the terms */
    public int [][] vertexParents;

    /** Contains the children of the term */
    public int [][] vertexChildren;

    /** Contains the descendants of the (i.e., children, grand-children, etc. and the term itself).
     * Note that the array of descendants is sorted.*/
    public int [][] vertexDescendants;

    /**
     * Default constructor.
     */
    public SlimDirectedGraphView()
    {
    }

    /**
     * Constructs a slim view from a given directed graph.
     *
     * @param graph
     * @deprecated use the create() method.
     */
    @Deprecated
    public SlimDirectedGraphView(DirectedGraph<VertexType> graph)
    {
        init(this, graph);
    }

    /**
     * @return the number of vertices.
     */
    public int getNumberOfVertices()
    {
        return vertices.length;
    }

    /**
     * Returns the vertex at the given index.
     *
     * @param index
     * @return the vertex
     */
    @SuppressWarnings("unchecked")
    public VertexType getVertex(int index)
    {
        return (VertexType)vertices[index];
    }

    /**
     * Returns the index of the given vertex.
     *
     * @param v
     * @return the index
     */
    public int getVertexIndex(VertexType v)
    {
        return vertex2Index.get(v);
    }

    /**
     * Returns the indices of the given vertices in a basic
     * array.
     *
     * @param vertices
     * @return the array of indices
     */
    public int [] getVertexIndices(Collection<VertexType> vertices)
    {
        int i;
        int [] vertexArray = new int[vertices.size()];

        i = 0;
        for (VertexType v : vertices)
            vertexArray[i++] = vertex2Index.get(v);

        return vertexArray;
    }

    /**
     * Determines whether node with the index i is an ancestor of node with index j.
     * Note that the ancestors of a given term include the given term itself.
     *
     * @param i
     * @param j
     * @return true if the node with the index i is an ancestor
     * of the node with the index j, otherwise false.
     */
    public boolean isAncestor(int i, int j)
    {
        int [] ancs = vertexAncestors[j];
        int r 		=  Arrays.binarySearch(ancs,i);
        return r >= 0;
    }

    /**
     * Determines whether the node with index i is a
     * descendant of the node with the index j.
     * Note that the descendants of a given term include the given term itself.
     *
     * @param i
     * @param j
     * @return true if the node with the index i is a descendant
     * of the node with the index j, otherwise false.
     */
    public boolean isDescendant(int i, int j)
    {
        int [] descs 	= vertexDescendants[j];
        int r 			= Arrays.binarySearch(descs,i);
        return r >= 0;
    }

    /**
     * Determines if the given vertex i is an ancestor of
     * the given vertex j.
     * @param i
     * @param j
     * @return true if the node i is an ancestor
     * of the node j, otherwise false.
     */
    public boolean isAncestor(VertexType i, VertexType j){

		/* Check that both nodes are present in graph */
        if ( (! isVertexInGraph(i)) || (! isVertexInGraph(j)))
            return false;

        int iIdx = vertex2Index.get(i);
        int jIdx = vertex2Index.get(j);

        return isAncestor(iIdx, jIdx);
    }

    /**
     * Determines if the given vertex i is a descendant of
     * the given vertex j.
     * @param i
     * @param j
     * @return true if the node i is a descendant
     * of the node j, otherwise false.
     */
    public boolean isDescendant(VertexType i, VertexType j){

		/* Check that both nodes are present in graph */
        if ( (! isVertexInGraph(i)) || (! isVertexInGraph(j)))
            return false;

        int iIdx = vertex2Index.get(i);
        int jIdx = vertex2Index.get(j);

        return isDescendant(iIdx, jIdx);
    }

    /**
     * Get the descendants of a given vertex as ArrayList of vertices.
     * @param t The vertex for that the descendant vertices should be found.
     * @return null if the given vertex was not found in the graph, otherwise an ArrayList of vertices
     * that are descendants of the given vertex.
     */
    public ArrayList<VertexType> getDescendants(VertexType t){

		/* check that this vertex is found in the graph */
        if ( ! isVertexInGraph(t)){
            return null;
        }

		/* get the index of the vertex */
        int indexOfTerm 						= getVertexIndex(t);
		/* get all descendent indices of the vertex */
        int[] descendantIndices					= vertexDescendants[indexOfTerm];

		/* init the return list of vertex-objects */
        ArrayList<VertexType> descendantObjects = new ArrayList<VertexType>(descendantIndices.length);

		/* convert each descendant-index to an vertex object */
        for (int descendantIdx : descendantIndices){
            VertexType descendantVertex = getVertex(descendantIdx);
            descendantObjects.add(descendantVertex);
        }
        return descendantObjects;
    }


    /**
     * Get the ancestors of a given vertex as ArrayList of vertices.
     * @param t The vertex for that the ancestor vertices should be found.
     * @return null if the given vertex was not found in the graph, otherwise an ArrayList of vertices
     * that are ancestors of the given vertex.
     */
    public ArrayList<VertexType> getAncestors(VertexType t){

		/* get the index of the vertex */
        int indexOfTerm 							= getVertexIndex(t);
		/* get all descendent indices of the vertex */
        int[] ancestorIndices					= vertexAncestors[indexOfTerm];

		/* init the return list of vertex-objects */
        ArrayList<VertexType> ancestorObjects 	= new ArrayList<VertexType>(ancestorIndices.length);

		/* convert each ancestor-index to an vertex object */
        for (int ancestorIdx : ancestorIndices){
            VertexType ancestorVertex = getVertex(ancestorIdx);
            ancestorObjects.add(ancestorVertex);
        }
        return ancestorObjects;
    }

    /**
     * Checks if a given vertex can be found in the graph.
     * @param The vertex to be searched.
     * @return True if the vertex can be found. False if not.
     */
    private boolean isVertexInGraph(VertexType vertex){
        return vertex2Index.containsKey(vertex);
    }


    public ArrayList<VertexType> getParents(VertexType t){

		/* get the index of the vertex */
        int indexOfTerm 							= getVertexIndex(t);
		/* get all indices of the vertex parents */
        int[] parentIndices						= vertexParents[indexOfTerm];

		/* init the return list of vertex-objects */
        ArrayList<VertexType> parentObjects = new ArrayList<VertexType>(parentIndices.length);

		/* convert each parent-index to an vertex object */
        for (int parentIdx : parentIndices){
            VertexType parentVertex = getVertex(parentIdx);
            parentObjects.add(parentVertex);
        }
        return parentObjects;
    }

    public ArrayList<VertexType> getChildren(VertexType t){

		/* get the index of the vertex */
        int indexOfTerm 							= getVertexIndex(t);
		/* get all indices of the vertex children */
        int[] childrenIndices					= vertexChildren[indexOfTerm];

		/* init the return list of vertex-objects */
        ArrayList<VertexType> childrenObjects 	= new ArrayList<VertexType>(childrenIndices.length);

		/* convert each child-index to an vertex object */
        for (int childIdx : childrenIndices){
            VertexType childVertex = getVertex(childIdx);
            childrenObjects.add(childVertex);
        }
        return childrenObjects;
    }

    /**
     * Initialize the slim graph view from a directed graph.
     *
     * @param slim
     * @param graph
     */
    @SuppressWarnings("unchecked")
    private static <V> void init(SlimDirectedGraphView<V> slim, DirectedGraph<V> graph)
    {
        int i;

		/* Vertices */
        slim.vertices = new Object[graph.getNumberOfVertices()];
        slim.vertex2Index = new ObjectIntHashMap<V>();
        i = 0;

        for (V t : graph)
        {
            slim.vertices[i] = t;
            slim.vertex2Index.put(t, i);
            i++;
        }

		/* Term parents stuff */
        slim.vertexParents = new int[slim.vertices.length][];
        for (i=0;i<slim.vertices.length;i++)
        {
            V v 					= (V)slim.vertices[i];
            Iterator<V> parentIter = graph.getParentNodes(v);
            slim.vertexParents[i] 				= createIndicesFromIter(slim.vertex2Index,parentIter);
        }

		/* Term ancestor stuff */
        slim.vertexAncestors = new int[slim.vertices.length][];
        for (i=0;i<slim.vertices.length;i++)
        {
            V v = (V)slim.vertices[i];
            final List<V> ancestors = new ArrayList<V>(20);
            graph.bfs(v, true, new IVisitor<V>() {
                public boolean visited(V vertex)
                {
                    ancestors.add(vertex);
                    return true;
                };
            });
            slim.vertexAncestors[i] = createIndicesFromIter(slim.vertex2Index,ancestors.iterator());

			/* Sort them, as we require this for binary search in isAncestor() */
            Arrays.sort(slim.vertexAncestors[i]);
        }

		/* Term children stuff */
        slim.vertexChildren = new int[slim.vertices.length][];
        for (i=0;i<slim.vertices.length;i++)
        {
            V v = (V)slim.vertices[i];

            Iterator<V> childrenIter 	= graph.getChildNodes(v);
            slim.vertexChildren[i] 		= createIndicesFromIter(slim.vertex2Index,childrenIter);
        }

		/* Term descendants stuff */
        slim.vertexDescendants = new int[slim.vertices.length][];
        for (i=0;i<slim.vertices.length;i++)
        {
            V v = (V)slim.vertices[i];
            final List<V> descendants = new ArrayList<V>(20);
            graph.bfs(v, false, new IVisitor<V>() {
                public boolean visited(V vertex)
                {
                    descendants.add(vertex);
                    return true;
                };
            });
            slim.vertexDescendants[i] = createIndicesFromIter(slim.vertex2Index, descendants.iterator());

			/* Sort them, as we require this for binary search in isDescendant() */
            Arrays.sort(slim.vertexDescendants[i]);
        }
    }

    /**
     * Creates an index array from the given vertex iterator.
     *
     * @param iterator
     * @return
     */
    private static <V> int[] createIndicesFromIter(ObjectIntHashMap<V> vertex2Index, Iterator<V> iterator)
    {
        ArrayList<Integer> indicesList = new ArrayList<Integer>(10);

        while (iterator.hasNext())
        {
            V p = iterator.next();
            int idx = vertex2Index.getIfAbsent(p, -1);
            if (idx != -1)
                indicesList.add(idx);
        }

        int [] indicesArray = new int[indicesList.size()];
        for (int i=0;i<indicesList.size();i++)
            indicesArray[i] = indicesList.get(i);

        return indicesArray;
    }

    /**
     * Create the slim view from the given directed graph.
     *
     * @param graph
     * @return the slim graph corresponding to graph
     */
    public static <V> SlimDirectedGraphView<V> create(DirectedGraph<V> graph)
    {
        SlimDirectedGraphView<V> g = new SlimDirectedGraphView<V>();
        init(g, graph);
        return g;
    }

    /**
     * Simple functional interface to provide a map form one type to another
     */
    public static interface Map<K,V>
    {
        public V map(K key);
    }

    /**
     * Create the slim view from the given directed graph but apply a mapping of the underlying
     * type.
     *
     * @param graph the graph from which a static mapping
     * @param map mapping
     * @return the slim graph view.
     */
    public static <K,V> SlimDirectedGraphView<V> create(DirectedGraph<K> graph, Map<K,V> map)
    {
        SlimDirectedGraphView<K> kg = create(graph);
        SlimDirectedGraphView<V> vg = new SlimDirectedGraphView<V>();

        vg.vertexAncestors = kg.vertexAncestors;
        vg.vertexChildren = kg.vertexChildren;
        vg.vertexDescendants = kg.vertexDescendants;
        vg.vertexParents = kg.vertexParents;

        vg.vertices = new Object[kg.vertices.length];
        vg.vertex2Index = new ObjectIntHashMap<V>(kg.vertices.length);
        for (int i = 0; i < vg.vertices.length; i++)
        {
            V v = map.map(kg.getVertex(i));
            vg.vertices[i] = v;
            vg.vertex2Index.put(v, i);
        }

        return vg;
    }
}