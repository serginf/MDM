package eu.supersede.mdm.storage.model;

/**
 * Created by snadal on 18/01/17.
 */
public enum DispatcherStrategiesTypes {

    ALL("Dispatch All"),
    SAMPLING("N/A Sampling"),
    SCHEMA_TYPECHECKING("N/A Schema Typechecking");

    private String element;

    DispatcherStrategiesTypes(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }

}
