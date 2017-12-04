package eu.supersede.mdm.storage.cep.RDF_Model.event;

import eu.supersede.mdm.storage.cep.Interpreter.Interpreter;

/**
 * Created by osboxes on 14/04/17.
 */
public abstract class CEPElement implements Interpreter {
    protected String IRI;

    public CEPElement(String IRI) {
        this.IRI = IRI;
    }

    public CEPElement() {
    }

    public String getIRI() {
        return IRI;
    }

    public void setIRI(String IRI) {
        this.IRI = IRI;
    }
}
