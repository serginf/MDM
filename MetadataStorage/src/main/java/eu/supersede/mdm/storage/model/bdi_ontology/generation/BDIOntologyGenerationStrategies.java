package eu.supersede.mdm.storage.model.bdi_ontology.generation;

/**
 * Created by snadal on 18/01/17.
 */
public enum BDIOntologyGenerationStrategies {

    MANUAL("N/A Manual"),
    COPY_FROM_SOURCE("Copy from Source Levels"),
    PARIS("N/A PARIS - Probabilistic Alignment of Relations, Instances, and Schema");

    private String element;

    BDIOntologyGenerationStrategies(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }

}
