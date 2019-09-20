package eu.supersede.mdm.storage.bdi.extraction;

/**
 * Created by snadal on 22/11/16.
 */
public enum Namespaces {
    T("http://www.BDIOntology.com/BDIOntology/"),
    S("http://www.BDIOntology.com/source/"),
    Schema("http://www.BDIOntology.com/schema/"),
    Alignments("http://www.BDIOntology.com/alignments/"),
    G("http://www.BDIOntology.com/global/"),
    EGG("http://www.BDIOntology.com/extendedGlobal/"),
    M("http://www.BDIOntology.com/mappings/"),
    R("http://www.BDIOntology.com/eca_rule/"),
    I("http://www.BDIOntology.com/integratedSchema/"),
    E("http://www.essi.upc.edu/~snadal/Event/"),
    BASE("http://www.BDIOntology.com/"),

    owl("http://www.w3.org/2002/07/owl#"),
    rdf("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
    rdfs("http://www.w3.org/2000/01/rdf-schema#"),
    dct("http://purl.org/dc/terms/"),
    ex("http://www.example.com/"),
    dcat("http://www.w3.org/ns/dcat#"),
    sc("http://schema.org/"),
    duv("http://www.w3.org/ns/duv#"),

    sup("http://www.supersede.eu/");

    private String element;

    Namespaces(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }

}


/**
 * Created by snadal on 22/11/16.
 */
/*
public enum Namespaces {

    T("http://www.essi.upc.edu/~snadal/BDIOntology/"),
    S("http://www.essi.upc.edu/~snadal/BDIOntology/Source/"),
    G("http://www.essi.upc.edu/~snadal/BDIOntology/Global/"),
    M("http://www.essi.upc.edu/~snadal/BDIOntology/Mappings/"),
    R("http://www.essi.upc.edu/~jvarga/sm4cep/"),
    E("http://www.essi.upc.edu/~snadal/Event/"),

    owl("http://www.w3.org/2002/07/owl#"),
    rdf("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
    rdfs("http://www.w3.org/2000/01/rdf-schema#"),
    dct("http://purl.org/dc/terms/"),
    ex("http://www.example.com/"),
    dcat("http://www.w3.org/ns/dcat#"),
    sc("http://schema.org/"),
    duv("http://www.w3.org/ns/duv#"),

    sup("http://www.supersede.eu/");

    private String element;

    Namespaces(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }
}*/
