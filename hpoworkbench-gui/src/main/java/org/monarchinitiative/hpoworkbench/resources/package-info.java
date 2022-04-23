/**
 * This aim of this package is to manage resources that are necessary for GUI functionality, but may not be available
 * during the whole run of the GUI (e.g. user needs to download them first). The value of resource is
 * <code>null</code>, until initialized.
 * <p>
 * <b>Recommended usage:</b>
 * <ul>
 * <li>wrap the resources (e.g. ontology object) in the {@link javafx.beans.property.ObjectProperty}</li>
 * <li>create binding using multiple properties (e.g. a binding that evaluates as <code>true</code> only when
 * all the resources are available)</li>
 * <li>bind the disableProperty of the buttons, textfields, etc.. to the binding</li>
 * </ul>
 */
package org.monarchinitiative.hpoworkbench.resources;