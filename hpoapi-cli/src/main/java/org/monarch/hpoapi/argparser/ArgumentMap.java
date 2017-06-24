package org.monarch.hpoapi.argparser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the results of parsing with the Arguments and their values.
 */
public class ArgumentMap {


    private Map<String,Argument> argmap;
    private Map<String,Argument>  argwithoutvalueset;

    public ArgumentMap() {
        argmap=new HashMap<>();
        argwithoutvalueset=new HashMap<>();
    }

    public void addArgWithValue(Argument a, String value) {
        a.setValue(value);
        argmap.put(a.getName(),a);
    }
    public void addArgWithoutValue(Argument a) {
        argwithoutvalueset.put(a.getName(),a);
    }
}
