package eu.supersede.mdm.storage.service.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import eu.supersede.mdm.storage.errorhandling.exception.DeleteNodeGlobalGException;
import eu.supersede.mdm.storage.service.impl.model.LavObj;
import eu.supersede.mdm.storage.util.MongoCollections;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.tdb.TDBFactory;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {Utils.class ,MongoCollections.class})
@PowerMockIgnore({"javax.xml.*", "org.xml.sax.*", "org.w3c.dom.*", "org.apache.log4j.*"})
public class DeleteGlobalGraphServiceImplTest {

    DeleteGlobalGraphServiceImpl delGlobalG;

    public DeleteGlobalGraphServiceImplTest(){}

    Dataset ds  = TDBFactory.createDataset();
    //Basic triples
    String graphName = "http://example/";
    String s = "http://example/team";
    String p = "http://example/hasFeature";
    String o = "http://example/teamname";

    /**
     * Initialize delGlobalG and dataset with the default triple  before every test.
     */
    @Before
    public void setup() {
        delGlobalG = new DeleteGlobalGraphServiceImpl();
        ds = TDBFactory.createDataset();
        ds.begin(ReadWrite.WRITE);
        Model model = ds.getNamedModel(graphName);
        model.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
        model.commit();
        ds.commit();
        ds.end();
    }

    /* Happy path
     *
     * Purpose: Make delete node S (http://example/team) to success. It is assumed there are not mappings,
     *          just one global graph. This test performs a call in deleteNode method of
     *          DeleteGlobalGraphServiceImpl class.
     *
     * Pre-condition: In jena dataset there is one graph: globalgraph (http://example/) with one set of triples
     *                containing the node to be deleted. The node to be deleted is not in attribute sameAs of
     *                Mongodb LAVMapping collection, so need to check the iri in jena TDB.
     *
     * Post-condition: Calling the method deleteNode will return true. The dataset will be empty since all
     *                 triples will be deleted.
     */
    @Test
    public void deleteNode_happypath() {
    /*-----------------------------------
     |            Create mocks
     *----------------------------------*/
        ds = Mockito.spy(ds);
        //create a mock when calling close function to avoid closing the dataset during the execution.
        Mockito.doNothing().when(ds).close();

        PowerMockito.mockStatic(Utils.class);
        //when calling Utils.getTDBDataset(), instead of running the real code, we return our dataset.
        PowerMockito.when(Utils.getTDBDataset()).thenReturn(ds);

        DeleteGlobalGraphServiceImpl delSpy = Mockito.spy(delGlobalG);

        //we return empty list since this test does not contains LAVMappings.
        Mockito.doReturn(new ArrayList<>()).when(delSpy).getLavMappingsRelated(Mockito.anyString(),Mockito.any(),Mockito.any());

    /*-----------------------------------
     | Execute method and verify results
     *----------------------------------*/
        Boolean flag = delSpy.deleteNode(graphName,s);
        Assert.assertTrue(flag); //Verify method returns true when delete is successful
        ds.begin(ReadWrite.READ);
        Assert.assertTrue(!ds.containsNamedModel(graphName)); //If delete is successful, there is no model in the dataset
        ds.end();
    }


    /* Unhappy path
     *
     * Purpose: Make delete node S (http://example/team) to fail. It is assumed the node S is contained in
     *          a wrapper graph. This test performs a call in deleteNode method of DeleteGlobalGraphServiceImpl class.
     *
     * Pre-condition: In jena dataset there are two graphs: GlobalGraph and Wrapper with the same triples.
     *
     * Post-condition: Calling the method deleteNode, throws a DeleteNodeGlobalGException since the node
     *                 to be deleted is containing in a wrapper cannot be deleted. Any elements are deleted.
     */
    @Test(expected = DeleteNodeGlobalGException.class)
    public void deleteNode_unhappypath() {

    /*-----------------------------------
     |  Initialize dataset for test and arraylist with related mappings IRIs.
     *----------------------------------*/
        String wrapperName = "http://example/wrapper";

        ds.begin(ReadWrite.WRITE);
        Model modelw = ds.getNamedModel(wrapperName);
        modelw.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
        modelw.commit();
        ds.commit();

        List<LavObj> list = new ArrayList<>();
        list.add(new LavObj("1234",wrapperName,"http://example/datasource"));

    /*-----------------------------------
     |            Create mocks
     *----------------------------------*/
        ds = Mockito.spy(ds);
        Mockito.doNothing().when(ds).close();

        PowerMockito.mockStatic(Utils.class);
        PowerMockito.when(Utils.getTDBDataset()).thenReturn(ds);

        DeleteGlobalGraphServiceImpl delSpy = Mockito.spy(delGlobalG);

        //we return our list with the wrapper and datasource IRI.
        Mockito.doReturn(list).when(delSpy).getLavMappingsRelated(Mockito.anyString(),Mockito.any(),Mockito.any());

    /*-----------------------------------
     | Execute method
     *----------------------------------*/
        Boolean flag = delSpy.deleteNode(graphName,s);
    }

    /* Unhappy path
     *
     * Purpose: Make delete node S (http://example/team) to fail. It is assumed the node S is contained in the attribute
     *          sameAs of LAVMapping collections. This test performs a call in deleteNode method of
     *          DeleteGlobalGraphServiceImpl class.
     *
     * Pre-condition: In jena dataset there are two graphs: GlobalGraph and Wrapper with the same triples.
     *                In mongodb there is a LAVMapping with the node to be deleted in the sameAs attribute.
     *
     * Post-condition: Calling the method deleteNode, throws a DeleteNodeGlobalGException since the node
     *                 to be deleted is containing in a the LAVMapping object of mongoDB, so it
     *                 cannot be deleted. Any deletion is performed.
     */
    @Test(expected = DeleteNodeGlobalGException.class)
    public void deleteNodeWhenIriInMongo_unhappypath() {

    /*-----------------------------------
     |  Initialize dataset for test and arraylist with related mappings IRIs.
     *----------------------------------*/
        String wrapperName = "http://example/wrapper";

        ds.begin(ReadWrite.WRITE);
        Model modelw = ds.getNamedModel(wrapperName);
        modelw.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
        modelw.commit();
        ds.commit();
        ds.end();

        List<Document> sameAs = new ArrayList<>();
        sameAs.add(new Document("feature","http://example/feature"));
        sameAs.add(new Document("feature",s));

        String LAVMappingID = "ID1234";
        String wrapperID = "IDW1234";
        String globalGraphID = "1234";
        Document LAVMappingDoc = new Document("LAVMappingID", LAVMappingID).append("sameAs",sameAs).append("wrapperID",wrapperID);

    /*-----------------------------------
     |            Create mocks
     *----------------------------------*/
        ds = Mockito.spy(ds);
        Mockito.doNothing().when(ds).close();

        PowerMockito.mockStatic(Utils.class);
        MongoClient client = PowerMockito.mock(MongoClient.class);
        PowerMockito.when(Utils.getMongoDBClient()).thenReturn(client);

        PowerMockito.mockStatic(MongoCollections.class);
        MongoCollection<Document> mockCollection = PowerMockito.mock(MongoCollection.class);

        FindIterable iterGlobalGraph = Mockito.mock(FindIterable.class);
        FindIterable iterLavMappping = Mockito.mock(FindIterable.class);
        FindIterable iterWrapper = Mockito.mock(FindIterable.class);
        FindIterable iterDataSource = Mockito.mock(FindIterable.class);

        MongoCursor cursor = PowerMockito.mock(MongoCursor.class);

        PowerMockito.when(MongoCollections.getGlobalGraphCollection(client)).thenReturn(mockCollection);
        PowerMockito.when(MongoCollections.getGlobalGraphCollection(client).find(new Document("namedGraph",graphName))).thenReturn(iterGlobalGraph);
        PowerMockito.when(iterGlobalGraph.first()).thenReturn(new Document("globalGraphID",globalGraphID));

        PowerMockito.when(MongoCollections.getLAVMappingCollection(client)).thenReturn(mockCollection);
        PowerMockito.when(MongoCollections.getLAVMappingCollection(client).find(new Document("globalGraphID", globalGraphID))).thenReturn(iterLavMappping);
        PowerMockito.when(iterLavMappping.iterator()).thenReturn(cursor);

        PowerMockito.when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        PowerMockito.when(cursor.next()).thenReturn(LAVMappingDoc);

        PowerMockito.when(MongoCollections.getWrappersCollection(client)).thenReturn(mockCollection);
        PowerMockito.when(MongoCollections.getWrappersCollection(client).find(new Document("wrapperID",wrapperID))).thenReturn(iterWrapper);
        PowerMockito.when(iterWrapper.first()).thenReturn(new Document("iri",wrapperName));

        PowerMockito.when(MongoCollections.getDataSourcesCollection(client)).thenReturn(mockCollection);
        PowerMockito.when(MongoCollections.getDataSourcesCollection(client).find(new Document("wrappers", wrapperID))).thenReturn(iterDataSource);
        PowerMockito.when(iterDataSource.first()).thenReturn(new Document("iri",graphName+"datasource") );

    /*-----------------------------------
     | Execute method
     *----------------------------------*/
        Boolean flag = delGlobalG.deleteNode(graphName,s);
    }


    /* Happy path
     *
     * Purpose: Make delete property P (http://example/hasFeature) whose domain is http://example/team and range
     *          http://example/teamname to success. It is assumed there are not mappings, just one global graph.
     *          This test performs a call in deleteNode method of DeleteGlobalGraphServiceImpl class.
     *
     * Pre-condition: In jena dataset there is one graph: globalgraph (http://example/) with one set of triples
     *                containing the node to be deleted. The node to be deleted is not in attribute sameAs of
     *                Mongodb LAVMapping collection, so need to check the iri in jena TDB.
     *
     * Post-condition: Calling the method deleteNode will return true. The dataset will be empty since all
     *                 triples will be deleted.
     */
    @Test
    public void deleteProperty_happypath() {

    /*-----------------------------------
     |  Initialize dataset for test
     *----------------------------------*/
        String s2 = "http://example/team";
        String p2 = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String o2 = "http://www.essi.upc.edu/~snadal/BDIOntology/Global/Concept";
        String s3 = "http://example/teamname";
        String p3 = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String o3 = "http://www.essi.upc.edu/~snadal/BDIOntology/Global/Feature";

        ds.begin(ReadWrite.WRITE);
        Model model = ds.getNamedModel(graphName);
        model.add(new ResourceImpl(s2), new PropertyImpl(p2), new ResourceImpl(o2));
        model.add(new ResourceImpl(s3), new PropertyImpl(p3), new ResourceImpl(o3));
        model.commit();
        ds.commit();
        ds.end();
    /*-----------------------------------
     |            Create mocks
     *----------------------------------*/
        ds = Mockito.spy(ds);
        //create a mock when calling close function to avoid closing the dataset during the execution.
        Mockito.doNothing().when(ds).close();

        PowerMockito.mockStatic(Utils.class);
        //when calling Utils.getTDBDataset(), instead of running the real code, we return our dataset.
        PowerMockito.when(Utils.getTDBDataset()).thenReturn(ds);

        DeleteGlobalGraphServiceImpl delSpy = Mockito.spy(delGlobalG);

        //we return empty list since this test does not contains LAVMappings.
        Mockito.doReturn(new ArrayList<>()).when(delSpy).getLavMappingsRelated(Mockito.anyString(),Mockito.any(),Mockito.any());

    /*-----------------------------------
     | Execute method and verify results
     *----------------------------------*/
        Boolean flag = delSpy.deleteProperty(graphName,s,p,o);
        Assert.assertTrue(flag); //Verify method returns true when delete is successful
        ds.begin(ReadWrite.READ);
        //TODO: improve condition for assert
        Assert.assertTrue(ds.getNamedModel(graphName).size() ==2); //If delete is successful, size equals 2
        ds.end();
    }

    /* Unhappy path
     *
     * Purpose: Make delete property P (http://example/hasFeature) whose domain is http://example/team and range
     *          http://example/teamname to fail. It is assumed there are mappings, the wrapper graph contains the
     *          property. This test performs a call in deleteProperty method of DeleteGlobalGraphServiceImpl class.
     *
     * Pre-condition: In jena dataset there is one graph: globalgraph (http://example/) with one set of triples
     *                containing the node to be deleted. The node to be deleted is not in attribute sameAs of
     *                Mongodb LAVMapping collection, so need to check the iri in jena TDB.
     *
     * Post-condition: Calling the method deleteProperty, throws a DeleteNodeGlobalGException since the node
     *                 to be deleted is containing in a the LAVMapping object of mongoDB, so it
     *                 cannot be deleted. Any deletion is performed.
     */
    @Test(expected = DeleteNodeGlobalGException.class)
    public void deleteProperty_unhappypath() {

    /*-----------------------------------
     |  Initialize dataset for test
     *----------------------------------*/
        String s2 = "http://example/team";
        String p2 = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String o2 = "http://www.essi.upc.edu/~snadal/BDIOntology/Global/Concept";
        String s3 = "http://example/teamname";
        String p3 = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String o3 = "http://www.essi.upc.edu/~snadal/BDIOntology/Global/Feature";

        ds.begin(ReadWrite.WRITE);
        Model model = ds.getNamedModel(graphName);
        model.add(new ResourceImpl(s2), new PropertyImpl(p2), new ResourceImpl(o2));
        model.add(new ResourceImpl(s3), new PropertyImpl(p3), new ResourceImpl(o3));
        model.commit();
        ds.commit();
        ds.end();

        String wrapperName = "http://example/wrapper";

        ds.begin(ReadWrite.WRITE);
        Model modelw = ds.getNamedModel(wrapperName);
        modelw.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
        modelw.commit();
        ds.commit();
        ds.end();

        List<LavObj> list = new ArrayList<>();
        list.add(new LavObj("1234",wrapperName,"http://example/datasource"));
    /*-----------------------------------
     |            Create mocks
     *----------------------------------*/
        ds = Mockito.spy(ds);
        //create a mock when calling close function to avoid closing the dataset during the execution.
        Mockito.doNothing().when(ds).close();

        PowerMockito.mockStatic(Utils.class);
        //when calling Utils.getTDBDataset(), instead of running the real code, we return our dataset.
        PowerMockito.when(Utils.getTDBDataset()).thenReturn(ds);

        DeleteGlobalGraphServiceImpl delSpy = Mockito.spy(delGlobalG);

        //we return empty list since this test does not contains LAVMappings.
        Mockito.doReturn(list).when(delSpy).getLavMappingsRelated(Mockito.anyString(),Mockito.any(),Mockito.any());

    /*-----------------------------------
     | Execute method and verify results
     *----------------------------------*/
        Boolean flag = delSpy.deleteProperty(graphName,s,p,o);
    }


}