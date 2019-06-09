package eu.supersede.mdm.storage.bdi.mdm.constructs;

import eu.supersede.mdm.storage.bdi.extraction.Namespaces;
import eu.supersede.mdm.storage.resources.WrapperResource;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.semarglproject.vocab.RDF;

public class MDMWrapper {
    private JSONObject globalGraphInfo;
    private JSONObject wrapper = new JSONObject();

    MDMWrapper(JSONObject ggInfo) {
        this.globalGraphInfo = ggInfo;
        createWrapper();
    }

    private void createWrapper() {
        JSONArray dataSourcesArray = (JSONArray) globalGraphInfo.get("dataSources");
        for (Object o : dataSourcesArray) {
            JSONObject dataSource = (JSONObject) o;
            //System.out.println(dataSource.toJSONString());
            populateWrapperContent(dataSource);
            try {
                JSONObject res = WrapperResource.createWrapper(wrapper.toJSONString());
                System.out.println(res.toJSONString());
                //HttpUtils.sendPost(wrapper, postWrapperUrl);
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
        System.out.println(wrapper.toJSONString());
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }
}
