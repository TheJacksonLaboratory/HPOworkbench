package org.monarch.hpoapi.util;


import java.util.HashMap;

/**
 * A io structure to hold a pool of references. Simpler
 * than a factory.
 *
 * @author Sebastian Bauer
 *
 * @param <T>
 */
public class ReferencePool<T>
{
    /**
     * The container for all refs. Have to use a HashMap here,
     * as it is not possible to retrieve the reference
     * via a HashSet.
     */
    private HashMap<T,T> referenceMap = new HashMap<T,T>();

    public T map(T toBeMapped)
    {
        T ref = referenceMap.get(toBeMapped);
        if (ref != null) return ref;
        referenceMap.put(toBeMapped, toBeMapped);
        return toBeMapped;
    }
}