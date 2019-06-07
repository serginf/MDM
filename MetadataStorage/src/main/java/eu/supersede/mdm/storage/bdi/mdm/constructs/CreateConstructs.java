package eu.supersede.mdm.storage.bdi.mdm.constructs;

import eu.supersede.mdm.storage.bdi.extraction.Namespaces;
import eu.supersede.mdm.storage.resources.bdi.SchemaIntegrationHelper;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.HttpUtils;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.semarglproject.vocab.RDF;

public class CreateConstructs {
    private JSONObject wrapper = new JSONObject();
    private JSONObject globalGraphInfo = new JSONObject();
    private String postWrapperUrl = ConfigManager.getProperty("metadata_data_storage_url") + "wrapper/";

    public CreateConstructs(String id) {
        SchemaIntegrationHelper schemaIntegrationHelper = new SchemaIntegrationHelper();
        String initGlobalGraphInfo = schemaIntegrationHelper.getIntegratedDataSourceInfo(id);
        if (!initGlobalGraphInfo.isEmpty()) {
            globalGraphInfo = (JSONObject) JSONValue.parse(initGlobalGraphInfo);
        }
        constructGlobalGraph();
        //prepareWrapperContent();
    }

    /**
     * This method is intended to transform the Integrated Global Graph into MDM Global Graph i.e. Concepts, features etc...
     */
    private void constructGlobalGraph(){
        String namedGraph = "http://www.essi.upc.edu/~snadal/SportsUML/f87da8e05246447fa8d245ce1fd86518";
        Dataset ds = Utils.getTDBDataset();
        System.out.println("IS IT TRUE: " + ds.containsNamedModel(namedGraph));
        ds.begin(ReadWrite.READ);
        Model graph = ds.getNamedModel(namedGraph);
        System.out.println(graph.size());
        StmtIterator graphIterator = graph.listStatements();
        try {
            while (graphIterator.hasNext()) {
                Statement graphStatement = graphIterator.next();

                    System.out.print(" Subject " + graphStatement.getSubject().getURI());
                    System.out.print(" Predicate " + graphStatement.getPredicate().getURI());
                    System.out.print(" Object " + graphStatement.getObject().toString());

            }
            System.out.println();
        } finally {
            if (graphIterator != null) graphIterator.close();
        }
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }
    private void prepareWrapperContent(){
        JSONArray dataSourcesArray = (JSONArray) globalGraphInfo.get("dataSources");
        for (Object o : dataSourcesArray) {
            JSONObject dataSource = (JSONObject) o;
            //System.out.println(dataSource.toJSONString());
            populateWrapperContent(dataSource);
            try {
                HttpUtils.sendPost(wrapper, postWrapperUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void populateWrapperContent(JSONObject dataSource) {
        String sourceIRI = Namespaces.Schema.val() + dataSource.getAsString("dataSourceName");
        wrapper.put("name", dataSource.getAsString("dataSourceName").replaceAll(" ", "") + "_Wrapper");
        wrapper.put("dataSourceID", dataSource.getAsString("dataSourceID"));
        JSONArray attributes = new JSONArray();

        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(sourceIRI);

        StmtIterator graphIterator = graph.listStatements();
        try {
            while (graphIterator.hasNext()) {
                Statement graphStatement = graphIterator.next();
                if (graphStatement.getObject().equals(new ResourceImpl(RDF.PROPERTY))) {
                    String name = graphStatement.getSubject().getLocalName();
                    JSONObject temp = new JSONObject();
                    temp.put("isID", "false");
                    temp.put("name", name);
                    temp.put("iri", graphStatement.getSubject().getURI());
                    //System.out.println(graphStatement.getSubject().getLocalName());
                    //System.out.print(" Subject " + graphStatement.getSubject().getURI());
                    //System.out.print(" Predicate " + graphStatement.getPredicate().getURI());
                    //System.out.print(" Object " + graphStatement.getObject().toString());
                    attributes.add(temp);
                }
            }
            wrapper.put("attributes", attributes);
            //System.out.println();
        } finally {
            if (graphIterator != null) graphIterator.close();
        }
        //System.out.println(wrapper.toJSONString());
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }
}
