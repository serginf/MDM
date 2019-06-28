package eu.supersede.mdm.storage.bdi.extraction;

import com.opencsv.CSVReader;
import eu.supersede.mdm.storage.bdi.extraction.metamodel.NewSourceLevel2;
import eu.supersede.mdm.storage.util.AttributeUtil;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.TempFiles;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static eu.supersede.mdm.storage.util.NewRDFUtil.addTriple;

public class CsvSchemaExtractor {
    private static final String LANG = "TURTLE"; //"RDF/XML");//"N-TRIPLE");
    private static String outputFile = "";
    private static String IRI = "";
    private OntModel schemaModel = null;
    private String root = null;
    private String outputFilePath = null;

    private List<String> header = new ArrayList<>();
    private List<String> types = new ArrayList<>();

    private static OntModel model;

    public CsvSchemaExtractor() {
    }

    public boolean initCsvSchemaExtractor(String filePath, String rootName) {
        boolean flag = false;
        try {
            extract(filePath);
            if (header.size() == types.size()) {
                initRDFSModel(rootName);
                constructRDFSModel();
                schemaModelToFile(rootName);
                flag = true;
            } else {
                System.out.println("Inconsistent CSV");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }



    public void extract(String csvFile) {
        try {
            CSVReader readerNew = new CSVReader(new FileReader(csvFile));
            List<String[]> rows = readerNew.readAll();
            System.out.println(rows.size());
            header = new ArrayList<>();
            types = new ArrayList<>();

            for (int x = 0; x < rows.size(); x++) {
                if (x == 0) {
                    //System.out.println(Arrays.toString(rows.get(x)));
                    for (String element : rows.get(x)) {
                        //System.out.print(cleanHeaderValue(element) + "\t");
                        header.add(cleanHeaderValue(element));
                    }
                    //System.out.println();
                } else if (x == 1) {
                    //System.out.println(Arrays.toString(rows.get(x)));
                    for (String element : rows.get(x)) {
                        //System.out.print(AttributeUtil.getStringDataType(element) + "\t");
                        types.add(AttributeUtil.getStringDataType(element));
                    }
                    //System.out.println();
                } else {
                    //System.out.println("Breaking at x = " + x);
                    break;
                }
            }
            System.out.println(header.toString());
            System.out.println(types.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initRDFSModel(String rootName) {
        try {
            model = ModelFactory.createOntologyModel();
            String path = ConfigManager.getProperty("resources_path") + "model/new_metadata_model.owl";

            model.read(new File(path).toURI().toURL().toString());
            //init Schema Model
            schemaModel = ModelFactory.createOntologyModel();
            schemaModel.setNsPrefix(NewNamespaces2.rdfs.name(), NewNamespaces2.rdfs.val());

            root = NewSourceLevel2.ROOT.val() + "/" + rootName;
            this.IRI = root;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void constructRDFSModel() {
        addTriple(schemaModel, root, NewSourceLevel2.TYPE, NewSourceLevel2.RDFSClass);
        for (int x = 0; x < header.size(); x++) {
            addTriple(schemaModel, root + "/" + header.get(x), NewSourceLevel2.TYPE, NewSourceLevel2.RDFProperty);
            addTriple(schemaModel, root + "/" + header.get(x), NewSourceLevel2.RDFSDomain, root);
            addTriple(schemaModel, root + "/" + header.get(x), NewSourceLevel2.RDFSRange, types.get(x));
        }
    }

    private void schemaModelToFile(String rootName) {
        outputFilePath = TempFiles.getIncrementalTempFile(rootName);
        try {
            assert schemaModel != null;
            schemaModel.write(new FileOutputStream(outputFilePath), LANG);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getCsvModelIRI() {
        return IRI;
    }

    public String getCsvModelOutputFilePath() {
        return outputFilePath;
    }

    private String cleanHeaderValue(String value) {
        String removeSpecialCharacters = value.replaceAll("[^-a-zA-Z0-9_\\s]", "");
        removeSpecialCharacters = removeSpecialCharacters.replaceAll("-", " ");
        return removeSpecialCharacters.trim().replaceAll("( )+", "_");
    }
}
