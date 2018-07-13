package eu.supersede.mdm.storage.tests;

import eu.supersede.mdm.storage.ApacheMain;
import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.model.omq.QueryRewriting;
import eu.supersede.mdm.storage.util.RDFUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ER_2018_Tests_old {

    public static void deleteTDB() {
        try {
            FileUtils.deleteDirectory(new File("/home/snadal/UPC/Projects/MDM/MetadataStorage/MDM_TDBMDM_TDB"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void populateOntology() {
        /*
        Global Level
        */
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"InfoMonitorL0", Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"InfoMonitorL1", Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"InfoMonitorL2", Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());

        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"FeedbackL0", Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"FeedbackL1", Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"FeedbackL2", Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());

        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"Second", Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"Minute", Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"Hour", Namespaces.rdf.val()+"type", GlobalGraph.CONCEPT.val());

        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"lagRatio", Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val());
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"text", Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val());
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"hourId", Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val());
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"minuteId", Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val());
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"secondId", Namespaces.rdf.val()+"type", GlobalGraph.FEATURE.val());

        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"sum", Namespaces.rdf.val()+"type", GlobalGraph.AGGREGATION_FUNCTION.val());
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"count", Namespaces.rdf.val()+"type", GlobalGraph.AGGREGATION_FUNCTION.val());

        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"InfoMonitorL0", GlobalGraph.HAS_FEATURE.val(), Namespaces.sup.val()+"lagRatio");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"InfoMonitorL1", GlobalGraph.HAS_FEATURE.val(), Namespaces.sup.val()+"lagRatio");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"InfoMonitorL2", GlobalGraph.HAS_FEATURE.val(), Namespaces.sup.val()+"lagRatio");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"lagRatio", GlobalGraph.HAS_AGGREGATION_FUNCTION.val(), Namespaces.sup.val()+"sum");

        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"FeedbackL0", GlobalGraph.HAS_FEATURE.val(), Namespaces.sup.val()+"text");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"FeedbackL1", GlobalGraph.HAS_FEATURE.val(), Namespaces.sup.val()+"text");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"FeedbackL2", GlobalGraph.HAS_FEATURE.val(), Namespaces.sup.val()+"text");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"text", GlobalGraph.HAS_AGGREGATION_FUNCTION.val(), Namespaces.sup.val()+"count");

        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"Second", GlobalGraph.HAS_FEATURE.val(), Namespaces.sup.val()+"secondId");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"Minute", GlobalGraph.HAS_FEATURE.val(), Namespaces.sup.val()+"minuteId");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"Hour", GlobalGraph.HAS_FEATURE.val(), Namespaces.sup.val()+"hourId");

        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"InfoMonitorL0", Namespaces.sup.val()+"hasTime", Namespaces.sup.val()+"Second");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"InfoMonitorL1", Namespaces.sup.val()+"hasTime", Namespaces.sup.val()+"Minute");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"InfoMonitorL2", Namespaces.sup.val()+"hasTime", Namespaces.sup.val()+"Hour");

        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"InfoMonitorL0", GlobalGraph.PART_OF.val(), Namespaces.sup.val()+"InfoMonitorL1");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"InfoMonitorL1", GlobalGraph.PART_OF.val(), Namespaces.sup.val()+"InfoMonitorL2");

        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"FeedbackL0", GlobalGraph.PART_OF.val(), Namespaces.sup.val()+"FeedbackL1");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"FeedbackL1", GlobalGraph.PART_OF.val(), Namespaces.sup.val()+"FeedbackL2");

        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"Second", GlobalGraph.PART_OF.val(), Namespaces.sup.val()+"Minute");
        RDFUtil.addTriple("http://www.essi.upc.edu/~snadal/ER_ontology",Namespaces.sup.val()+"Minute", GlobalGraph.PART_OF.val(), Namespaces.sup.val()+"Hour");
    }

    public static void main(String[] args) {
        ApacheMain.configPath = "/home/snadal/UPC/Projects/MDM/MetadataStorage/config.sergi.properties";

        populateOntology();


        String SPARQL =
                "SELECT ?l ?t " +
                //"FROM <http://www.essi.upc.edu/~snadal/ER_ontology> "+
                "WHERE { " +
                   "VALUES (?l ?t) {( "+"<"+Namespaces.sup.val()+"lagRatio"+">"+" "+"<"+Namespaces.sup.val()+"text"+">"+" )} " +
                   "<"+Namespaces.sup.val()+"InfoMonitorL2"+">"+" "+"<"+GlobalGraph.HAS_FEATURE.val()+">"+" "+"<"+Namespaces.sup.val()+"lagRatio"+">"+" . "+
                   "<"+Namespaces.sup.val()+"InfoMonitorL2"+">"+" "+"<"+Namespaces.sup.val()+"hasTime"+">"+" "+"<"+Namespaces.sup.val()+"Hour"+">"+" . "+
                   "<"+Namespaces.sup.val()+"FeedbackL2"+">"+" "+"<"+GlobalGraph.HAS_FEATURE.val()+">"+" "+"<"+Namespaces.sup.val()+"text"+">"+" . "+
                   "<"+Namespaces.sup.val()+"FeedbackL2"+">"+" "+"<"+Namespaces.sup.val()+"hasTime"+">"+" "+"<"+Namespaces.sup.val()+"Hour"+">"+" . "+
                   "<"+Namespaces.sup.val()+"Hour"+">"+" "+"<"+GlobalGraph.HAS_FEATURE.val()+">"+" "+"<"+Namespaces.sup.val()+"hourId"+">"+
                "}";
        System.out.println(SPARQL);

        QueryRewriting qr = new QueryRewriting(SPARQL);
        //qr.aggregationStep();

        deleteTDB();
    }

}
