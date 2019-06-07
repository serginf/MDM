package eu.supersede.mdm.storage.bdi.extraction;

import eu.supersede.mdm.storage.bdi.extraction.metamodel.NewSourceLevel2;
import eu.supersede.mdm.storage.util.AttributeUtil;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.TempFiles;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.Objects;

import static eu.supersede.mdm.storage.util.NewRDFUtil.addTriple;


/**
 *
 * @author Kashif Rabbani
 */
public class XmlSchemaExtractor {
    private static OntModel model;
    private static final String LANG = "TURTLE"; //"RDF/XML");//"N-TRIPLE");
    private boolean documentRootFlag = false;

    public XmlSchemaExtractor(String filePath) {
        try {
            File file = new File(filePath);

            InputStream inputStream= new FileInputStream(filePath);
            Reader reader = new InputStreamReader(inputStream,"UTF-8");
            InputSource inputSource = new InputSource(reader);
            inputSource.setEncoding("UTF-8");

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(inputSource);
            doc.getDocumentElement().normalize();
            String rootName = doc.getDocumentElement().getNodeName();
            System.out.println("Root element :" + rootName);

            OntModel schemaModel = null;
            if (doc.hasChildNodes()) {
                //init OntModel
                model = ModelFactory.createOntologyModel();
                String path = ConfigManager.getProperty("resources_path") + "/" + "model/new_metadata_model.owl";
                model.read(new File(path).toURI().toURL().toString());

                //init Schema Model
                schemaModel = ModelFactory.createOntologyModel();
                schemaModel.setNsPrefix(NewNamespaces2.rdfs.name(), NewNamespaces2.rdfs.val());

                //create Root
                String root = NewSourceLevel2.ROOT.val() + "/" + file.getName().split("\\.")[0];
                //NewRDFUtil.addTriple(schemaModel, root, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass.asOntClass(model));

                //Call to Parse XML Structure
                parseXmlStructure(schemaModel, doc.getChildNodes(), root);
            }

            String outputFile = TempFiles.getIncrementalTempFile(file.getName().split("\\.")[0]);
            try {
                assert schemaModel != null;
                schemaModel.write(new FileOutputStream(outputFile), LANG);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseXmlStructure(OntModel schemaModel, NodeList nodeList, String root) {

        for (int count = 0; count < nodeList.getLength(); count++) {

            Node currentNode = nodeList.item(count);
            String newRoot = "";
            String root_Collection = "";
            //String thisElement = root + "/" + currentNode.getNodeName();
            //System.out.println("Type of the Node: " + currentNode.getNodeType());

            // Element Node
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                // get node name and value
                System.out.println("\n Node " + currentNode.getNodeName() + " [OPEN]");

                int childNodesCount = countNodeChildren(currentNode);

                if (childNodesCount > 0) {
                    newRoot = root + "/" + currentNode.getNodeName();
                    root_Collection = root + "/" + currentNode.getNodeName() + "_Collection";

                    //It means it is a Document Root Node
                    if (count == 0) {
                        documentRootFlag = true;
                        addTriple(schemaModel, newRoot, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);

                        checkForParentAttributes(schemaModel, currentNode, root, newRoot, root_Collection);
                        // loop again as this node has child nodes
                        parseXmlStructure(schemaModel, currentNode.getChildNodes(), newRoot);

                    } else {
                        documentRootFlag = false;
                        addTriple(schemaModel, root_Collection, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
                        addTriple(schemaModel, newRoot, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
                        addTriple(schemaModel, newRoot, NewSourceLevel2.RDFSDomain, root);
                        addTriple(schemaModel, newRoot, NewSourceLevel2.RDFSRange, root_Collection);

                        checkForParentAttributes(schemaModel, currentNode, root, newRoot, root_Collection);
                        // loop again as this node has child nodes
                        parseXmlStructure(schemaModel, currentNode.getChildNodes(), root_Collection);
                    }


                }

                if (childNodesCount == 0) {
                    newRoot = root + "/" + currentNode.getNodeName();

                    if (currentNode.getTextContent().trim() != null) {
                        addTriple(schemaModel, newRoot, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
                        addTriple(schemaModel, newRoot, NewSourceLevel2.RDFSDomain, root);
                        addTriple(schemaModel, newRoot, NewSourceLevel2.RDFSRange,
                                AttributeUtil.getStringDataType(currentNode.getTextContent().trim()));
                    }
                    checkForChildAttributes(schemaModel, currentNode, root, newRoot, root_Collection);
                }

                System.out.println("Node " + currentNode.getNodeName() + " [CLOSE]");
            }
        }
        //schemaModel.write(System.out, LANG);
    }

    private Integer countNodeChildren(Node currentNode) {
        //System.out.println("Node Value =" + currentNode.getTextContent());
        int nodeCounter = 0;
        if (currentNode.hasChildNodes()) {
            NodeList nodeList1 = currentNode.getChildNodes();
            for (int x = 0; x < nodeList1.getLength(); x++) {
                Node temp = nodeList1.item(x);
                if (temp.getNodeType() != Node.TEXT_NODE) {
                    System.out.println(temp.getNodeName());
                    nodeCounter++;
                }
            }
        }
        System.out.println(currentNode.getNodeName() + " has " + nodeCounter + " children");
        return nodeCounter;
    }

    private void checkForParentAttributes(OntModel schemaModel, Node currentNode, String root, String new_Root, String root_Collection) {
        String attribute = null;
        if (currentNode.hasAttributes()) {
            NamedNodeMap nodeMap = currentNode.getAttributes();
            for (int i = 0; i < nodeMap.getLength(); i++) {
                Node attributeNode = nodeMap.item(i);
                //System.out.println("attr name : " + attributeNode.getNodeName() + " attr value : " + attributeNode.getNodeValue());

                if (attributeNode.getNodeValue().trim() != null) {
                    if (documentRootFlag) {
                        attribute = new_Root + "/" + attributeNode.getNodeName();
                        createAttributeTriples(schemaModel, attribute, attributeNode, new_Root);
                    } else {
                        attribute = root_Collection + "/" + attributeNode.getNodeName();
                        createAttributeTriples(schemaModel, attribute, attributeNode, root_Collection);
                    }
                    /*NewRDFUtil.addTriple(schemaModel, attribute, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
                    NewRDFUtil.addTriple(schemaModel, attribute, NewSourceLevel2.RDFSDomain, root_Collection);
                    NewRDFUtil.addTriple(schemaModel, attribute, NewSourceLevel2.RDFSRange, AttributeUtil.getStringDataType(attributeNode.getNodeValue().trim()));*/
                }
            }
        }
    }


    private void checkForChildAttributes(OntModel schemaModel, Node currentNode, String root, String new_Root, String root_Collection) {
        if (currentNode.hasAttributes()) {
            NamedNodeMap nodeMap = currentNode.getAttributes();
            for (int i = 0; i < nodeMap.getLength(); i++) {
                Node attributeNode = nodeMap.item(i);
                System.out.println("attr name : " + attributeNode.getNodeName());
                System.out.println("attr value : " + attributeNode.getNodeValue());
                String attribute = root + "/" + attributeNode.getNodeName();
                createAttributeTriples(schemaModel, attribute, attributeNode, root);
                /*NewRDFUtil.addTriple(schemaModel, attribute, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
                NewRDFUtil.addTriple(schemaModel, attribute, NewSourceLevel2.RDFSDomain, root);
                NewRDFUtil.addTriple(schemaModel, attribute, NewSourceLevel2.RDFSRange, AttributeUtil.getStringDataType(attributeNode.getNodeValue().trim()));*/
            }
        }
    }

    private void createAttributeTriples(OntModel schemaModel, String attribute, Node attributeNode, String uri) {
        addTriple(schemaModel, attribute, NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
        addTriple(schemaModel, attribute, NewSourceLevel2.RDFSDomain, uri);
        addTriple(schemaModel, attribute, NewSourceLevel2.RDFSRange,
                AttributeUtil.getStringDataType(attributeNode.getNodeValue().trim()));
    }

/*
    public static void main(String[] args) {
        new XmlSchemaExtractor(args[0]);
    }
*/
}
