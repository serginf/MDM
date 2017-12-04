package eu.supersede.mdm.storage.cep.RDF_Model.event;

import java.util.LinkedList;

/**
 * Created by osboxes on 14/04/17.
 */
public abstract class Pattern extends CEPElement {

    protected LinkedList<CEPElement> CEPElements;

    public Pattern(LinkedList<CEPElement> CEPElements) {
        super();
        this.CEPElements = CEPElements;
    }

    public Pattern() {
        super();
        CEPElements = new LinkedList<>();
    }

    public Pattern(LinkedList<CEPElement> CEPElements, String IRI) {
        super(IRI);
        this.CEPElements = CEPElements;
    }

    public Pattern(String IRI) {
        super(IRI);
        CEPElements = new LinkedList<>();
    }

    public void addEvents(CEPElement CEPElement) {
        CEPElements.add(CEPElement);
    }

    public LinkedList<CEPElement> getCEPElements() {
        return CEPElements;
    }

    public void setCEPElements(LinkedList<CEPElement> CEPElements) {
        this.CEPElements = CEPElements;
    }

//    public abstract Pattern getInstance(List<CEPElement> CEPElements);

//    public abstract Pattern getInstance();

//    public abstract void addEvent(CEPElement event) throws Exception;
}
