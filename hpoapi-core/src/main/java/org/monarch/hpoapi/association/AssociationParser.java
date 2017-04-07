package org.monarch.hpoapi.association;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.monarch.hpoapi.util.AbstractByteLineScanner;
import org.monarch.hpoapi.ontology.IParserInput;
import org.monarch.hpoapi.ontology.TermID;
import org.monarch.hpoapi.ontology.TermMap;
import org.monarch.hpoapi.types.ByteString;

/**
 * This class is responsible for parsing GO association files. One object is
 * made for each entry; since genes can have 0, 1, or more than one synonyms, we also parse the
 * synonym field and create a mapping from each synonym to the gene-association
 * object. Therefore, if the user enters the synonym of a gene, we may still be
 * able to identify it.
 *
 * @author Peter Robinson, Sebastian Bauer
 * @see Association
 * @see <A HREF="http://www.geneontology.org">www.geneontology.org</A>
 */

public class AssociationParser
{
    private static Logger logger = Logger.getLogger(AssociationParser.class.getName());

    enum Type
    {
        UNKNOWN,
        GAF,
        IDS,
        AFFYMETRIX
    };

    private IParserInput input;
    private TermMap terms;
    private HashSet<ByteString> names;
    private Collection<String> evidences;
    private IAssociationParserProgress progress;
    private boolean iterative;
    private boolean parsingFinished;

    /** Mapping from gene (or gene product) names to Association objects */
    private ArrayList<Association> associations;

    /** The mapping */
    private AnnotationContext annotationMapping;

    /** The file type of the association file which was parsed */
    private Type fileType = Type.UNKNOWN;

    /** Counts the symbol warnings */
    private int symbolWarnings;

    /** Counts the dbObject warnings */
    private int dbObjectWarnings;

    /**
     * Construct the association parser object. The given file name will
     * parsed. Convenience constructor when not using progress monitor.
     *
     * @param input
     * @param terms
     * @throws IOException
     */
    public AssociationParser(IParserInput input, TermMap terms) throws IOException
    {
        this(input,terms,null);
    }


    /**
     * Construct the association parser object. The given file name will
     * parsed. Convenience constructor when not using progress monitor.
     *
     * @param input
     * @param terms
     * @param names
     * @throws IOException
     */
    public AssociationParser(IParserInput input, TermMap terms, HashSet<ByteString> names) throws IOException
    {
        this(input,terms,names,null);
    }

    /**
     * Construct the association parser object. The given file name will
     * parsed.
     *
     * @param input specifies wrapping input that contains association of genes to GO terms.
     * @param terms the container of the GO terms
     * @param names list of genes from which the associations should be gathered.
     *        If null all associations are taken,
     * @param progress
     * @throws IOException
     */
    public AssociationParser(IParserInput input, TermMap terms, HashSet<ByteString> names, IAssociationParserProgress progress) throws IOException
    {
        this(input,terms,names,null,progress);
    }


    /**
     * Construct the association parser object. The given file name will
     * parsed.
     *
     * @param input specifies wrapping input that contains association of genes to GO terms.
     * @param terms the container of the GO terms
     * @param names list of genes from which the associations should be gathered.
     *        If null all associations are taken,
     * @param progress
     * @throws IOException
     */
    public AssociationParser(IParserInput input, TermMap terms, HashSet<ByteString> names, Collection<String> evidences, IAssociationParserProgress progress) throws IOException
    {
        this(input,terms,names,evidences,progress,false);
    }

    /**
     * Construct the association parser object. The given file name will
     * parsed.
     *
     * @param input specifies wrapping input that contains association of genes to GO terms.
     * @param terms the container of the GO terms
     * @param names list of genes from which the associations should be gathered.
     *        If null all associations are taken,
     * @param evidences keep only the annotation whose evidence match the given ones. If null, all annotations are used.
     *        Note that this field is currently used when the filenames referes to a GAF file.
     * @param progress
     * @param iterative set to true if no actual parsing should be done in the constructor.
     * @throws IOException
     */
    public AssociationParser(IParserInput input, TermMap terms, HashSet<ByteString> names, Collection<String> evidences, IAssociationParserProgress progress, boolean iterative) throws IOException
    {
        this.input = input;
        this.terms = terms;
        this.names = names;
        this.evidences = evidences;
        this.progress = progress;
        this.iterative = iterative;

        associations = new ArrayList<Association>();

        if (iterative)
        {
            return;
        }

        parse();
    }

    /**
     * Start or continue to parse the associations. This needs only to be called when the
     * parser was created with the iterative flag set to true.
     *
     * @return true if parsing is completed. Otherwise, false is returned in which
     *  case you must call parse() again.
     * @throws IOException
     */
    public boolean parse() throws IOException
    {
        if (parsingFinished)
        {
            return true;
        }

        long startMillis = System.currentTimeMillis();

        if (input.getFilename().endsWith(".ids"))
        {
            importIDSAssociation(input,terms,progress);
            fileType = Type.IDS;
        } else
        {
			/* First, skip headers */
            final List<byte[]> lines = new ArrayList<byte[]>();
            AbstractByteLineScanner abls = new AbstractByteLineScanner(input.inputStream()) {
                @Override
                public boolean newLine(byte[] buf, int start, int len)
                {
                    if (len > 0 && buf[start] != '#')
                    {
                        byte [] b = new byte[len+1];
                        System.arraycopy(buf, start, b, 0, len);
                        b[len] = 10;
                        lines.add(b);
                        return false;
                    }
                    return true;
                }
            };
            abls.scan();

            if (lines.size() == 0)
                return true;

            byte [] head = merge(lines.get(0), abls.availableBuffer());

            importGAF(input,head,names,terms,evidences,progress);
            fileType = Type.GAF;

        }

        long durationMillis = System.currentTimeMillis() - startMillis;
        if (durationMillis == 0)
        {
            durationMillis = 1;
        }
        logger.log(Level.INFO, "Parsed annotations in " + durationMillis + " ms" + " (" + (associations.size() * 1000 / durationMillis) + " per second)");

        parsingFinished = true;
        return true;
    }

    /**
     * Import the annotation from a file generated by GOStat.
     *
     * @param input
     * @param
     */
    private void importIDSAssociation(IParserInput input, TermMap terms, IAssociationParserProgress progress)
    {
        try
        {
            BufferedReader is = new BufferedReader(new InputStreamReader(input.inputStream()));
            String line;

            while ((line = is.readLine()) != null)
            {
                if (line.equalsIgnoreCase("GoStat IDs Format Version 1.0"))
                    continue;

                String [] fields = line.split("\t",2);

                if (fields.length != 2) continue;

                String [] annotatedTerms = fields[1].split(",");

                for (int i = 0; i <annotatedTerms.length; i++)
                {

                    TermID tid;

                    try
                    {
                        tid = new TermID(annotatedTerms[i]);
                    } catch (IllegalArgumentException ex)
                    {
                        int id = new Integer(annotatedTerms[i]);
                        tid = new TermID(TermID.DEFAULT_PREFIX,id);
                    }

                    if (terms.get(tid) != null)
                    {
                        Association assoc = new Association(new ByteString(fields[0]),tid.toString());
                        associations.add(assoc);
                    } else
                    {
                        logger.warning(tid.toString() + " which annotates " + fields[0] + " not found");
                    }
                }
            }
        } catch (IOException e)
        {
        }
    }

    /**
     * Get from a collection of strings a ByteString set.
     *
     * @param strings
     * @return
     */
    private static Set<ByteString> getByteStringSetFromStringCollection(Collection<String> strings)
    {
        Set<ByteString> byteStrings; /* Evidences converted to ByteString */

        if (strings != null)
        {
            byteStrings = new HashSet<ByteString>();
            for (String e : strings)
                byteStrings.add(new ByteString(e));
        } else
        {
            byteStrings = null;
        }
        return byteStrings;
    }

    /**
     * Import GAF.
     *
     * @param input the wrapped input.
     * @param head the header of the file. Basically, the beginning of the text until the current position of the input.
     * @param names names of items that are interesting or null if annotations of them should be considered
     * @param terms all known terms
     * @param evidences specifies which annotations to take.
     * @param progress used for monitoring progress.
     * @throws IOException
     */
    private void importGAF(IParserInput input, byte [] head, HashSet<ByteString> names, TermMap terms, Collection<String> evidences, IAssociationParserProgress progress) throws IOException
    {
        if (progress != null)
            progress.init(input.getSize());

        GAFByteLineScanner ls = new GAFByteLineScanner(input, head, names, terms,getByteStringSetFromStringCollection(evidences), progress);
        ls.scan();

        if (progress != null)
            progress.update(input.getSize());

        logger.log(Level.INFO, ls.good + " associations parsed, " + ls.kept
                + " of which were kept while " + ls.bad
                + " malformed lines had to be ignored.");
        logger.log(Level.INFO, "A further " + ls.skipped
                + " associations were skipped due to various reasons whereas "
                + ls.nots + " of those where explicitly qualified with NOT, " +
                + ls.obsolete + " referred to obsolete terms and "
                + ls.evidenceMismatch + " didn't"
                + " match the requested evidence codes");
        logger.log(Level.INFO, "A total of " + ls.getNumberOfUsedTerms()
                + " terms are directly associated to " + ls.getAnnotationContext().getSymbols().length
                + " items.");

        associations = ls.getAssociations();
        annotationMapping = ls.getAnnotationContext();

        if (symbolWarnings >= 1000)
            logger.warning("The symbols of a total of " + symbolWarnings + " entries mapped ambiguously");
        if (dbObjectWarnings >= 1000)
            logger.warning("The objects of a  total of " + dbObjectWarnings + " entries mapped ambiguously");
    }

    /**
     * @return all parsed associations.
     */
    public ArrayList<Association> getAssociations()
    {
        return associations;
    }

    public AnnotationContext getAnnotationMapping()
    {
        return annotationMapping;
    }

    /**
     * @return the list of object symbols of all associations.
     */
    public List<ByteString> getListOfObjectSymbols()
    {
        ArrayList<ByteString> arrayList = new ArrayList<ByteString>();

        for (Association assoc : associations)
            arrayList.add(assoc.getObjectID());

        return arrayList;
    }

    /**
     * @return the file type of the associations.
     */
    public Type getFileType()
    {
        return fileType;
    }

    /**
     * Merge two byte arrays.
     *
     * @param a1
     * @param a2
     * @return
     */
    private static byte [] merge(byte [] a1, byte [] a2)
    {
        byte [] b = new byte[a1.length + a2.length];
        System.arraycopy(a1, 0, b, 0, a1.length);
        System.arraycopy(a2, 0, b, a1.length,a2.length);
        return b;
    }
}
