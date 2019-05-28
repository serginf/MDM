package eu.supersede.mdm.storage.util;

import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import eu.supersede.mdm.storage.model.Namespaces;
import org.apache.jena.query.*;
import org.apache.jena.update.UpdateAction;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by snadal on 24/11/16.
 */
public class RDFUtil {

    public static void addTriple(String namedGraph, String s, String p, String o) {
        //System.out.println("Adding triple: [namedGraph] "+namedGraph+", [s] "+s+", [p] "+p+", [o] "+o);
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        graph.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }

    public static void loadTTL(String namedGraph, String contentTTL){
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        graph.read(new ByteArrayInputStream(contentTTL.getBytes()), null,"TTL");
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }

    public static void deleteTriplesNamedGraph(String namedGraph){
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        graph.removeAll();
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }

    public static void addBatchOfTriples(String namedGraph, List<Tuple3<String,String,String>> triples) {
        //System.out.println("Adding triple: [namedGraph] "+namedGraph+", [s] "+s+", [p] "+p+", [o] "+o);
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        Model graph = ds.getNamedModel(namedGraph);
        for (Tuple3<String,String,String> t : triples) {
            graph.add(new ResourceImpl(t._1), new PropertyImpl(t._2), new ResourceImpl(t._3));
        }
        graph.commit();
        graph.close();
        ds.commit();
        ds.close();
    }


    public static ResultSet runAQuery(String sparqlQuery, String namedGraph) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), ds)) {
            ResultSetRewindable results = ResultSetFactory.copyResults(qExec.execSelect());
            qExec.close();
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void runAnUpdateQuery(String sparqlQuery) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.WRITE);
        try {
            UpdateAction.parseExecute(sparqlQuery,ds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ds.commit();
        ds.close();
    }


    public static ResultSet runAQuery(String sparqlQuery, Dataset ds) {
        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), ds)) {
            ResultSetRewindable results = ResultSetFactory.copyResults(qExec.execSelect());
            qExec.close();
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static ResultSet runAQuery(String sparqlQuery, InfModel o) {
        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), o)) {
            ResultSetRewindable results = ResultSetFactory.copyResults(qExec.execSelect());
            qExec.close();
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Short name
    public static String nn(String s) {
        return noNamespace(s);
    }

    public static String noNamespace(String s) {
        return s.replace(Namespaces.G.val(),"")
                .replace(Namespaces.S.val(),"")
                .replace(Namespaces.sup.val(),"")
                .replace(Namespaces.rdfs.val(),"")
                .replace(Namespaces.owl.val(),"");
    }

    public static String getRDFString (String namedGraph) {
        Dataset ds = Utils.getTDBDataset();
        ds.begin(ReadWrite.READ);
        Model graph = ds.getNamedModel(namedGraph);

        // Output RDF
        String tempFileForO = TempFiles.getTempFile();
        try {
            graph.write(new FileOutputStream(tempFileForO),"RDF/XML-ABBREV");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String content = "";
        try {
            content = new String(java.nio.file.Files.readAllBytes(new java.io.File(tempFileForO).toPath()));
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        graph.close();
        ds.close();

        return content;
    }

    public static String convertToURI(String name) {
        //If it is a semantic annotation, add the right URI
        if (name.equals("hasFeature")) {
            return GlobalGraph.HAS_FEATURE.val();
        }
        else if (name.equals("subClass") || name.equals("subClassOf")) {
            return Namespaces.rdfs.val()+"subClassOf";
        }
        else if (name.equals("ID") || name.equals("identifier")) {
            return Namespaces.sc.val() + "identifier";
        }

        //Otherwise, just add the SUPERSEDE one
        return Namespaces.sup.val()+name;
    }

}