package eu.supersede.mdm.storage.service.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.ServiceUtils;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class UpdateGlobalGraphServiceImpl {


  public Boolean updateTriples(JSONObject changes,String namedGraph) {

    if (changes.containsKey("nodes")) {

      List<String> currentIris = new ArrayList<>();
      ((JSONArray)changes.get("nodes")).forEach(el -> currentIris.add(((JSONObject)el).getAsString("old")));

      // Check which mappings contains the modified iri.
      List<LavObj> LavM = getLavMappingsAffected(getGlobalGraphId(namedGraph),currentIris);

      for (Object selectedElement : ((JSONArray) changes.get("nodes"))) {
        JSONObject objSelectedElement = (JSONObject) selectedElement;
        String oldIRI = objSelectedElement.getAsString("old");
        String newIRI = objSelectedElement.getAsString("new");

        updateNodeIri(namedGraph, oldIRI, newIRI);

        if (!LavM.isEmpty()) {
          LavM.forEach( obj -> {
            updateLavMapping(obj.LAVMappingID,oldIRI,newIRI);
            updateNodeIri(obj.wrapperIRI, oldIRI, newIRI);
            updateNodeIri(obj.dataSourceIRI,oldIRI,newIRI);
          });
        }
      }
    }

    if (changes.containsKey("properties")) {
      ((JSONArray)changes.get("properties")).forEach(selectedElement -> {
        JSONObject objSelectedElement = (JSONObject)selectedElement;
        String pOldIRI = objSelectedElement.getAsString("pOld");
        String pNewIRI = objSelectedElement.getAsString("pNew");

        updatePropertyIri(namedGraph,pOldIRI,pNewIRI);

      });
    }

    if(changes.containsKey("new")){
      ((JSONArray)changes.get("new")).forEach(selectedElement -> {
        JSONObject objSelectedElement = (JSONObject)selectedElement;
        String sIRI = objSelectedElement.getAsString("s");
        String pIRI = objSelectedElement.getAsString("p");
        String oIRI = objSelectedElement.getAsString("o");

        RDFUtil.addTriple(namedGraph,sIRI,pIRI,oIRI);
      });
    }

    if(changes.containsKey("changeNodeType")){

      List<String> currentIris = new ArrayList<>();
      ((JSONArray)changes.get("changeNodeType")).forEach(el -> currentIris.add(((JSONObject)el).getAsString("s")));
      // Check which mappings contains the change node.
      List<LavObj> LavM = getLavMappingsAffected(getGlobalGraphId(namedGraph),currentIris);

      ((JSONArray)changes.get("changeNodeType")).forEach(selectedElement -> {
        JSONObject objSelectedElement = (JSONObject)selectedElement;
        String sIRI = objSelectedElement.getAsString("s");
        String pIRI = objSelectedElement.getAsString("p");
        String oIRI = objSelectedElement.getAsString("o");
        String operation = objSelectedElement.getAsString("operation");

        if(operation.equals("add")){
          RDFUtil.addTriple(namedGraph,sIRI,pIRI,oIRI);
        }else{
          ServiceUtils.deleteTriples(namedGraph,sIRI,pIRI,oIRI);
        }


        if (!LavM.isEmpty()) {
          LavM.forEach( obj -> {
            if(operation.equals("add")){
              RDFUtil.addTriple(obj.wrapperIRI,sIRI,pIRI,oIRI);
            }else{
              ServiceUtils.deleteTriples(obj.wrapperIRI,sIRI,pIRI,oIRI);
            }
          });
        }

      });
    }

    return null;
  }

  /**
   * Gets the LavMappings IRI, wrapperIri and datasourceIRI which contains the features IRIs to be updated.
   *
   * @param globalGraphId
   * @return a list for every lavmapping associated with the given globalgraph and need to be updated.
   */
  public List<LavObj> getLavMappingsAffected(String globalGraphId, List<String> IRIs){

    MongoClient client = Utils.getMongoDBClient();
    // Array which contains the wrapper iri for the lavmapping.
    List<LavObj> listLWD = new ArrayList<>();

    try (MongoCursor<Document> cursor =  MongoCollections.getLAVMappingCollection(client).find
            (new Document("globalGraphID", globalGraphId)).iterator()) {

      while (cursor.hasNext()) {
        Document lavMappingObj = cursor.next();

        //Gets related datasource, wrapper and mapping that also need to be updated
        for (Object el : ((ArrayList)lavMappingObj.get("sameAs"))) {
          String feature = ((Document) el).getString("feature");

          if(IRIs.contains(feature)){
            String lavMappingID = lavMappingObj.getString("LAVMappingID");
            String wrapperIri = MongoCollections.getWrappersCollection(client).find
                    (new Document("wrapperID", lavMappingObj.getString("wrapperID")))
                    .first().getString("iri");
            String datasourceIri = MongoCollections.getDataSourcesCollection(client).find(
                    new Document("wrappers", lavMappingObj.getString("wrapperID")))
                    .first().getString("iri");

            listLWD.add(new LavObj(lavMappingID,wrapperIri,datasourceIri));
            //we stop for each loop since we already identify we need to update these objects.
            break;
          }
        }
      }
    }catch (Exception e){
      e.printStackTrace();
    }
    client.close();
    return listLWD;
  }


  /**
   * Updates the feature IRI from sameAs key in LavMappings
   * @param LAVMappingID
   * @param oldIRI actual iri.
   * @param newIRI new iri.
   */
  public void updateLavMapping(String LAVMappingID, String oldIRI, String newIRI){
    Document query = new Document();
    query.append("LAVMappingID",LAVMappingID)
            .append("sameAs.feature", oldIRI);

    Document setData = new Document();
    setData.append("sameAs.$.feature", newIRI);

    Document update = new Document();
    update.append("$set", setData);

    MongoClient client = Utils.getMongoDBClient();
    MongoCollections.getLAVMappingCollection(client).updateOne(query,update);

    client.close();
  }

  /**
   * Delete triple with oldIri and insert new triple with newIri in jena graph
   * @param graphIRI iri of the graph that needs to be updated.
   * @param oldIRI actual iri that appears in the triples.
   * @param newIRI new iri that is going to replace the actual iri.
   */
  public void updatePropertyIri(String graphIRI, String oldIRI, String newIRI){
    RDFUtil.runAnUpdateQuery("WITH <"+graphIRI+"> DELETE {?s <"+oldIRI+"> ?o} " +
            "INSERT {?s <"+newIRI+"> ?o } WHERE {  ?s <"+oldIRI+"> ?o }");
  }

  /**
   * Delete triple with oldIri and insert new triple with newIri in jena graph
   * @param graphIRI iri of the graph that needs to be updated.
   * @param oldIRI actual iri that appears in the triples.
   * @param newIRI new iri that is going to replace the actual iri.
   */
  public void updateNodeIri(String graphIRI, String oldIRI, String newIRI){
    // Look and update triples where oldIRI is object.
    RDFUtil.runAnUpdateQuery("WITH <"+graphIRI+">  DELETE {?s ?p <"+oldIRI+">} " +
            "INSERT {?s ?p <"+newIRI+">} WHERE {  ?s ?p <"+oldIRI+"> }");
    // Look and update triples where oldIRI is subject.
    RDFUtil.runAnUpdateQuery("WITH <"+graphIRI+">  DELETE {<"+oldIRI+"> ?p ?o} " +
            "INSERT {<"+newIRI+"> ?p ?o} WHERE {  <"+oldIRI+"> ?p ?o }");
  }

  /**
   * Gets the global graph id.
   * @param namedGraph is the graph iri
   * @return a string that represent the global graph id.
   */
  public String getGlobalGraphId(String namedGraph){
    MongoClient client = Utils.getMongoDBClient();
    String globalGraphId = MongoCollections.getGlobalGraphCollection(client)
            .find(new Document("namedGraph",namedGraph)).first().getString("globalGraphID");
    client.close();
    return globalGraphId;
  }

  class LavObj{
    String LAVMappingID;
    String wrapperIRI;
    String dataSourceIRI;

    public LavObj(){
      this.LAVMappingID = null;
      this.wrapperIRI = null;
      this.dataSourceIRI = null;
    }

    public LavObj(String LAVMappingID, String wrapperIRI, String dataSourceIRI) {
      this.LAVMappingID = LAVMappingID;
      this.wrapperIRI = wrapperIRI;
      this.dataSourceIRI = dataSourceIRI;
    }
  }

}
