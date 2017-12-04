package eu.supersede.mdm.storage.model.bdi_ontology.eca_rules;

/**
 * Created by snadal on 20/01/17.
 */
public enum ActionTypes {
    ALERT_EVOLUTION("Software Evolution Alert"),
    ALERT_DYNAMIC_ADAPTATION("Dynamic Adaptation Alert");

    private String element;

    ActionTypes(String element) {
            this.element = element;
        }

    public String val() {
            return element;
        }
}
