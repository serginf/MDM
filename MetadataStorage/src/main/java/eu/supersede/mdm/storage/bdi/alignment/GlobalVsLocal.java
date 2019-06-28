package eu.supersede.mdm.storage.bdi.alignment;

import eu.supersede.mdm.storage.bdi.extraction.Namespaces;
import eu.supersede.mdm.storage.resources.bdi.SchemaIntegrationHelper;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Tuple2;
import eu.supersede.mdm.storage.util.Tuple3;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Kashif-Rabbani in June 2019
 */
public class GlobalVsLocal {
    private JSONObject dataSource1Info = new JSONObject();
    private JSONObject dataSource2Info = new JSONObject();
    private JSONArray alignmentsArray = new JSONArray();
    private final SchemaIntegrationHelper schemaIntegrationHelper = new SchemaIntegrationHelper();

    public GlobalVsLocal(JSONObject a, JSONObject b) {
        this.dataSource1Info = a;
        this.dataSource2Info = b;
    }

    public JSONArray runGlobalVsLocalIntegration() {
        /* Create an IRI for alignments which will be produced by LogMap for the two sources. Note that this IRI is required to store the alignments in the TripleStore. */
        String alignmentsIRI = Namespaces.Alignments.val() + dataSource1Info.getAsString("dataSourceID") + "-" + dataSource2Info.getAsString("dataSourceID");

        // Calling LogMapMatcher class to extract, and save the alignments
        LogMapMatcher logMapMatcher = new LogMapMatcher(dataSource1Info.getAsString("parsedFileAddress"), dataSource2Info.getAsString("parsedFileAddress"), alignmentsIRI);
        /*
        *  This array is storing the structure like this:
        * {
          "SUPERCLASSES": [
            [
              {
                "s": "global Super Class",
                "p": "local",
                "o": "conf"
              },
              {
                "s": "global Sub Class 1",
                "p": "local",
                "o": "conf"
              },
              {
                "s": "global Sub Class 2",
                "p": "local",
                "o": "conf"
              }
            ],[], []
          ]
        }*/
        JSONArray superAndSubClassesArray = new JSONArray();
        JSONArray onlyClassesArray = new JSONArray();
        refactorSuperClassesAndSubClasses(logMapMatcher, superAndSubClassesArray, onlyClassesArray);

        //System.out.println(superAndSubClassesArray.toJSONString());
        //System.out.println(onlyClassesArray.toJSONString());

        RDFUtil.runAQuery("SELECT * WHERE { GRAPH <" + alignmentsIRI + "> {?s ?p ?o} }", alignmentsIRI).forEachRemaining(triple -> {
            JSONObject alignments = new JSONObject();
            if (!triple.get("o").toString().split("__")[1].equals("CLASS")) {
                schemaIntegrationHelper.populateResponseArray(alignmentsArray, triple, alignments);
            }
        });

        Finder finder = new Finder(dataSource1Info.getAsString("schema_iri"), dataSource2Info.getAsString("schema_iri"));
        JSONArray dataPropertiesSpecialAlignments = finder.getAlignmentsArray();
        alignmentsArray.addAll(dataPropertiesSpecialAlignments);

        JSONObject o = new JSONObject();
        o.put("mapping_type", "CLASS");
        o.put("super_classes", superAndSubClassesArray);
        o.put("other_classes", onlyClassesArray);

        alignmentsArray.add(o);
        return alignmentsArray;
    }

    private void refactorSuperClassesAndSubClasses(LogMapMatcher logMapMatcher, JSONArray superAndSubClassesArray, JSONArray onlyClassesArray) {
        String iri = dataSource1Info.getAsString("schema_iri");

        List<Tuple3<String, String, String>> classAlignments = logMapMatcher.getClassesAlignments();

        Map<String, List<Tuple3<String, String, String>>> classAlignmentsGroupedByClassB = classAlignments.stream().collect(Collectors.groupingBy(w -> w._2));
        //System.out.println(classAlignmentsGroupedByClassB.values());

        /* classAlignmentsGroupedByClassB looks like this:
         * [groupedByClass, List[ Tuple3<classA, groupedByClass, Confidence>, Tuple3<,,>,....] ] */

        // Iterate over list Of Class Pairs grouped by Class B
        classAlignmentsGroupedByClassB.forEach((groupedClass, listOfClassPairs) -> {
            //System.out.println("Grouped Class: ------------> " + groupedClass);
            // This map contains the information about the super classes (IF EXISTS) and their sub classes (Result of Query)
            // Map<Tuple2<ClassIRI, ConfidenceValue>, List<AllSubClassesOfClass>>
            Map<Tuple2<String, String>, List<String>> superClassesPlusSubClasses = new HashMap<>();

            // This list contains the classes which are not super classes
            List<String> classes = new ArrayList<>();

            // This list is to store the classes (which are not super classes) with their confidence
            List<Tuple2> classesAndConfidence = new ArrayList<>();

            // Iterate over list of Class Pairs to separate the information about super classes and others
            for (Tuple3 classPair : listOfClassPairs) {
                // If the class A contains a global IRI, it means it is a super class (According to our GG Model Representation)
                if (classPair._1.toString().contains(Namespaces.G.val())) {
                    // Query to get the subClasses of the superClass from the global graph
                    String query = " SELECT DISTINCT ?p WHERE { GRAPH <" + iri + "> { ?p rdfs:subClassOf <" + classPair._1.toString() + "> . } }";
                    List<String> subClasses = schemaIntegrationHelper.getSparqlQueryResult(iri, query);
                    // Saving the SuperClasses with their Sub Classes and Confidence value in the Map
                    superClassesPlusSubClasses.put(new Tuple2(classPair._1.toString(), classPair._3.toString()), subClasses);
                } else {
                    // If the class A does not contain the Global IRI, it means it is a class with Namespaces.Schema.val() IRI, so it can not be a super class (According to our GG Model Representation)
                    // Add the classA into a separate List
                    classes.add(classPair._1.toString());
                    // Add the class into a list including its confidence value
                    classesAndConfidence.add(new Tuple2(classPair._1.toString(), classPair._3.toString()));
                }
            }
            // Iterate over all the superClasses having their subClasses (IF EXISTS)
            if (superClassesPlusSubClasses.size() > 0) {
                populateSuperSubClasses(superAndSubClassesArray, onlyClassesArray, groupedClass, superClassesPlusSubClasses, classes, classesAndConfidence);
            } else {
                //Iterate over the list of ClassesAndConfidence (These are the classes which are not superClasses)
                for (Tuple2 tuple : classesAndConfidence) {
                    JSONObject temp = new JSONObject();
                    temp.put("s", tuple._1);
                    temp.put("p", groupedClass);
                    temp.put("confidence", tuple._2);
                    onlyClassesArray.add(temp);
                }
            }
        });
    }

    private void populateSuperSubClasses(JSONArray superAndSubClassesArray, JSONArray onlyClassesArray, String groupedClass, Map<Tuple2<String, String>, List<String>> superClassesPlusSubClasses, List<String> classes, List<Tuple2> classesAndConfidence) {
        superClassesPlusSubClasses.forEach((superClass, subClasses) -> {
            //System.out.println("********* Printing Super and Sub Classes: ");
            //System.out.println(superClass._1);
            //System.out.println(subClasses.toString());
            //System.out.println();
            // Creating a wrapper array to wrap the information about this super class
            JSONArray wrapperArray = new JSONArray();
            // Creating an object to store the ClassA, GroupedByClass i.e. class B, and the Confidence Value. Note that we know that this is a superClass Info only.
            JSONObject containerObject = new JSONObject();
            containerObject.put("s", superClass._1);
            containerObject.put("p", groupedClass);
            containerObject.put("confidence", superClass._2);
            // Adding the object into wrapper Array
            wrapperArray.add(containerObject);

            // Filter out those classes which are same as subclasses of the superclass we are iterating
            List<String> classesSameAsSubClasses = classes.stream().filter(subClasses::contains).collect(Collectors.toList());
            // Filter out those classes which are not same as subclasses of the superclass we are iterating
            List<String> classesDifferentFromSubClasses = classes.stream().filter(obj -> !subClasses.contains(obj)).collect(Collectors.toList());
            //System.out.println("classesSameAsSubClasses: ");
            //System.out.println(classesSameAsSubClasses);

            //System.out.println("classesDifferentFromSubClasses: ");
            //System.out.println(classesDifferentFromSubClasses);

            //Iterate over the list of ClassesAndConfidence (These are the classes which are not superClasses)
            for (Tuple2 tuple : classesAndConfidence) {
                // Extracting the confidence value for the classes which are same as subclasses
                constructTripleResponse(groupedClass, wrapperArray, classesSameAsSubClasses, tuple);
                // Extracting the confidence value for the classes which are not same as subclasses
                constructTripleResponse(groupedClass, onlyClassesArray, classesDifferentFromSubClasses, tuple);
            }
            superAndSubClassesArray.add(wrapperArray);
        });
    }

    private void constructTripleResponse(String groupedClass, JSONArray jsonArray, List<String> listOfClasses, Tuple2 tuple) {
        if (listOfClasses.contains(tuple._1)) {
            JSONObject temp = new JSONObject();
            String tupleValue = listOfClasses.get(listOfClasses.indexOf(tuple._1));
            temp.put("s", tupleValue);
            temp.put("p", groupedClass);
            temp.put("confidence", tuple._2);
            jsonArray.add(temp);
        }
    }
}
