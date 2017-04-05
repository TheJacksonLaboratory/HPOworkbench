package org.monarch.hpoapi.ontology;


import org.monarch.hpoapi.util.ReferencePool;

/**
 * A common pool for Prefix instances.
 *
 * @author Sebastian Bauer
 */
public class PrefixPool
{
    private ReferencePool<Prefix> prefixPool = new ReferencePool<>();

    public Prefix map(Prefix ref)
    {
        return prefixPool.map(ref);
    }
}

