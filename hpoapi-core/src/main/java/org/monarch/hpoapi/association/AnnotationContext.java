package org.monarch.hpoapi.association;



import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.monarch.hpoapi.types.ByteString;
import org.monarch.hpoapi.collections.ObjectIntHashMap;
import org.monarch.hpoapi.collections.ObjectIntHashMap.ObjectIntProcedure;

/**
 * TODO document me
 * @author Sebastian Bauer, Peter Robinson
 */
public class AnnotationContext
{
    /** The symbols */
    private ByteString[] symbols;

    /** And the corresponding object ids */
    private ByteString[] objectIds;

    /** Maps object symbols to item indices within the items list */
    private ObjectIntHashMap<ByteString> objectSymbolMap;

    /** Maps object ids to item indices within the items list */
    private ObjectIntHashMap<ByteString> objectIdMap;

    /** Maps synonyms to item indices within the items list */
    private ObjectIntHashMap<ByteString> synonymMap;

    public AnnotationContext(Collection<ByteString> symbols, List<ByteString> objectIds, ObjectIntHashMap<ByteString> objectSymbolMap, ObjectIntHashMap<ByteString> objectIdMap, ObjectIntHashMap<ByteString> synonymMap)
    {
        if (symbols.size() != objectIds.size()) throw new IllegalArgumentException("Symbols and object ids size must match");

        this.symbols = new ByteString[symbols.size()];
        symbols.toArray(this.symbols);

        this.objectIds = new ByteString[objectIds.size()];
        objectIds.toArray(this.objectIds);

        this.objectSymbolMap = objectSymbolMap;
        this.objectIdMap = objectIdMap;
        this.synonymMap = synonymMap;
    }

    /**
     * Creates a mapping from a list and two other maps.
     *
     * @param symbols
     * @param synonym2Item
     * @param objectId2Item
     */
    public AnnotationContext(Collection<ByteString> symbols, HashMap<ByteString, ByteString> synonym2Item, HashMap<ByteString, ByteString> objectId2Item)
    {
        int initialSynonymMapSize = 32;
        int initialObjectIdMapSize = 32;

        objectSymbolMap = new ObjectIntHashMap<ByteString>();
        Set<ByteString> allSymbols = new HashSet<ByteString>(symbols);

        if (synonym2Item != null)
        {
            for (ByteString otherSymbol : synonym2Item.values())
                allSymbols.add(otherSymbol);
            initialSynonymMapSize = synonym2Item.size();
        }

        if (objectId2Item != null)
        {
            for (ByteString otherSymbol : objectId2Item.values())
                allSymbols.add(otherSymbol);
            initialObjectIdMapSize = objectId2Item.size();
        }

        this.symbols = new ByteString[allSymbols.size()];
        this.objectIds = new ByteString[allSymbols.size()];
        this.synonymMap = new ObjectIntHashMap<ByteString>(initialSynonymMapSize);
        this.objectIdMap = new ObjectIntHashMap<ByteString>(initialObjectIdMapSize);
        int i = 0;
        for (ByteString symbol : allSymbols)
        {
            objectSymbolMap.put(symbol, i);
            this.symbols[i] = symbol;
            i++;
        }

        if (synonym2Item != null)
        {
            for (Entry<ByteString, ByteString> e : synonym2Item.entrySet())
            {
                ByteString synonym = e.getKey();
                ByteString symbol = e.getValue();

                synonymMap.put(synonym, objectSymbolMap.get(symbol));
            }
        }

        if (objectId2Item != null)
        {
            for (Entry<ByteString, ByteString> e : objectId2Item.entrySet())
            {
                ByteString objectId = e.getKey();
                ByteString symbol = e.getValue();

                objectIdMap.put(objectId, objectSymbolMap.get(symbol));
            }
        }
    }

    /**
     * Construct and return the mapping from synonyms to symbols.
     *
     * @return the constructed map.
     */
    public HashMap<ByteString, ByteString> getSynonym2Symbol()
    {
        final HashMap<ByteString, ByteString> synonym2symbol = new HashMap<ByteString, ByteString>(synonymMap.size());
        synonymMap.forEachKeyValue(new ObjectIntProcedure<ByteString>()
        {
            @Override
            public void keyValue(ByteString key, int value)
            {
                synonym2symbol.put(key, symbols[value]);
            }
        });
        return synonym2symbol;
    }

    /**
     * Construct and return the mapping from object ids to symbols.
     *
     * @return the constructed map.
     */
    public HashMap<ByteString, ByteString> getDbObjectID2Symbol()
    {
        final HashMap<ByteString, ByteString> dbObjectID2gene = new HashMap<ByteString, ByteString>(synonymMap.size());
        objectIdMap.forEachKeyValue(new ObjectIntProcedure<ByteString>()
        {
            @Override
            public void keyValue(ByteString key, int value)
            {
                dbObjectID2gene.put(key, symbols[value]);
            }
        });
        return dbObjectID2gene;
    }

    /**
     * Return the array of symbols.
     *
     * @return array of symbols.
     */
    public ByteString [] getSymbols()
    {
        return symbols;
    }

    /**
     * Map the given symbol to the unique id.
     *
     * @param name
     * @return the id or Integer.MAX if mapping was not successful.
     */
    public int mapSymbol(ByteString name)
    {
        return objectSymbolMap.getIfAbsent(name, Integer.MAX_VALUE);
    }

    /**
     * Map the given objectid to the unique id.
     *
     * @param objectid
     * @return the id or Integer.MAX if mapping was not successful.
     */
    public int mapObjectID(ByteString objectid)
    {
        return objectIdMap.getIfAbsent(objectid, Integer.MAX_VALUE);
    }

    /**
     * Map the given symbol to the unique id.
     *
     * @param synonym
     * @return the id or Integer.MAX if mapping was not successful.
     */
    public int mapSynonym(ByteString synonym)
    {
        return synonymMap.getIfAbsent(synonym, Integer.MAX_VALUE);
    }

    public int getNumberOfSynonyms()
    {
        return synonymMap.size();
    }
}
