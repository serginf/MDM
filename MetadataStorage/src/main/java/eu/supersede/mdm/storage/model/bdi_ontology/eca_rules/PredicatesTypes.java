package eu.supersede.mdm.storage.model.bdi_ontology.eca_rules;

/**
 * Created by snadal on 20/01/17.
 */
public enum PredicatesTypes {
    EQUAL("=="),
    NOT_EQUAL("!="),
    GREATER_THAN(">"),
    GREATER_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_OR_EQUAL("<=");

    private String element;

    PredicatesTypes(String element) {
            this.element = element;
        }

    public String val() {
            return element;
        }
}
