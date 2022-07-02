package org.monarchinitiative.hpoworkbench.resources;

import javafx.application.HostServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Host services are not always available (i.e. during unit tests). The {@link #isActive()} will return <code>false</code>
 * and {@link #showDocument(String)} will do nothing.
 */
public record HostServicesWrapper(HostServices hostServices) {
    private static final Logger LOGGER = LoggerFactory.getLogger(HostServicesWrapper.class);

    public static HostServicesWrapper wrap(HostServices hostServices) {
        return new HostServicesWrapper(hostServices);
    }

    public static HostServicesWrapper getInactiveWrapper() {
        return new HostServicesWrapper(null);
    }

    public void showDocument(String uri) {
        if (hostServices != null) {
            LOGGER.debug("Showing '{}'", uri);
            hostServices.showDocument(uri);
        } else {
            LOGGER.warn("Requested usage of host services while unavailable");
        }
    }

    /**
     * @return <code>true</code> if the wrapper is not a <code>null</code> value, and therefore it would not work
     */
    public boolean isActive() {
        return hostServices != null;
    }

}
