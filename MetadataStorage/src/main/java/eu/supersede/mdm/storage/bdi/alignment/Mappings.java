package eu.supersede.mdm.storage.bdi.alignment;

import eu.supersede.mdm.storage.bdi.extraction.Namespaces;
import eu.supersede.mdm.storage.resources.bdi.SchemaIntegrationHelper;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.util.ResourceUtils;
import org.semarglproject.vocab.RDF;

import java.util.Collection;
import java.util.HashSet;

public class Mappings {
    private JSONObject basicInfo;
    private SchemaIntegrationHelper schemaIntegrationHelper = new SchemaIntegrationHelper();
    private JSONObject dataSource1Info = new JSONObject();
    private JSONObject dataSource2Info = new JSONObject();


    public Mappings(JSONObject objBody) {
        this.basicInfo = objBody;
        getSourcesInfo();
        toGlobalURI(basicInfo.getAsString("integratedIRI"));
        //createSameAsMappings();
    }

    private void createSameAsMappings() {
        dataSource1Info.getAsString("iri");
        dataSource2Info.getAsString("iri");
        basicInfo.getAsString("integratedIRI");

        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);

        Model globalGraph = ds.getNamedModel(basicInfo.getAsString("integratedIRI"));
        Model localGraphA = ds.getNamedModel(dataSource1Info.getAsString("iri"));
        Model localGraphB = ds.getNamedModel(dataSource2Info.getAsString("iri"));

        StmtIterator globalGraphIterator = globalGraph.listStatements();

        System.out.println("________________________________________________________________________________");
        try {
            while (globalGraphIterator.hasNext()) {
                Statement globalGraphStatement = globalGraphIterator.next();

                StmtIterator localGraphAIterator = localGraphA.listStatements();
                StmtIterator localGraphBIterator = localGraphB.listStatements();

                System.out.println();
                System.out.print(" GG " + globalGraphStatement.getSubject().getURI());
                System.out.print(" --- " + globalGraphStatement.getPredicate().getURI());
                System.out.print(" --- " + globalGraphStatement.getObject().toString());
                System.out.println();


                String globalURN = "";
                if (globalGraphStatement.getSubject().getURI().toLowerCase().contains(Namespaces.G.val().toLowerCase())) {
                    globalURN = globalGraphStatement.getSubject().getURI().split(Namespaces.G.val())[1];
                    //System.out.print(globalURN + " ---- ");
                }


                while (localGraphAIterator.hasNext()) {
                    Statement localGraphStatement = localGraphAIterator.next();

                    String localURN = "";

                    if (localGraphStatement.getSubject().getURI().toLowerCase().contains(Namespaces.Schema.val().toLowerCase())) {
                        localURN = localGraphStatement.getSubject().getURI().split(Namespaces.Schema.val())[1];
                        //System.out.print(localURN);
                    }

                    if (globalURN.equals(localURN)) {
                        if (localGraphStatement.getPredicate().equals(new ResourceImpl(RDF.TYPE))) {
                            System.out.print(" A " + localGraphStatement.getSubject().getURI());
                            System.out.print(" --- " + localGraphStatement.getPredicate().getURI());
                            System.out.print(" --- " + localGraphStatement.getObject().toString());
                            System.out.println();
                        }
                    }
                }

                while (localGraphBIterator.hasNext()) {
                    Statement localGraphStatement = localGraphBIterator.next();

                    String localURN = "";
                    if (localGraphStatement.getSubject().getURI().toLowerCase().contains(Namespaces.Schema.val().toLowerCase())) {
                        localURN = localGraphStatement.getSubject().getURI().split(Namespaces.Schema.val())[1];
                        //System.out.print(localURN);
                    }

                    if (globalURN.equals(localURN)) {
                        if (localGraphStatement.getPredicate().equals(new ResourceImpl(RDF.TYPE))) {
                            System.out.print(" B " + localGraphStatement.getSubject().getURI());
                            System.out.print(" --- " + localGraphStatement.getPredicate().getURI());
                            System.out.print(" --- " + localGraphStatement.getObject().toString());
                            System.out.println();
                        }
                    }
                }
            }
        } finally {
            if (globalGraphIterator != null) globalGraphIterator.close();
        }
        globalGraph.commit();
        globalGraph.close();
        ds.commit();
        ds.close();

    }

    private void toGlobalURI(String namedModel) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);

        Model graph = ds.getNamedModel(namedModel);
        Collection<String> sa = new HashSet();
        ResIterator iter = graph.listSubjects();

        while (iter.hasNext()) {
            String uri = iter.next().getURI();
            sa.add(uri);
            System.out.println(uri);
        }

        for (String s : sa) {
            String sNew = getNewIRI(s);
            if (sNew == null) {
                continue;
            }
            System.out.println("sNew :" + sNew);
            ResourceUtils.renameResource(graph.getResource(s), sNew);
        }
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }

    private String getNewIRI(String s) {
        String ret = "";
        if (s.toLowerCase().contains(Namespaces.Schema.val().toLowerCase())) {
            String urn = s.split(Namespaces.Schema.val())[1];
            //String uri = s.split(Namespaces.Schema.val())[0];
            ret = Namespaces.G.val() + urn;
        } else if (s.toLowerCase().contains(Namespaces.G.val().toLowerCase())) {
            String urn = s.split(Namespaces.G.val())[1];
            //String uri = s.split(Namespaces.Schema.val())[0];
            ret = Namespaces.G.val() + urn;
        }
        return ret;
    }

    private void getSourcesInfo() {
        String dataSource1;
        if (basicInfo.getAsString("ds1_id").contains("INTEGRATED-")) {
            dataSource1 = schemaIntegrationHelper.getIntegratedDataSourceInfo(basicInfo.getAsString("ds1_id"));
        } else {
            dataSource1 = schemaIntegrationHelper.getDataSourceInfo(basicInfo.getAsString("ds1_id"));
        }

        String dataSource2 = schemaIntegrationHelper.getDataSourceInfo(basicInfo.getAsString("ds2_id"));

        if (!dataSource1.isEmpty())
            dataSource1Info = (JSONObject) JSONValue.parse(dataSource1);

        if (!dataSource2.isEmpty())
            dataSource2Info = (JSONObject) JSONValue.parse(dataSource2);
    }
}
