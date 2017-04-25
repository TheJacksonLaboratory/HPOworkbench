package org.monarch.hpoapi.ontology;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.monarch.hpoapi.graph.DirectedGraph;
import org.monarch.hpoapi.graph.Edge;
import org.monarch.hpoapi.graph.SlimDirectedGraphView;
import org.monarch.hpoapi.graph.AbstractGraph.INeighbourGrabber;
import org.monarch.hpoapi.graph.AbstractGraph.IVisitor;
import org.monarch.hpoapi.graph.DirectedGraph.IDistanceVisitor;

/**
 * Represents an edge in the ontology graph
 *
 * @author Sebastian Bauer
 */
class OntologyEdge extends Edge<Term>
{
    /** Relation always to the parent (source) */
    private TermRelation relation;

    public void setRelation(TermRelation relation)
    {
        this.relation = relation;
    }

    public TermRelation getRelation()
    {
        return relation;
    }

    public OntologyEdge(Term source, Term dest, TermRelation relation)
    {
        super(source, dest);

        this.relation = relation;
    }
}

/**
 * Represents the whole ontology. Note that the terms "parents" and "children" are
 * used somewhat mixed here.
 *
 * @author Sebastian Bauer
 */
public class Ontology implements Iterable<Term>
{
    private static Logger logger = Logger.getLogger(Ontology.class.getName());

    /** The five top level sobontologies of the HPO */
    private static HashSet<String> level1TermNames = new HashSet<String>(Arrays.asList("Phenotypic abnormality","Mortality/Aging", "Mode of inheritance", "Clinical modifier", "Frequency"));

    /** The graph */
    private DirectedGraph<Term> graph;

    /** The {@link TermContainer} contains each of the HPO terms parsed from the input OBO file. */
    private TermContainer termContainer;

    /** The (possibly) artificial root term */
    private Term rootTerm;

    /** Level 1 terms */
    private List<Term> level1terms = new ArrayList<Term>();

    /** Available subsets */
    private HashSet <Subset> availableSubsets = new HashSet<Subset>();

    /**
     * Terms often have alternative IDs (mostly from term merges). This map is used by
     * getTermIncludingAlternatives(String termIdString) and initialized there lazily.
     * Todo should this be ByteString?
     */
    private HashMap<String, String> alternativeId2primaryId;


    private Ontology() { }

    /**
     * Returns the induced subgraph which contains the terms with the given ids.
     *
     * @param termIDs
     * @return the subgraph induced by given the term ids.
     */
    public Ontology getInducedGraph(Collection<TermID> termIDs)
    {
        Ontology subgraph 		= new Ontology();
        HashSet<Term> allTerms 	= new HashSet<Term>();

        for (TermID tid : termIDs)
            for (TermID tid2 : getTermsOfInducedGraph(null, tid))
                allTerms.add(getTerm(tid2));

        subgraph.availableSubsets 	= availableSubsets;
        subgraph.graph 				= graph.subGraph(allTerms);
        subgraph.termContainer 		= termContainer;
        subgraph.availableSubsets 	= availableSubsets;

        subgraph.assignLevel1TermsAndFixRoot();

        return subgraph;
    }

    /**
     * @return terms that have no descendants.
     */
    public ArrayList<Term> getLeafTerms()
    {
        ArrayList<Term> leafTerms = new ArrayList<Term>();
        for (Term t : graph.getVertices())
        {
            if (graph.getOutDegree(t) == 0)
                leafTerms.add(t);
        }

        return leafTerms;
    }

    /**
     * @return term id of terms that have no descendants.
     */
    public Collection<TermID> getLeafTermIDs()
    {
        ArrayList<TermID> leafTerms = new ArrayList<TermID>();
        for (Term t : graph.getVertices())
        {
            if (graph.getOutDegree(t) == 0)
                leafTerms.add(t.getID());
        }

        return leafTerms;
    }

    /**
     * @return the term in topological order.
     */
    public ArrayList<Term> getTermsInTopologicalOrder()
    {
        return graph.topologicalOrder();
    }

    /**
     * @return a slim representation of the ontology.
     */
    public SlimDirectedGraphView<Term> getSlimGraphView()
    {
        return SlimDirectedGraphView.create(graph);
    }

    /**
     * @return a slim representation with TermIDs as underlying type.
     */
    public SlimDirectedGraphView<TermID> getTermIDSlimGraphView()
    {
        return SlimDirectedGraphView.create(graph, new SlimDirectedGraphView.Map<Term,TermID>()
        {
            @Override
            public TermID map(Term key)
            {
                return key.getID();
            }
        });
    }

    /**
     * Finds about level 1 terms and fix the root as we assume here
     * that there is only a single root.
     */
    private void assignLevel1TermsAndFixRoot()
    {
        level1terms = new ArrayList<Term>();

		/* Find the terms without any ancestors */
        for (Term term : graph)
        {
            if (graph.getInDegree(term) == 0 && !term.isObsolete())
                level1terms.add(term);
        }

        if (level1terms.size() > 1)
        {
            StringBuilder level1StringBuilder = new StringBuilder();
            level1StringBuilder.append("\"");
            level1StringBuilder.append(level1terms.get(0).getName());
            level1StringBuilder.append("\"");
            for (int i=1;i<level1terms.size();i++)
            {
                level1StringBuilder.append(" ,\"");
                level1StringBuilder.append(level1terms.get(i).getName());
                level1StringBuilder.append("\"");
            }

            String rootName = "root";
            if (level1terms.size() == 3)
            {
                boolean isGO = false;
                for (Term t : level1terms)
                {
                    if (level1TermNames.contains(t.getName().toString().toLowerCase())) isGO = true;
                    else
                    {
                        isGO = false;
                        break;
                    }
                }
                if (isGO) rootName = "Gene Ontology";
            }

            rootTerm = new Term(level1terms.get(0).getID().getPrefix().toString()+":0000000", rootName);

            logger.log(Level.INFO,"Ontology contains multiple level-one terms: " + level1StringBuilder.toString() + ". Adding artificial root term \"" + rootTerm.getID().toString() + "\".");

            rootTerm.setSubsets(new ArrayList<Subset>(availableSubsets));
            graph.addVertex(rootTerm);

            for (Term lvl1 : level1terms)
            {
                graph.addEdge(new OntologyEdge(rootTerm, lvl1,TermRelation.UNKOWN));
            }
        } else
        {
            if (level1terms.size() == 1)
            {
                rootTerm = level1terms.get(0);
                logger.log(Level.INFO,"Ontology contains a single level-one term ("+ rootTerm.toString() + "");
            }
        }
    }

    /**
     * @return whether the given id is the id of the (possible artificial)
     *  root term
     */
    public boolean isRootTerm(TermID id)
    {
        return id.equals(rootTerm.getID());
    }

    /**
     * Determines if the given term is the artificial root term.
     *
     * @param id the id of the term to check
     * @return true if id is the artificial root term.
     */
    public boolean isArtificialRootTerm(TermID id)
    {
        return isRootTerm(id) && getLevel1Terms().contains(id);
    }

    /**
     * Get (possibly artificial) TermID of the root vertex of graph
     *
     * @return The term representing to root
     */
    public Term getRootTerm()
    {
        return rootTerm;
    }

    /**
     * @return all available subsets.
     */
    public Collection<Subset> getAvailableSubsets()
    {
        return availableSubsets;
    }

    /**
     * Utility method to return a term from the given termId.
     * If the root term id is given then the root term (that
     * is not necessarily contained in the termContainer)
     * will be returned.
     *
     * @param termID as string
     * @return the term or null if no such term could be found.
     */
    private Term getTermOrRoot(String termID)
    {
        Term term;
        if (termID.equals(rootTerm.getIDAsString()))
            term = rootTerm;
        else
            term = termContainer.get(termID);
        return term;
    }

    /**
     * Return the set of term IDs containing the given term's descendants.
     *
     * @param termID as string
     * @return the set of term ids of children as a set of strings
     */
    public Set<String> getTermChildrenAsStrings(String termID)
    {
        Term term = getTermOrRoot(termID);

        HashSet<String> terms = new HashSet<String>();
        Iterator<Edge<Term>> edgeIter = graph.getOutEdges(term);
        while (edgeIter.hasNext())
            terms.add(edgeIter.next().getDest().getIDAsString());
        return terms;
    }

    /**
     * Return the set of term IDs containing the given term's ancestors.
     *
     * @param termID - the id as a string
     * @return the set of term ids of parents as a set of strings.
     */
    public Set<String> getTermParentsAsStrings(String termID)
    {
        Term term = getTermOrRoot(termID);

        HashSet<String> terms = new HashSet<String>();

        Iterator<Edge<Term>> edgeIter = graph.getInEdges(term);
        while (edgeIter.hasNext())
            terms.add(edgeIter.next().getSource().getIDAsString());
        return terms;
    }

    /**
     * Return the set of term IDs containing the given term's children.
     *
     * @param termID - the term's id as a TermID
     * @return the set of termID of the descendants as term-IDs
     */
    public Set<TermID> getTermChildren(TermID termID)
    {
        Term goTerm;
        if (rootTerm.getID().id == termID.id)
            goTerm = rootTerm;
        else
            goTerm = termContainer.get(termID);

        HashSet<TermID> terms = new HashSet<TermID>();
        Iterator<Edge<Term>> edgeIter = graph.getOutEdges(goTerm);
        while (edgeIter.hasNext())
            terms.add(edgeIter.next().getDest().getID());
        return terms;
    }

    /**
     * Return the set of terms containing the given term's children.
     *
     * @param term - the term for which the children should be returned
     * @return the set of terms of the descendants as terms
     */
    public Set<Term> getTermChildren(Term term)
    {
        Term goTerm;
        if (rootTerm.getID().id == term.getID().id)
            goTerm = rootTerm;
        else
            goTerm = termContainer.get(term.getID());

        HashSet<Term> terms = new HashSet<Term>();
        Iterator<Edge<Term>> edgeIter = graph.getOutEdges(goTerm);
        while (edgeIter.hasNext())
            terms.add(edgeIter.next().getDest());
        return terms;
    }

    /**
     * Return the set of term IDs containing the given term-ID's ancestors.
     *
     * @param goTermID
     * @return the set of Term-IDs of ancestors
     */
    public Set<TermID> getTermParents(TermID goTermID)
    {
        HashSet<TermID> terms = new HashSet<TermID>();
        if (rootTerm.getID().id == goTermID.id)
            return terms;

        Term goTerm;
        if (goTermID.equals(rootTerm.getIDAsString()))
            goTerm = rootTerm;
        else
            goTerm = termContainer.get(goTermID);

        Iterator<Edge<Term>> edgeIter = graph.getInEdges(goTerm);
        while (edgeIter.hasNext())
            terms.add(edgeIter.next().getSource().getID());
        return terms;
    }

    /**
     * Return the set of terms that are parents of the given term.
     *
     * @param term
     * @return the set of Terms of parents
     */
    public Set<Term> getTermParents(Term term)
    {
        HashSet<Term> terms = new HashSet<Term>();
        if (rootTerm.getID().id == term.getID().id)
            return terms;

        Term goTerm;
        if (term.getID().equals(rootTerm.getIDAsString()))
            goTerm = rootTerm;
        else
            goTerm = termContainer.get(term.getID());

        Iterator<Edge<Term>> edgeIter = graph.getInEdges(goTerm);
        while (edgeIter.hasNext())
            terms.add(edgeIter.next().getSource());
        return terms;
    }


    /**
     * Return the set of GO term IDs containing the given GO term's parents.
     *
     * @param goTermID
     * @return the set of parent terms including the type of relationship.
     */
    public Set<ParentTermID> getTermParentsWithRelation(TermID goTermID)
    {
        HashSet<ParentTermID> terms = new HashSet<ParentTermID>();
        if (rootTerm.getID().id == goTermID.id)
            return terms;

        Term goTerm;
        if (goTermID.equals(rootTerm.getIDAsString()))
            goTerm = rootTerm;
        else
            goTerm = termContainer.get(goTermID);

        Iterator<Edge<Term>> edgeIter = graph.getInEdges(goTerm);
        while (edgeIter.hasNext())
        {
            OntologyEdge t = (OntologyEdge)edgeIter.next();
            terms.add(new ParentTermID(t.getSource().getID(),t.getRelation()));
        }

        return terms;
    }

    /**
     * Get the relation that relates term to the parent or null.
     *
     * @param parent selects the parent
     * @param term selects the term
     * @return the relation type of term and the parent term or null if no
     *  parent is not the parent of term.
     */
    public TermRelation getDirectRelation(TermID parent, TermID term)
    {
        Set<ParentTermID> parents = getTermParentsWithRelation(term);
        for (ParentTermID p : parents)
            if (p.termid.equals(parent)) return p.relation;
        return null;
    }

    /**
     * Returns the siblings of the term, i.e., terms that are also children of the
     * parents.
     *
     * @param tid
     * @return set the siblings
     */
    public Set<TermID> getTermsSiblings(TermID tid)
    {
        Set<TermID> parentTerms = getTermParents(tid);
        HashSet<TermID> siblings = new HashSet<TermID>();
        for (TermID p : parentTerms)
            siblings.addAll(getTermChildren(p));
        siblings.remove(tid);
        return siblings;
    }

    /**
     * Determines if there exists a directed path from sourceID to destID on the
     * ontology graph (in that direction).
     *
     * @param sourceID the id of the source term
     * @param destID teh id of the destination term
     */
    public boolean existsPath(TermID sourceID, TermID destID)
    {
		/* Some special cases because of the artificial root */
        if (isRootTerm(destID))
        {
            if (isRootTerm(sourceID))
                return true;
            return false;
        }

		/*
		 * We walk from the destination to the source against the graph
		 * direction. Basically a breadth-depth search is done.
		 */

        final boolean [] pathExists = new boolean[1];
        final Term source = termContainer.get(sourceID);
        Term dest = termContainer.get(destID);

        graph.bfs(dest, true, new IVisitor<Term>()
        {
            @Override
            public boolean visited(Term vertex)
            {
                if (!vertex.equals(source))
                    return true;

                pathExists[0] = true;
                return false;
            }
        });

        return pathExists[0];
    }

    /**
     * This interface is used as a callback mechanisim by the walkToSource()
     * and walkToSinks() methods.
     *
     * @author Sebastian Bauer
     */
    public interface IVisitingGOVertex extends IVisitor<Term>{};

    /**
     * Starting at the vertex representing goTermID walk to the source of the
     * DAG (ontology vertex) and call the method visiting of given object
     * implementimg IVisitingGOVertex.
     *
     * @param goTermID
     *            the TermID to start with (note that visiting() is also called
     *            for this vertex)
     *
     * @param vistingVertex
     */
    public void walkToSource(TermID goTermID, IVisitingGOVertex vistingVertex)
    {
        ArrayList<TermID> set = new ArrayList<TermID>(1);
        set.add(goTermID);
        walkToSource(set, vistingVertex);
    }

    /**
     * Convert a collection of termids to a list of terms.
     *
     * @param termIDSet
     * @return
     */
    private ArrayList<Term> termIDsToTerms(Collection<TermID> termIDSet)
    {
        ArrayList<Term> termList = new ArrayList<Term>(termIDSet.size());
        for (TermID id : termIDSet)
        {
            Term t;

            if (isRootTerm(id)) t = rootTerm;
            else t = termContainer.get(id);
            if (t == null)
                throw new IllegalArgumentException("\"" + id + "\" could not be mapped to a known term!");

            termList.add(t);
        }
        return termList;
    }

    /**
     * Starting at the vertices within the goTermIDSet walk to the source of the
     * DAG (ontology vertex) and call the method visiting of given object
     * Implementing IVisitingGOVertex.
     *
     * @param termIDSet
     *            the set of go TermsIDs to start with (note that visiting() is
     *            also called for those vertices/terms)
     *
     * @param vistingVertex
     */
    public void walkToSource(Collection<TermID> termIDSet, IVisitingGOVertex vistingVertex)
    {
        graph.bfs(termIDsToTerms(termIDSet), true, vistingVertex);
    }

    /**
     * Starting at the vertices within the goTermIDSet walk to the source of the
     * DAG (ontology vertex) and call the method visiting of given object
     * Implementing IVisitingGOVertex. Only relations in relationsToFollow are
     * considered.
     *
     * @param termIDSet
     * @param vistingVertex
     * @param relationsToFollow
     */
    public void walkToSource(Collection<TermID>  termIDSet, IVisitingGOVertex vistingVertex, final Set<TermRelation> relationsToFollow)
    {
        graph.bfs(termIDsToTerms(termIDSet), new INeighbourGrabber<Term>() {
            public Iterator<Term> grabNeighbours(Term t)
            {
                Iterator<Edge<Term>> inIter = graph.getInEdges(t);
                ArrayList<Term> termsToConsider = new ArrayList<Term>();
                while (inIter.hasNext())
                {
                    OntologyEdge edge = (OntologyEdge)inIter.next(); /* Ugly cast */
                    if (relationsToFollow.contains(edge.getRelation()))
                        termsToConsider.add(edge.getSource());
                }
                return termsToConsider.iterator();
            }
        }, vistingVertex);
    }

    /**
     * Starting at the vertices within the goTermIDSet walk to the sinks of the
     * DAG and call the method visiting of given object implementing
     * IVisitingGOVertex.
     *
     * @param goTermID
     *            the TermID to start with (note that visiting() is also called
     *            for this vertex)
     *
     * @param vistingVertex
     */

    public void walkToSinks(TermID goTermID, IVisitingGOVertex vistingVertex)
    {
        ArrayList<TermID> set = new ArrayList<TermID>(1);
        set.add(goTermID);
        walkToSinks(set, vistingVertex);
    }

    /**
     * Starting at the vertices within the goTermIDSet walk to the sinks of the
     * DAG and call the method visiting of given object implementing
     * IVisitingGOVertex.
     *
     * @param goTermIDSet
     *            the set of go TermsIDs to start with (note that visiting() is
     *            also called for those vertices/terms)
     *
     * @param vistingVertex
     */
    public void walkToSinks(Collection<TermID> goTermIDSet, IVisitingGOVertex vistingVertex)
    {
        graph.bfs(termIDsToTerms(goTermIDSet), false, vistingVertex);
    }

    /**
     * Returns the term container attached to this ontology graph.
     * Note that the term container usually contains all terms while
     * the graph object may contain a subset.
     *
     * @return the term container attached to this ontology graph.
     * @deprecated Use getTermMap
     */
    @Deprecated
    public TermMap getTermContainer()
    {
        return termContainer;
    }

    /**
     * Return the term map attached to this ontology graph.
     * Note that the term container usually contains all terms while
     * the graph object may contain a subset.
     *
     * @return the term map
     */
    public TermMap getTermMap()
    {
        return termContainer;
    }

    /**
     * Returns the term represented by the given term id string or null.
     *
     * @param termId the term string id
     * @return the proper term object corresponding to the given term string id.
     */
    public Term getTerm(String termId)
    {
        Term go = termContainer.get(termId);
        if (go == null)
        {
			/* GO Term Container doesn't include the root term so we have to handle
			 * this case for our own.
			 */
            try
            {
                TermID id = new TermID(termId);
                if (id.id == rootTerm.getID().id)
                    return rootTerm;
            } catch (IllegalArgumentException iea)
            {
            }
        }
		/*
		 * In order to avoid the returning of terms that
		 * are only in the TermContainer but not in the graph
		 * we check here that the term is contained in the graph.
		 */
        if (  ! graph.containsVertex(go) ){
            return null;
        }

        return go;
    }

    /**
     * A method to get a term using the term-ID as string.
     * If no term with the given primary ID is found all
     * alternative IDs are used. If still no term is found null is returned.
     *
     * @param termId the term id string
     * @return the term.
     */
    public Term getTermIncludingAlternatives(String termId)
    {

        // try using the primary id
        Term term = getTerm(termId);
        if (term != null)
            return term;

		/*
		 *  no term with this primary id exists -> use alternative ids
		 */

        // do we already have a mapping between alternative ids and primary ids ?
        if (alternativeId2primaryId == null)
            setUpMappingAlternativeId2PrimaryId();

        // try to find a mapping to a primary term-id
        if (alternativeId2primaryId.containsKey(termId)){
            String primaryId 	= alternativeId2primaryId.get(termId);
            term 				= termContainer.get(primaryId);
        }

        // term still null?
        if (term == null)
        {
			/* GO Term Container doesn't include the root term so we have to handle
			 * this case for our own.
			 */
            try
            {
                TermID id = new TermID(termId);
                if (id.id == rootTerm.getID().id)
                    return rootTerm;
            } catch (IllegalArgumentException iea)
            {
            }
        }
        return term;
    }

    private void setUpMappingAlternativeId2PrimaryId() {
        alternativeId2primaryId = new HashMap<String, String>();
        for (Term t : this.termContainer){
            String primaryId = t.getIDAsString();
            for (TermID alternativeTermId : t.getAlternatives()){
                alternativeId2primaryId.put(alternativeTermId.toString(), primaryId);
            }
        }

    }

    /**
     * Returns the full-fledged term given its id.
     *
     * @param id the term id
     * @return the term instance.
     */
    public Term getTerm(TermID id)
    {
        Term go = termContainer.get(id);
        if (go == null && id.id == rootTerm.getID().id)
            return rootTerm;
        return go;
    }


    /**
     * Returns whether the given term is included in the graph.
     *
     * @param term which term to check
     * @return if term is included in the graph.
     */
    public boolean termExists(TermID term)
    {
        return graph.getOutDegree(getTerm(term)) != -1;
    }


    /**
     * Returns the set of terms given from the set of term ids.
     *
     * @param termIDs
     * @return set of terms
     *
     * @deprecated use termSet
     */
    public Set<Term> getSetOfTermsFromSetOfTermIds(Set<TermID> termIDs)
    {
        return termSet(termIDs);
    }

    /**
     * Return a set of terms given an iterable instance of term id objects.
     *
     * @param termIDs
     * @return set of terms
     */
    public Set<Term> termSet(Iterable<TermID> termIDs)
    {
        HashSet<Term> termSet = new HashSet<Term>();
        for (TermID tid : termIDs)
            termSet.add(getTerm(tid));
        return termSet;
    }

    /**
     * Return a list of term ids given an iterable instance of term objects.
     * Mostly a work horse for the other termIDList() methods.
     *
     * @param termIDs a collection where to store the term ids.
     * @param terms the terms whose ids shall be placed into termIdList
     * @return a collection of term ids
     */
    private static <A extends Collection<TermID>> A termIDs(A termIDs, Iterable<Term> terms)
    {
        for (Term t : terms)
            termIDs.add(t.getID());
        return termIDs;
    }

    /**
     * Return a list of term ids given an iterable instance of term objects.
     *
     * @param terms the collection of term objects
     * @return list of term ids
     */
    public static List<TermID> termIDList(Iterable<Term> terms)
    {
        return termIDs(new ArrayList<TermID>(), terms);
    }

    /**
     * Return a list of term ids given a collection of term objects.
     *
     * @param terms the collection of term objects
     * @return list of term ids
     */
    public static List<TermID> termIDList(Collection<Term> terms)
    {
        return termIDs(new ArrayList<TermID>(terms.size()), terms);
    }

    /**
     * Return a set of term ids given a collection of term objects.
     *
     * @param terms iterable of terms
     * @return set of term ids
     */
    public static Set<TermID> termIDSet(Iterable<Term> terms)
    {
        return termIDs(new HashSet<TermID>(), terms);
    }

    /**
     * Returns a set of induced terms that are the terms of the induced graph.
     * Providing null as root-term-ID will induce all terms up to the root to be included.
     *
     * @param rootTermID the root term (all terms up to this are included). if you provide null all terms
     * up to the original root term are included.
     * @param termID the inducing term.
     *
     * @return set of term ids
     */
    public Set<TermID> getTermsOfInducedGraph(final TermID rootTermID, TermID termID)
    {
        HashSet<TermID> nodeSet = new HashSet<TermID>();

        /**
         * Visitor which simply add all nodes to the nodeSet.
         *
         * @author Sebastian Bauer
         */
        class Visitor implements IVisitingGOVertex
        {
            public Ontology graph;
            public HashSet<TermID> nodeSet;

            public boolean visited(Term term)
            {
                if (rootTermID != null && !graph.isRootTerm(rootTermID))
                {
					/*
					 * Only add the term if there exists a path
					 * from the requested root term to the visited
					 * term.
					 *
					 * TODO: Instead of existsPath() implement
					 * walkToGoTerm() to speed up the whole stuff
					 */
                    if (term.getID().equals(rootTermID) || graph.existsPath(rootTermID, term.getID()))
                        nodeSet.add(term.getID());
                } else
                    nodeSet.add(term.getID());

                return true;
            }
        };

        Visitor visitor = new Visitor();
        visitor.nodeSet = nodeSet;
        visitor.graph = this;

        walkToSource(termID, visitor);

        return nodeSet;
    }

    /**
     * @return all level 1 terms.
     */
    public Collection<Term> getLevel1Terms()
    {
        return level1terms;
    }

    /**
     * Returns the parents shared by both t1 and t2.
     *
     * @param t1 term 1
     * @param t2 term 2
     * @return set of term ids that defines the terms shared by t1 and t2
     */
    public Collection<TermID> getSharedParents(TermID t1, TermID t2)
    {
        final Set<TermID> p1 = getTermsOfInducedGraph(null,t1);

        final ArrayList<TermID> sharedParents = new ArrayList<TermID>();

        walkToSource(t2, new IVisitingGOVertex()
        {
            public boolean visited(Term t2)
            {
                if (p1.contains(t2.getID()))
                    sharedParents.add(t2.getID());
                return true;
            }
        });

        return sharedParents;
    }

    static public class GOLevels
    {
        private HashMap<Integer,HashSet<TermID>> level2terms = new HashMap<Integer,HashSet<TermID>>();
        private HashMap<TermID,Integer> terms2level = new HashMap<TermID,Integer>();

        private int maxLevel = -1;

        public void putLevel(TermID tid, int distance)
        {
            HashSet<TermID> levelTerms = level2terms.get(distance);
            if (levelTerms == null)
            {
                levelTerms = new HashSet<TermID>();
                level2terms.put(distance, levelTerms);
            }
            levelTerms.add(tid);
            terms2level.put(tid,distance);

            if (distance > maxLevel) maxLevel = distance;
        }

        /**
         * Returns the level of the given term.
         *
         * @param tid
         * @return the level or -1 if the term is not included.
         */
        public int getTermLevel(TermID tid)
        {
            Integer level = terms2level.get(tid);
            if (level == null) return -1;
            return level;
        }

        public Set<TermID> getLevelTermSet(int level)
        {
            return level2terms.get(level);
        }

        public int getMaxLevel()
        {
            return maxLevel;
        }
    };


    /**
     * Returns the levels of the given terms starting from the root. Considers
     * only the relevant terms.
     *
     * @param termids
     * @return levels of the terms as defined in the set.
     */
    public GOLevels getGOLevels(final Set<TermID> termids)
    {
        DirectedGraph<Term> transGraph;
        Term transRoot;

        if ((getRelevantSubontology() != null && !isRootTerm(getRelevantSubontology())) || getRelevantSubset() != null)
        {
            Ontology ontologyTransGraph = getOntlogyOfRelevantTerms();
            transGraph = ontologyTransGraph.graph;
            transRoot = ontologyTransGraph.getRootTerm();
        } else
        {
            transGraph = graph;
            transRoot = rootTerm;
        }

        final GOLevels levels = new GOLevels();

        transGraph.singleSourceLongestPath(transRoot, new IDistanceVisitor<Term>()
        {
            public boolean visit(Term vertex, List<Term> path,
                                 int distance)
            {
                if (termids.contains(vertex.getID()))
                    levels.putLevel(vertex.getID(),distance);
                return true;
            }});
        return levels;
    }

    /**
     * Returns the number of terms in this ontology
     *
     * @return the number of terms.
     */
    public int getNumberOfTerms()
    {
        return graph.getNumberOfVertices();
    }

    /**
     * @return the highest term id used in this ontology.
     */
    public int maximumTermID()
    {
        int id=0;

        for (Term t : termContainer)
        {
            if (t.getID().id > id)
                id = t.getID().id;
        }

        return id;
    }

    /**
     * Returns an iterator to iterate over all terms
     */
    public Iterator<Term> iterator()
    {
        return graph.getVertexIterator();
    }

    private Subset relevantSubset;
    private Term relevantSubontology;


    /**
     * Sets the relevant subset.
     *
     * @param subsetName
     */
    public void setRelevantSubset(String subsetName)
    {
        for (Subset s : availableSubsets)
        {
            if (s.getName().equals(subsetName))
            {
                relevantSubset = s;
                return;
            }
        }

        relevantSubset = null;
        throw new IllegalArgumentException("Subset \"" + subsetName + "\" couldn't be found!");
    }

    /**
     * @return the current relevant subject.
     */
    public Subset getRelevantSubset()
    {
        return relevantSubset;
    }

    /**
     * Sets the relevant subontology.
     *
     * @param subontologyName
     */
    public void setRelevantSubontology(String subontologyName)
    {
		/* FIXME: That's so slow */
        for (Term t : termContainer)
        {
            if (t.getName().equals(subontologyName))
            {
                relevantSubontology = t;
                return;
            }
        }
        throw new IllegalArgumentException("Subontology \"" + subontologyName + "\" couldn't be found!");
    }

    /**
     * @return the relevant subontology.
     */
    public TermID getRelevantSubontology()
    {
        if (relevantSubontology != null) return relevantSubontology.getID();
        return rootTerm.getID();
    }

    /**
     * Returns whether the given term is relevant (i.e., is contained in a relevant sub ontology and subset).
     *
     * @param term the term to check
     * @return whether term is relevant.
     */
    public boolean isRelevantTerm(Term term)
    {
        if (relevantSubset != null)
        {
            boolean found = false;
            for (Subset s : term.getSubsets())
            {
                if (s.equals(relevantSubset))
                {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        if (relevantSubontology != null)
        {
            if (term.getID().id != relevantSubontology.getID().id)
                if (!(existsPath(relevantSubontology.getID(), term.getID())))
                    return false;
        }

        return true;
    }

    /**
     * Returns whether the given term is relevant (i.e., is contained in a relevant sub ontology and subset).
     *
     * @param termId defines the id of the term to check.
     * @return whether the term specified by the term id is relevant
     */
    public boolean isRelevantTermID(TermID termId)
    {
        Term t;
        if (isRootTerm(termId)) t = rootTerm;
        else t = termContainer.get(termId);

        return isRelevantTerm(t);
    }

    /**
     * Returns a redundant relation to this term.
     *
     * @param t term to check
     * @return null, if there is no redundant relation
     */
    public TermID findARedundantISARelation(Term t)
    {
		/* We implement a naive algorithm which results straight-forward from
		 * the definition: A relation is redundant if it can be removed without
		 * having a effect on the reachability of the nodes.
		 */
        Set<TermID> parents = getTermParents(t.getID());

        Set<TermID> allInducedTerms = getTermsOfInducedGraph(null,t.getID());

        for (TermID p : parents)
        {
            HashSet<TermID> thisInduced = new HashSet<TermID>();

            for (TermID p2 : parents)
            {
				/* Leave out the current parent */
                if (p.equals(p2)) continue;

                thisInduced.addAll(getTermsOfInducedGraph(null, p2));
            }

            if (thisInduced.size() == allInducedTerms.size() - 1)
                return p;

        }

        return null;
    }

    /**
     * Finds redundant is a relations and outputs them.
     */
    public void findRedundantISARelations()
    {
        for (Term t : this)
        {
            TermID redundant = findARedundantISARelation(t);
            if (redundant != null)
            {
                System.out.println(t.getName() + " (" + t.getIDAsString() + ") -> " + getTerm(redundant).getName() + "(" + redundant.toString() +")");
            }
        }
    }

    /**
     * @return the graph of relevant terms.
     */
    public Ontology getOntlogyOfRelevantTerms()
    {
        HashSet<Term> terms = new HashSet<Term>();
        for (Term t : this)
            if (isRelevantTerm(t)) terms.add(t);

        DirectedGraph<Term> trans = graph.pathMaintainingSubGraph(terms);

        Ontology g 		= new Ontology();
        g.graph 			= trans;
        g.termContainer	= termContainer;
        g.assignLevel1TermsAndFixRoot();

		/* TODO: Add real GOEdges */

        return g;
    }

    public DirectedGraph<Term> getGraph() {
        return graph;
    }

    /**
     * Merges equivalent terms. The first term given to this
     * method will be the representative of this
     * "equivalence-cluster".
     * @param t1
     * @param eqTerms
     */
    public void mergeTerms(Term t1, Iterable<Term> eqTerms)
    {
        HashSet<TermID> t1ExistingAlternatives = new HashSet<TermID>(Arrays.asList(t1.getAlternatives()));
        for (Term t : eqTerms)
        {
            TermID tId = t.getID();

            if (t1ExistingAlternatives.contains(tId))
                continue;

            t1.addAlternativeId(tId);
        }

        this.graph.mergeVertices(t1,eqTerms);
    }

    /**
     * Init the ontology from a term container.
     *
     * @param o the ontology to be initialized
     * @param tc the term container from which to init the ontology.
     */
    private static void init(Ontology o, TermContainer tc)
    {
        o.termContainer = tc;
        o.graph = new DirectedGraph<Term>();

		/* At first add all goterms to the graph */
        for (Term term : tc)
            o.graph.addVertex(term);

        int skippedEdges = 0;

		/* Now add the edges, i.e. link the terms */
        for (Term term : tc)
        {
            if (term.getSubsets() != null)
                for (Subset s : term.getSubsets())
                    o.availableSubsets.add(s);

            for (ParentTermID parent : term.getParents())
            {
				/* Ignore loops */
                if (term.getID().equals(parent.termid))
                {
                    logger.log(Level.INFO,"Detected self-loop in the definition of the ontology (term "+ term.getIDAsString()+"). This link has been ignored.");
                    continue;
                }
                if (tc.get(parent.termid) == null)
                {
					/* FIXME: We may want to add a new vertex to graph here instead */
                    logger.log(Level.INFO,"Could not add a link from term " + term.toString() + " to " + parent.termid.toString() +" as the latter's definition is missing.");
                    ++skippedEdges;
                    continue;
                }
                o.graph.addEdge(new OntologyEdge(tc.get(parent.termid), term, parent.relation));
            }
        }

        if (skippedEdges > 0)
            logger.log(Level.INFO,"A total of " + skippedEdges + " edges were skipped.");
        o.assignLevel1TermsAndFixRoot();

    }

    /**
     * Create an ontology from a term container.
     *
     * @param tc defines the term container
     * @return the ontology derived from tc
     */
    public static Ontology create(TermContainer tc)
    {
        Ontology o = new Ontology();
        init(o, tc);
        return o;
    }
}