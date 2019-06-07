package eu.supersede.mdm.storage.bdi.alignment;

import eu.supersede.mdm.storage.bdi.extraction.Namespaces;
import eu.supersede.mdm.storage.resources.bdi.SchemaIntegrationHelper;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.SQLiteUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.HashMap;

public class AlignmentAlgorithm {
    private JSONObject basicInfo;

    public AlignmentAlgorithm(JSONObject obj) {
        this.basicInfo = obj;
        alignClasses();
        alignProperties();
    }

    private void alignProperties() {
        JSONArray propertiesData = SQLiteUtils.executeSelect("SELECT * FROM Property", SchemaIntegrationHelper.getPropertyTableFeatures());
        propertiesData.forEach(node -> {
            HashMap<String, String> data = new HashMap<>();

            Object[] row = ((JSONArray) node).toArray();
            for (Object element : row) {
                JSONObject obj = (JSONObject) element;
                data.put(obj.getAsString("feature"), obj.getAsString("value"));
            }

            switch (data.get("actionType")) {
                case "ACCEPTED":
                    switch (data.get("AlignmentType")) {
                        case "OBJECT-PROPERTY":
                            System.out.println("OBJECT-PROPERTY");
                            //TODO Handle the Object Property
                            RDFUtil.addCustomTriple(basicInfo.getAsString("integratedIRI"), data.get("PropertyA"), "EQUIVALENT_PROPERTY", data.get("PropertyB"));
                            break;
                        case "DATA-PROPERTY":
                            System.out.println("DATA-PROPERTY");
                            String query = "SELECT * FROM Class WHERE classA = '" + data.get("DomainPropA") + "' and classB = '" + data.get("DomainPropB") + "'";
                            System.out.println(query);
                            JSONArray result = SQLiteUtils.executeSelect(query, SchemaIntegrationHelper.getClassTableFeatures());

                            //TODO Case 1 -  When classes of the properties are aligned
                            if (result.size() > 0) {
                                System.out.println("CLASSES PRESENT");


                                // Remove Properties from aligned Classes
                                RDFUtil.removeProperty(basicInfo.getAsString("integratedIRI"), data.get("PropertyA"), data.get("DomainPropA"), data.get("RangePropA"));
                                RDFUtil.removeProperty(basicInfo.getAsString("integratedIRI"), data.get("PropertyB"), data.get("DomainPropB"), data.get("RangePropB"));


                                //Move the Properties to the Parent class
                                String newGlobalProperty = basicInfo.getAsString("integratedIRI") + "/" + ResourceFactory.createResource(data.get("PropertyA")).getLocalName();
                                String newPropertyDomain = basicInfo.getAsString("integratedIRI") + "/" + ResourceFactory.createResource(data.get("DomainPropA")).getLocalName(); //+ "_" + ResourceFactory.createResource(data.get("DomainPropB")).getLocalName();

                                RDFUtil.addProperty(basicInfo.getAsString("integratedIRI"), newGlobalProperty, newPropertyDomain, data.get("RangePropA"));

                                // Handle SameAs
                                RDFUtil.addCustomTriple(basicInfo.getAsString("integratedIRI"), data.get("PropertyA"), "EQUIVALENT_PROPERTY", newGlobalProperty);
                                RDFUtil.addCustomTriple(basicInfo.getAsString("integratedIRI"), data.get("PropertyB"), "EQUIVALENT_PROPERTY", newGlobalProperty);



                            } else if(data.get("PropertyB").contains(Namespaces.G.val())){ // PropertyB is the one with global IRI, coming from integrated global graph
                                // Add domain of PropertyA as domain of PropertyB
                                RDFUtil.addPropertyDomain(basicInfo.getAsString("integratedIRI"), data.get("PropertyB"), data.get("DomainPropA"));
                                //Remove PropertyA
                                RDFUtil.removeProperty(basicInfo.getAsString("integratedIRI"), data.get("PropertyA"), data.get("DomainPropA"), data.get("RangePropA"));

                                //Create sameAs edge to the PropertyA from PropertyB
                                RDFUtil.addCustomTriple(basicInfo.getAsString("integratedIRI"), data.get("PropertyB"), "EQUIVALENT_PROPERTY", data.get("PropertyA"));
                            }
                            else {
                                //TODO Case 2 -  When classes of the properties are not aligned
                                String newGlobalGraphProperty = basicInfo.getAsString("integratedIRI") + "/" + ResourceFactory.createResource(data.get("PropertyA")).getLocalName();

                                RDFUtil.removeProperty(basicInfo.getAsString("integratedIRI"), data.get("PropertyA"), data.get("DomainPropA"), data.get("RangePropA"));
                                RDFUtil.removeProperty(basicInfo.getAsString("integratedIRI"), data.get("PropertyB"), data.get("DomainPropB"), data.get("RangePropB"));

                                String[] domainsForNewGlobalPropertyResource = {data.get("DomainPropA"), data.get("DomainPropB")};

                                RDFUtil.addProperty(basicInfo.getAsString("integratedIRI"), newGlobalGraphProperty, domainsForNewGlobalPropertyResource, data.get("RangePropA"));


                                // Handle SameAs
                                RDFUtil.addCustomTriple(basicInfo.getAsString("integratedIRI"), newGlobalGraphProperty, "EQUIVALENT_PROPERTY", data.get("PropertyA"));
                                RDFUtil.addCustomTriple(basicInfo.getAsString("integratedIRI"), newGlobalGraphProperty, "EQUIVALENT_PROPERTY", data.get("PropertyB"));
                            }

                            break;
                    }
                    break;
                case "REJECTED":
                    break;
            }
        });
    }


    private void alignClasses() {
        JSONArray classesData = SQLiteUtils.executeSelect("SELECT * FROM Class", SchemaIntegrationHelper.getClassTableFeatures());
        classesData.forEach(node -> {
            Object[] row = ((JSONArray) node).toArray();
            HashMap<String, String> classRow = new HashMap<>();
            for (Object element : row) {
                JSONObject obj = (JSONObject) element;
                classRow.put(obj.getAsString("feature"), obj.getAsString("value"));
            }

            switch (classRow.get("actionType")) {
                case "ACCEPTED":
                    switch (classRow.get("classType")) {
                        case "SUPERCLASS":
                            RDFUtil.addCustomTriple(basicInfo.getAsString("integratedIRI"), classRow.get("classA"), "SUB_CLASS_OF", classRow.get("classB"));
                            break;
                        case "LOCALCLASS":
                            Resource classA = ResourceFactory.createResource(classRow.get("classA"));
                            //Resource classB = ResourceFactory.createResource(classRow.get("classB"));

                            //if (basicInfo.getAsString("integrationType").equals("LOCAL-vs-LOCAL")) {
                            //newGlobalGraphClassResource = integratedIRI + "/" + classA.getURI().split(Namespaces.Schema.val())[1];
                            String newGlobalGraphClassResource = basicInfo.getAsString("integratedIRI") + "/" + classA.getLocalName();
                            // }

                            System.out.println("GG Resource: " + newGlobalGraphClassResource);

                            RDFUtil.addClassOrPropertyTriple(basicInfo.getAsString("integratedIRI"), newGlobalGraphClassResource, "CLASS");
                            RDFUtil.addCustomTriple(basicInfo.getAsString("integratedIRI"), classRow.get("classA"), "SUB_CLASS_OF", newGlobalGraphClassResource);
                            RDFUtil.addCustomTriple(basicInfo.getAsString("integratedIRI"), classRow.get("classB"), "SUB_CLASS_OF", newGlobalGraphClassResource);
                            break;
                    }
                    break;
                case "REJECTED":
                    //TODO:
                    break;
            }


        });
    }
}
