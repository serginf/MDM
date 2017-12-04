package eu.supersede.mdm.storage.cep.RDF_Model.Operators;

import eu.supersede.mdm.storage.cep.Interpreter.Interpreter;

/**
 * Created by osboxes on 17/05/17.
 */
public abstract class Operator implements Interpreter {
    protected String IRI;

    public Operator(String IRI) {
        this.IRI = IRI;
    }

    public Operator() {
    }

    public String getIRI() {
        return IRI;
    }

    public void setIRI(String IRI) {
        this.IRI = IRI;
    }
}
