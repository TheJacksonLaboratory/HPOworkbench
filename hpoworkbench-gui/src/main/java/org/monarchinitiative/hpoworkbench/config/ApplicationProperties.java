package org.monarchinitiative.hpoworkbench.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {

    private final String applicationUiTitle;

    private final String version;

    private final String hpoJsonUrl;

    private final String mondoJsonUrl;


    private final String phenotypeHpoaUrl;

    @Autowired
    public ApplicationProperties(@Value("${spring.application.ui.title}") String uiTitle,
                                 @Value("${hpowb.version}") String version,
                                 @Value("${hpo.json.url}") String hpoJson,
                                 @Value("${mondo.obo.url}") String mondoJson,
                                 @Value("${hpo.phenotype.annotations.url}") String annotsUrl) {
        this.applicationUiTitle = uiTitle;
        this.version = version;
        this.hpoJsonUrl = hpoJson;
        this.mondoJsonUrl = mondoJson;
        this.phenotypeHpoaUrl = annotsUrl;
    }

    public String getApplicationUiTitle() {
        return applicationUiTitle;
    }

    public String getVersion() {
        return version;
    }

    public String getHpoJsonUrl() {
        return hpoJsonUrl;
    }

    public String getMondoJsonUrl() {
        return mondoJsonUrl;
    }

    public String getPhenotypeHpoaUrl() {
        return phenotypeHpoaUrl;
    }
}
