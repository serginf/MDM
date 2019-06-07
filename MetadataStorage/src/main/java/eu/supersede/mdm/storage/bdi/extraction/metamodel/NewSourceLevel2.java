package eu.supersede.mdm.storage.bdi.extraction.metamodel;

import eu.supersede.mdm.storage.bdi.extraction.NewNamespaces2;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import java.util.Arrays;

public enum NewSourceLevel2 {
	//OBJECT
	CProperty(NewNamespaces2.source.val() + "has"),
	RDFProperty(NewNamespaces2.rdf.val()+"Property"),//NewNamespaces2.rdf.val() + 
	DATA_TYPE(NewNamespaces2.source.val() + "Datatype"),
	STRING(NewNamespaces2.xsdd.val() + "string"),
	LITERAL(NewNamespaces2.source.val() + "Literal"),
	NUMBER(NewNamespaces2.xsdd.val() + "integer"),
	Date(NewNamespaces2.xsdd.val() + "date"),
	Boolean(NewNamespaces2.xsdd.val() + "boolean"),	
	Decimal(NewNamespaces2.xsdd.val() + "decimal"),
	RDFSContainer(NewNamespaces2.rdfs.val() + "Container"),
	RDFSeq(NewNamespaces2.rdf.val() + "Seq"),
	RDFBag(NewNamespaces2.rdf.val() + "Bag"),
	RDFSCMP(NewNamespaces2.rdfs.val() + "ContainerMembershipProperty"),
	RDFSClass(NewNamespaces2.rdfs.val() + "Class"),
	
	RDFSResource(NewNamespaces2.source.val() + "RDFSResource"),

	//PREDICATES
	
	//HAS_cProperty(NewNamespaces2.source.val() + "has" +),
	HAS_Property(NewNamespaces2.rdfs.val() + "hasProperty"),
	RDFSSubClassOf(NewNamespaces2.rdfs.val() + "RDFSSubClassOf"),
	RDFSMember(NewNamespaces2.rdfs.val() + "RDFSMember"),
	HAS_Member(NewNamespaces2.rdfs.val() + "hasMember"),
	RDFSRange(NewNamespaces2.rdfs.val() + "range"),
	RDFSDomain(NewNamespaces2.rdfs.val() + "domain"),

	//RDF
	TYPE(NewNamespaces2.rdf.val() + "type"),

	//SCHEMA
	ROOT(NewNamespaces2.schema.val());

	private String element;

	NewSourceLevel2(String element) {
		this.element = element;
	}

	public String val() {
		return element;
	}

	public String generateIdentifier() {
		//Note: must match regex in #getIdentifierRegex
		return String.join("-", Arrays.asList(this.toString(), RandomStringUtils.randomAlphanumeric(10).toLowerCase()));
	}

	public String getIdentifierRegex() {
		//Note: must match ranadom characters generated in #generateIdentifier()
		return String.join("-", Arrays.asList(this.toString(), "[a-z0-9]{10}"));
	}

	public ObjectProperty asObjectProperty(OntModel model) {
		return model.getObjectProperty(this.val());
	}

	public OntClass asOntClass(OntModel model) {
		return model.getOntClass(this.val());
	}
}