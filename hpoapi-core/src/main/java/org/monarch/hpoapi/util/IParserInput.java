package org.monarch.hpoapi.util;

import java.io.InputStream;

/**
 * An interface wrapping the input for miscellaneous parsers. It is intended to be used with classes such as {@link ParserFileInput}
 * and to provide a common interface that offers and inputStream.
 *
 * @author Sebastian Bauer, Peter Robinson
 * @version 0.2 (April 19, 2017)
 */
public interface IParserInput
{
    /**
     * @return the wrapped input stream
     */
    public InputStream inputStream();

    /**
     * Close the associated input streams.
     */
    public void close();

    /**
     * @return the size of the contents of the input stream or -1 if this
     *  information is not available.
     */
    public int getSize();

    /**
     * @return the current position of the input.
     */
    public int getPosition();

    /**
     * @return the filename associated to the input or null if no filename is associated.
     */
    public String getFilename();
}