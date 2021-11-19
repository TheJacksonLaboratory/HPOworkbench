package org.monarchinitiative.hpoworkbench.gui.webpopup;

import javafx.stage.Stage;
import org.monarchinitiative.hpoworkbench.resources.OptionalHpoResource;
import org.monarchinitiative.hpoworkbench.resources.OptionalHpoaResource;
import org.monarchinitiative.hpoworkbench.resources.OptionalMondoResource;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;

import java.util.*;

public class SettingsPopup extends WebViewerPopup {

    private final String NOT_INITIALIZED = "not initialized";

    private final String html;

    private final Properties properties;
    private final OptionalHpoResource optionalHpoResource;

    private final OptionalMondoResource optionalMondoResource;

    private final OptionalHpoaResource optionalHpoaResource;

    public SettingsPopup(Properties pgProps ,
                         OptionalHpoResource optionalHpoResource,
                         OptionalMondoResource optionalMondoResource,
                         OptionalHpoaResource optionalHpoaResource,
                         Stage stage) {
        super(stage);
        this.properties = pgProps;
        this.optionalHpoResource = optionalHpoResource;
        this.optionalMondoResource = optionalMondoResource;
        this.optionalHpoaResource = optionalHpoaResource;
        this.html = getHTML();
    }

    private Map<String, String> getSettingsItems() {
        Map<String, String> items = new HashMap<>();
        if (properties.contains(OptionalResources.HP_JSON_PATH_PROPERTY)) {
            items.put("hp.json path", properties.getProperty(OptionalResources.HP_JSON_PATH_PROPERTY));
        } else {
            items.put("hp.json path",NOT_INITIALIZED);
        }
        if (properties.contains(OptionalResources.MONDO_PATH_PROPERTY)) {
            items.put("mondo.json path", properties.getProperty(OptionalResources.MONDO_PATH_PROPERTY));
        } else {
            items.put("mondo.json path",NOT_INITIALIZED);
        }
        if (properties.contains(OptionalResources.HPOA_PATH_PROPERTY)) {
            items.put("phenotype.hpoa path", properties.getProperty(OptionalResources.HPOA_PATH_PROPERTY));
        } else {
            items.put("phenotype.hpoa path",NOT_INITIALIZED);
        }
        // check the loaded ontologies etc.
        if (optionalHpoResource.getOntology() != null) {
            items.put("HPO", "initialized");
        } else {
            items.put("HPO", NOT_INITIALIZED);
        }
        if (optionalMondoResource.getOntology() != null) {
            items.put("Mondo", "initialized");
        } else {
            items.put("Mondo", NOT_INITIALIZED);
        }
        if (optionalHpoaResource.getId2diseaseModelMap() != null) {
            items.put("id2disease map", "initialized");
        } else {
            items.put("id2disease map", NOT_INITIALIZED);
        }
        if (optionalHpoaResource.getDirectAnnotMap() != null) {
            items.put("Direct annotation map", "initialized");
        } else {
            items.put("Direct annotation map", NOT_INITIALIZED);
        }
        if (optionalHpoaResource.getIndirectAnnotMap() != null) {
            items.put("Indirect annotation map", "initialized");
        } else {
            items.put("Indirect annotation map", NOT_INITIALIZED);
        }


        return items;
    }

    private String getLiRow(Map.Entry<String,String> e) {
        return String.format("<li>%s: %s</li>\n", e.getKey(), e.getValue());
    }

    private String getHTML() {
        Map<String, String> items = getSettingsItems();
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>");
        sb.append(inlineCSS());
        sb.append("""
                </head>
                <body><h2>HPO Workbench Settings</h2>
                <p>These parameters must be set (via the Setup menu) before annotating.</p>
                <p><ul>
                """);
        for (var e : items.entrySet()) {
            sb.append(getLiRow(e));
        }
        sb.append("""
                </ul></p>
                </body></html>
                                
                """);
        return sb.toString();
    }

    protected String inlineCSS() {
        return "<style>\n" +
                "  html { margin: 20; padding: 20; }" +
                "body { font: 100% georgia, sans-serif; line-height: 1.88889;color: #001f3f; margin: 0; padding: 0; }"+
                "p { margin-top: 10;text-align: justify;}"+
                "h2 {font-family: 'serif';font-size: 1.4em;font-style: normal;font-weight: bold;"+
                "letter-spacing: 1px; margin-bottom: 0; color: #001f3f;}"+
                "  </style>";
    }

    @Override
    public void popup() {
        showHtmlContent("Settings", html);
    }
}
