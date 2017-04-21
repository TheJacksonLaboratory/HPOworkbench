package org.monarch.hpoapi.io;

/**
 * Created by robinp on 4/7/17.
 */

public interface IAssociationParserProgress
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
     */
    void update(int current);

    /**
     * In case of a warning.
     *
     * @param message
     */
    void warning(String message);
}