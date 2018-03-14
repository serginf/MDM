package eu.supersede.mdm.storage.model.metamodel;

import eu.supersede.mdm.storage.model.Namespaces;

/**
 * Created by snadal on 6/06/17.
 */
public enum SourceOntology {

    DATA_SOURCE(Namespaces.S.val()+"DataSource"),
    WRAPPER(Namespaces.S.val()+"Wrapper"),
    ATTRIBUTE(Namespaces.S.val()+"Attribute"),

    HAS_WRAPPER(Namespaces.S.val()+"hasWrapper"),
    HAS_ATTRIBUTE(Namespaces.S.val()+"hasAttribute");


    private String element;

    SourceOntology(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }
}
