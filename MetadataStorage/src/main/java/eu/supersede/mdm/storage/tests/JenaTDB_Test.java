package eu.supersede.mdm.storage.tests;

import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.GlobalLevel;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

/**
 * Created by snadal on 20/06/17.
 */
public class JenaTDB_Test {

    public static void main(String[] args) throws Exception {
        Dataset dataset = TDBFactory.createDataset("/home/snadal/Desktop/BolsterMetadataStorage");
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        RDFUtil.addTriple(model, Namespaces.R.val()+"DataGatheringTool", Namespaces.rdf.val()+"type", GlobalLevel.CONCEPT.val());
        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();

    }

}
