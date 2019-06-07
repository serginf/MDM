package eu.supersede.mdm.storage.bdi.extraction;

import eu.supersede.mdm.storage.bdi.extraction.metamodel.NewSourceLevel2;
import eu.supersede.mdm.storage.util.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.vocabulary.RDF;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;


public class JsonSchemaExtractor {
    private static final String LANG = "TURTLE"; //"RDF/XML");//"N-TRIPLE");
    private static String outputFile = "";
    private static String IRI = "";


    private static OntModel model;

    public JsonSchemaExtractor() {
    }

    public JsonSchemaExtractor(String filePath) {
        try {
            File file = new File(filePath);
            String body = new String(Files.readAllBytes(Paths.get(file.toURI())));
            JsonSchemaExtractor.extract_schema(file.getName().split("\\.")[0], body);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public JSONObject initiateExtraction(String filePath, String rootName) {
        JSONObject res = null;
        try {
            File file = new File(filePath);
            String body = new String(Files.readAllBytes(Paths.get(file.toURI())));
            res = JsonSchemaExtractor.extract_schema(rootName, body);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    static {
        try {
            //Load the new metamodel
            model = ModelFactory.createOntologyModel();

            String path = ConfigManager.getProperty("resources_path") + "/" + "model/new_metadata_model.owl";
            model.read(new File(path).toURI().toURL().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject extract_schema(String filename, String body) throws IOException {
        JSONObject json = (JSONObject) JSONValue.parse(body);

        OntModel schemaModel = getOrCreateSchemaModel(filename);

        String root = NewSourceLevel2.ROOT.val() + "/" + filename;
        IRI = root;

        NewRDFUtil.addTriple(schemaModel, root, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass.asOntClass(model));

        jsonToSchemaNew(schemaModel, json, root);

        outputFile = TempFiles.getIncrementalTempFile(filename);
        schemaModel.write(new FileOutputStream(outputFile), LANG);

        String content = new String(Files.readAllBytes(new File(outputFile).toPath()));

        JSONObject response = new JSONObject();
        response.put("rdf", content);

        return response;
    }

    public static OntModel getOrCreateSchemaModel(String filename) throws MalformedURLException {
        File latestSchemaModelFile = TempFiles.getLatestFile(filename).map(longFilePair -> longFilePair.getValue()).orElse(null);

        OntModel schemaModel;
        if (latestSchemaModelFile != null) {
            schemaModel = ModelFactory.createOntologyModel();
            schemaModel.read(latestSchemaModelFile.toURI().toURL().toString(), LANG);
            return schemaModel;
        } else {
            schemaModel = ModelFactory.createOntologyModel();
            schemaModel.setNsPrefix(NewNamespaces2.rdfs.name(), NewNamespaces2.rdfs.val());
        }

        return schemaModel;
    }

    private static void jsonToSchemaNew(OntModel schemaModel, Object base, String parentElement) {
        if (JSONUtil.isObject(base)) {
            JSONObject jsonObject = (JSONObject) base;

            //String newName=parentElement+"/has_"+jsonObject.getAsString(key);
            //NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
            //NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.RDFSDomain, parentElement);
            //NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.RDFSRange.asObjectProperty(model), oKeyName);


            jsonObject.forEach((k, v) -> {
                String thisElement = parentElement + "/" + k;

                JSONAdapter jsonAdapter = new JSONAdapter(model, jsonObject, k);

                // If element is now an array but was previously an attribute,
                // remove the attribute rdf:type and hasAttribute from parent
                // because they will be replaced with rdf:type Array and hasCollection below.
                // Also remove any existing hasDataType (now that it has become an array, its old data type no longer applies)
                QuerySolution existingAttributeTriple = searchExistingTriple(schemaModel, thisElement, RDF.type.asNode(), NewSourceLevel2.RDFProperty.val());
                if (jsonAdapter.getOntClass().toString().equals(NewSourceLevel2.RDFSeq.val()) && existingAttributeTriple != null) {
                    NewRDFUtil.removeTriple(schemaModel, thisElement, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty.val());
                    NewRDFUtil.removeTriple(schemaModel, parentElement, NewSourceLevel2.HAS_Property, thisElement);
                    NewRDFUtil.removeTriple(schemaModel, thisElement, NewSourceLevel2.RDFSRange);


                }//to test rdfseq

                //RDFSCMP
                QuerySolution existingAttributeTriple4 = searchExistingTriple(schemaModel, thisElement, RDF.type.asNode(), NewSourceLevel2.RDFSCMP.val());
                if (jsonAdapter.getOntClass().toString().equals(NewSourceLevel2.RDFSeq.val()) && NewSourceLevel2.RDFSCMP.val() != null) {
                    NewRDFUtil.removeTriple(schemaModel, thisElement, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSCMP.val());
                    NewRDFUtil.removeTriple(schemaModel, parentElement, NewSourceLevel2.HAS_Property, thisElement);
                    NewRDFUtil.removeTriple(schemaModel, thisElement, NewSourceLevel2.RDFSRange);


                }

                QuerySolution existingAttributeTriple2 = searchExistingTriple(schemaModel, thisElement, RDF.type.asNode(), NewSourceLevel2.RDFSSubClassOf.val());
                if (jsonAdapter.getOntClass().toString().equals(NewSourceLevel2.RDFSClass.val()) && NewSourceLevel2.RDFSSubClassOf.val() != null) {
                    String newName = parentElement + "/has_" + k;
                    NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
                    NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.RDFSDomain, parentElement);
                    NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.RDFSRange.asObjectProperty(model), thisElement);
                    //NewRDFUtil.removeTriple(schemaModel, thisElement, NewSourceLevel2.RDFSSubClassOf, NewSourceLevel2.RDFSSubClassOf.val());
                }
                if (jsonAdapter.getOntClass().toString().equals(NewSourceLevel2.RDFSClass.val()) && NewSourceLevel2.RDFSSubClassOf.val() != null) {
                    NewRDFUtil.removeTriple(schemaModel, thisElement, NewSourceLevel2.RDFSSubClassOf, NewSourceLevel2.RDFSSubClassOf.val());
                    NewRDFUtil.removeTriple(schemaModel, thisElement, NewSourceLevel2.RDFSSubClassOf);
                }

                //Add its type
                NewRDFUtil.addTriple(schemaModel, thisElement, NewSourceLevel2.TYPE, jsonAdapter.getOntClass());

                //Associate with parent
                NewRDFUtil.addTriple(schemaModel, thisElement, jsonAdapter.getParentLink(), parentElement); //parent and this are interchanged
                //c1 NewRDFUtil.addTriple(schemaModel, thisElement, NewSourceLevel2.RDFSDomain, parentElement);
                //NewRDFUtil.addTriple(schemaModel, parentElement, NewSourceLevel2.RDFSDomain.asObjectProperty(model), thisElement);

                if (jsonAdapter.getOntClass().toString().equals(NewSourceLevel2.RDFSClass.val()) && NewSourceLevel2.RDFSSubClassOf.val() != null) {
                    NewRDFUtil.removeTriple(schemaModel, thisElement, NewSourceLevel2.RDFSSubClassOf, NewSourceLevel2.RDFSSubClassOf.val());
                    NewRDFUtil.removeTriple(schemaModel, thisElement, NewSourceLevel2.RDFSSubClassOf);
                }

                Object itemJSON = jsonObject.get(k);

                if (JSONUtil.isArray(itemJSON)) {
                    jsonToSchemaNew(schemaModel, itemJSON, thisElement);
                } else if (JSONUtil.isAttribute(itemJSON)) {
                    addDataTypeTriple(schemaModel, thisElement, itemJSON);
                    //NewRDFUtil.addTriple(schemaModel, parentElement, NewSourceLevel2.RDFSDomain.asObjectProperty(model), thisElement);
                } else if (JSONUtil.isObject(itemJSON)) {

                    JSONObject object = (JSONObject) itemJSON;
                    //jsonToSchemaNew(OntModel schemaModel, Object base, String parentElement)
                    //when an object has inside another object
                    //String newName=thisElement+"/has_"+object.get(k);
                    //NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
                    //NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.RDFSDomain, parentElement);
                    //NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.RDFSRange.asObjectProperty(model), thisElement);
                    //try domain and range for referenced object

                    object.forEach((oKey, oVal) -> {
                        String oKeyName = thisElement + "/" + oKey;

                        JSONAdapter adapter = new JSONAdapter(model, object, oKey);
                        NewRDFUtil.addTriple(schemaModel, thisElement, NewSourceLevel2.TYPE, adapter.getOntClass());

                        String newName = thisElement + "/" + k + "_Collection";//thisElement oKeyName
                        NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
                        NewRDFUtil.addTriple(schemaModel, thisElement, NewSourceLevel2.RDFSRange, newName);
                        NewRDFUtil.addTriple(schemaModel, oKeyName, NewSourceLevel2.TYPE, adapter.getOntClass());

                        //NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.TYPE, adapter.getOntClass());
                        //	jsonToSchemaNew(schemaModel, object.get(oKey), newName);    //to remove seq  for attributes

                        //NewRDFUtil.addTriple(schemaModel, oKeyName, NewSourceLevel2.RDFSDomain.asObjectProperty(model), thisElement); //repeated?

                        //if an object is nested we create a variable
						/*	String newName="has_"+oKey;
					
						NewRDFUtil.addTriple(schemaModel, oKeyName, NewSourceLevel2.RDFSRange.asObjectProperty(model), newName);
						NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.RDFSDomain.asObjectProperty(model), oKeyName);
						
						//Add its type
						NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.TYPE, jsonAdapter.getOntClass());

						//Associate with parent
						NewRDFUtil.addTriple(schemaModel, thisElement, jsonAdapter.getParentLink(), newName);
						NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.RDFSDomain, thisElement);
						
						//NewRDFUtil.addTriple(schemaModel, oKeyName, adapter.getParentLink(), newName);
*/
                        NewRDFUtil.addTriple(schemaModel, oKeyName, adapter.getParentLink(), newName);//oKayName with thisElement switched
                        if (jsonAdapter.getOntClass().toString().equals(NewSourceLevel2.RDFSClass.val()) && NewSourceLevel2.RDFSSubClassOf.val() != null) {


                            NewRDFUtil.removeTriple(schemaModel, oKeyName, NewSourceLevel2.RDFSSubClassOf, NewSourceLevel2.RDFSSubClassOf.val());
                            NewRDFUtil.removeTriple(schemaModel, oKeyName, NewSourceLevel2.RDFSSubClassOf);

                        }

                        //if (!JSONUtil.isAttribute(oVal)) {

                        if (JSONUtil.isObject(oVal)) {
                            //when an object has inside another object
						/*	String newName=thisElement+"/has_"+oKey;
							NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
							NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.RDFSDomain, thisElement);
							NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.RDFSRange.asObjectProperty(model), oKeyName);
							//try domain and range for referenced object
							jsonToSchemaNew(schemaModel, object.get(oKey), oKeyName);
							*/
                            NewRDFUtil.addTriple(schemaModel, oKeyName, NewSourceLevel2.TYPE, adapter.getOntClass());
                            String nnewName = newName + "/" + oKey + "/" + oKey + "_Collection";//newName thisElement displays path without collection
                            NewRDFUtil.addTriple(schemaModel, nnewName, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
                            NewRDFUtil.addTriple(schemaModel, oKeyName, NewSourceLevel2.RDFSRange, nnewName);
                            jsonToSchemaNew(schemaModel, object.get(oKey), nnewName);    //to remove seq  for attributes

                        } else if (JSONUtil.isArray(oVal)) {
                            //String newArray = thisElement + "/" + NewSourceLevel2.RDFSeq.getIdentifierRegex();
                            //NewRDFUtil.addTriple(schemaModel, oKeyName, NewSourceLevel2.RDFSRange, newArray);
                            jsonToSchemaNew(schemaModel, object.get(oKey), oKeyName);

                        } else {
                            //NewRDFUtil.addTriple(schemaModel, parentElement, NewSourceLevel2.RDFSDomain.asObjectProperty(model), thisElement);
                            addDataTypeTriple(schemaModel, oKeyName, oVal);
                        }
                    });
                }
            });
        } else if (JSONUtil.isArray(base)) {
            JSONArray jsonArray = (JSONArray) base;

            String objectElemIdentifier = parentElement + "/" + NewSourceLevel2.RDFSeq.generateIdentifier();  //RDFSCMP Rdfseq
            String sqelem = objectElemIdentifier + "/" + NewSourceLevel2.RDFSCMP.generateIdentifier();
            String arrayElemIdentifier = objectElemIdentifier + "/" + NewSourceLevel2.RDFSeq.generateIdentifier();
            //String arrayElemIdentifier = objectElemIdentifier + "/" + NewSourceLevel2.RDFSeq.generateIdentifier();
            //String arrayElemIdentifier = parentElement + "/" + NewSourceLevel2.RDFSeq.generateIdentifier();

            NewRDFUtil.addTriple(schemaModel, objectElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
            NewRDFUtil.addTriple(schemaModel, objectElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSeq);
		/*
		String[] seg = parentElement.split("/");
		String ir = seg[seg.length-1];
		if(!ir.contains("RDFSCMP"))
		{*/
            NewRDFUtil.addTriple(schemaModel, parentElement, NewSourceLevel2.RDFSRange.asObjectProperty(model), objectElemIdentifier);

            NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSCMP);
            NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
            NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.RDFSDomain, objectElemIdentifier);
            for (int i = 0; i < jsonArray.size(); i++) {
                Object item = jsonArray.get(i);
                //JSONObject item = (JSONObject) jsonArray.get(i);
                if (JSONUtil.isObject(item)) {
                    //If content of array are objects, always use a single collection element, as defined outside

				/*	String existingElemRegex = parentElement + "/" + NewSourceLevel2.RDFSeq.getIdentifierRegex();//RDFSCMP RDFSeq
					QuerySolution existingTriple = searchExistingTriple(schemaModel, parentElement, NewSourceLevel2.RDFSDomain.asObjectProperty(model).asNode(), existingElemRegex);//HAS_Member RDFSDomain
					if (existingTriple != null) {
						objectElemIdentifier = existingTriple.getResource("o").getURI();
					}*/
                    // doamin should point to parent class
                    //NewRDFUtil.addTriple(schemaModel, parentElement, NewSourceLevel2.RDFSDomain.asObjectProperty(model), objectElemIdentifier);//HAS_Member RDFSDomain


                    //	String newName=parentElement+"/has_"+k;
			     /*		NewRDFUtil.addTriple(schemaModel, objectElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
					NewRDFUtil.addTriple(schemaModel, objectElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSeq);
					NewRDFUtil.addTriple(schemaModel, parentElement, NewSourceLevel2.RDFSRange.asObjectProperty(model), objectElemIdentifier);
					*/
                    //String sqelem=objectElemIdentifier + "/" + NewSourceLevel2.RDFSCMP.generateIdentifier();
                    //NewRDFUtil.addTriple(schemaModel, objectElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
                    //NewRDFUtil.addTriple(schemaModel, objectElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSeq);
                    //NewRDFUtil.addTriple(schemaModel, parentElement, NewSourceLevel2.RDFSRange.asObjectProperty(model), objectElemIdentifier);
				/*	
					NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSCMP);
					NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
					NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.RDFSDomain, objectElemIdentifier);
					*/

                    String[] segments = parentElement.split("/");
                    String idStr = segments[segments.length - 1];
                    String newName = objectElemIdentifier + "/" + idStr + "_Collection";
                    NewRDFUtil.addTriple(schemaModel, newName, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
                    NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.RDFSRange, newName);
                    jsonToSchemaNew(schemaModel, item, newName);    //to remove seq  for attributes

                } else if (JSONUtil.isArray(item)) {
                    //String arrayElemIdentifier = parentElement + "/" + NewSourceLevel2.RDFSeq.generateIdentifier();
                    //NewRDFUtil.addTriple(schemaModel, parentElement, NewSourceLevel2.RDFSRange.asObjectProperty(model), objectElemIdentifier);
					
				/*	NewRDFUtil.addTriple(schemaModel, objectElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
					NewRDFUtil.addTriple(schemaModel, objectElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSeq);
					//NewRDFUtil.addTriple(schemaModel, parentElement, NewSourceLevel2.RDFSRange.asObjectProperty(model), objectElemIdentifier);
					
					NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSCMP);
					NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
					NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.RDFSDomain, objectElemIdentifier);
					*/
                    jsonToSchemaNew(schemaModel, item, sqelem);//item , arrayElementIdentifier
                    //NewRDFUtil.addTriple(schemaModel, arrayElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
                    //NewRDFUtil.addTriple(schemaModel, arrayElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSeq);
                    //NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.RDFSRange, objectElemIdentifier);

                    //NewRDFUtil.addTriple(schemaModel, objectElemIdentifier, NewSourceLevel2.RDFSMember.asObjectProperty(model), arrayElemIdentifier);//RDFSMember to RDFDomain
                    //objectElemIdentifier   parentElement

                    //NewRDFUtil.addTriple(schemaModel, arrayElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
                    //NewRDFUtil.addTriple(schemaModel, arrayElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSeq);
                    //jsonToSchemaNew(schemaModel, item, arrayElemIdentifier);
                    //String sqelem2=objectElemIdentifier + "/" + NewSourceLevel2.RDFSCMP.generateIdentifier();
                    //String arrayElemIdentifier2 = sqelem2 + "/" + NewSourceLevel2.RDFSeq.generateIdentifier();

                    //NewRDFUtil.addTriple(schemaModel, arrayElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
                    //NewRDFUtil.addTriple(schemaModel, arrayElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSeq);
                    //NewRDFUtil.addTriple(schemaModel, arrayElemIdentifier, NewSourceLevel2.RDFSRange.asObjectProperty(model), objectElemIdentifier);//parentElement

                    //NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSCMP);
                    //NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
                    //NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.RDFSDomain, objectElemIdentifier);
                    //NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.RDFSRange, arrayElemIdentifier);


                    //String sqelem=objectElemIdentifier + "/" + NewSourceLevel2.RDFSCMP.generateIdentifier();

                    //jsonToSchemaNew(schemaModel,item, sqelem);//item , arrayElementIdentifier
                } else {
                    //String sqelem=objectElemIdentifier + "/" + NewSourceLevel2.RDFSCMP.generateIdentifier();

                    QuerySolution existingAttributeTriple2 = searchExistingTriple(schemaModel, parentElement, RDF.type.asNode(), NewSourceLevel2.RDFProperty.val());


                    //String sqelem=objectElemIdentifier + "/" + NewSourceLevel2.RDFSCMP.generateIdentifier();
				/*	NewRDFUtil.addTriple(schemaModel, objectElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
					NewRDFUtil.addTriple(schemaModel, objectElemIdentifier, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSeq);
					NewRDFUtil.addTriple(schemaModel, parentElement, NewSourceLevel2.RDFSRange.asObjectProperty(model), objectElemIdentifier);
					
					NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSCMP);
					NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
					NewRDFUtil.addTriple(schemaModel, sqelem, NewSourceLevel2.RDFSDomain, objectElemIdentifier);
					*/
                    addDataTypeTriple(schemaModel, sqelem, item);


                }
            }

        }
    }

    private static void addDataTypeTriple(OntModel schemaModel, String subject, Object item) {
        //String object = subject + "/" + AttributeUtil.getDataType(item).name();
        String object = AttributeUtil.getDataType(item);//.name()
        //Not necessary to look for existing triple here because all DataTypes do not have a unique identifier (random chars)
        NewRDFUtil.addTriple(schemaModel, subject, NewSourceLevel2.RDFSRange.asObjectProperty(model), object);

    }


    private static QuerySolution searchExistingTriple(OntModel schemaModel, String subject, Node predicate, String object) {
        Var s = Var.alloc("s");
        Var o = Var.alloc("o");
        Triple pattern = Triple.create(s, predicate, o);

        Optional<E_Regex> subjectFilterExpr = Optional.of(subject != null ? new E_Regex(new E_Str(new ExprVar(s)), subject, "i") : null);
        Optional<E_Regex> objectFilterExpr = Optional.of(object != null ? new E_Regex(new E_Str(new ExprVar(o)), object, "i") : null);
        //		Expr e = new E_Equals(new E_Str(new ExprVar(s)), new NodeValueString(object));
        ElementTriplesBlock block = new ElementTriplesBlock();
        block.addTriple(pattern);
        ElementGroup body = new ElementGroup();
        objectFilterExpr.ifPresent(regex -> body.addElement(new ElementFilter(regex)));
        subjectFilterExpr.ifPresent(regex -> body.addElement(new ElementFilter(regex)));
        body.addElement(block);

        Query q = QueryFactory.make();
        q.setQueryPattern(body);
        q.setQuerySelectType();
        q.addResultVar(s);
        q.addResultVar(o);

        QueryExecution qe = QueryExecutionFactory.create(q, schemaModel);
        System.out.println(q.toString());
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            return results.next();
        }

        return null;
    }


    public static String getOutputFile() {
        return outputFile;
    }

    public static String getIRI() {
        return IRI;
    }
}