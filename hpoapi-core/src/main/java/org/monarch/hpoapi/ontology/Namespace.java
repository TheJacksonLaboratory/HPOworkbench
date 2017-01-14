package org.monarch.hpoapi.ontology;


import java.util.HashMap;

import org.monarch.hpoapi.types.ByteString;

/**
 * A basic class representing a name space.
 *
 * @author Sebastian Bauer
 */
public class Namespace
{
    /* This static mapping stuff is for historical reasons. This enum should be removed very soon */
    static public enum NamespaceEnum
    {
        BIOLOGICAL_PROCESS,
        MOLECULAR_FUNCTION,
        CELLULAR_COMPONENT,
        UNSPECIFIED;
    };
    static private HashMap<Namespace,NamespaceEnum> namespaceMap = new HashMap<Namespace,NamespaceEnum>();
    static
    {
        namespaceMap.put(new Namespace(new ByteString("biological_process")), NamespaceEnum.BIOLOGICAL_PROCESS);
        namespaceMap.put(new Namespace(new ByteString("molecular_function")), NamespaceEnum.MOLECULAR_FUNCTION);
        namespaceMap.put(new Namespace(new ByteString("cellular_component")), NamespaceEnum.CELLULAR_COMPONENT);
    };
    static public NamespaceEnum getNamespaceEnum(Namespace namespace)
    {
        NamespaceEnum e = namespaceMap.get(namespace);
        if (e == null) return NamespaceEnum.UNSPECIFIED;
        return e;
    }

    /** The global unknown namespace */
    public static Namespace UNKOWN_NAMESPACE = new Namespace(new ByteString("unknown"));

    private ByteString name;

    public Namespace(String newName)
    {
        this.name = new ByteString(newName);
    }

    public Namespace(ByteString newName)
    {
        this.name = newName;
    }

    public ByteString getName()
    {
        return name;
    }

    /**
     * Return the abbreviated name of the namespace.
     *
     * @return the abbreviated name of the namespace
     */
    public String getAbbreviatedName()
    {
        return Character.toUpperCase(name.toString().charAt(0)) + "";
    }

    @Override
    public boolean equals(Object obj)
    {
        return name.equals(((Namespace)obj).name);
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
