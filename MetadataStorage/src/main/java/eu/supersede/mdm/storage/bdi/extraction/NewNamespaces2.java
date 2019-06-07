package eu.supersede.mdm.storage.bdi.extraction;

public enum NewNamespaces2 {
	source("http://www.BDIOntology.com/source#"),
	schema("http://www.BDIOntology.com/schema"),
	rdf("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
	xsdd("http://www.w3.org/2001/XMLSchema#"),
	rdfs("http://www.w3.org/2000/01/rdf-schema#");

	private String element;

	NewNamespaces2(String element) {
		this.element = element;
	}

	public String val() {
		return element;
	}
}
