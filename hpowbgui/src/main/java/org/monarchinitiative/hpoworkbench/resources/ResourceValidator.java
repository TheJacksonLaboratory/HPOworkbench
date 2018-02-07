package org.monarchinitiative.hpoworkbench.resources;

/**
 * Classes that implement this interface are responsible for making sure that the resource {@link T} is valid, before
 * it is used in GUI.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.1.10
 * @see OptionalResources
 * @see ResourceValidators
 * @since 0.1
 */
@FunctionalInterface
public interface ResourceValidator<T> {

    boolean isValid(T resource);

}
