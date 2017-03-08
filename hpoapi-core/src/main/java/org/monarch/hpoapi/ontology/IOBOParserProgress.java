package org.monarch.hpoapi.ontology;

/**
 * Created by robinp on 3/8/17.
 */

public interface IOBOParserProgress
{
    /**
     * Called upon initialization.
     *
     * @param max maximal number of steps.
     */
    void init(int max);

    /**
     * Called arbitrary.
     *
     * @param current the current number of steps.
     * @param terms number of parsed terms.
     */
    void update(int current, int terms);
}