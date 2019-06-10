package eu.supersede.mdm.storage.bdi.mdm.constructs;

import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.model.metamodel.GlobalGraph;
import eu.supersede.mdm.storage.resources.WrapperResource;
import eu.supersede.mdm.storage.resources.bdi.SchemaIntegrationHelper;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.HttpUtils;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.semarglproject.vocab.OWL;
import org.semarglproject.vocab.RDF;
import org.semarglproject.vocab.RDFS;

public class Conversion {
    private JSONObject wrapper = new JSONObject();
    private JSONObject globalGraphInfo = new JSONObject();
    private String postWrapperUrl = ConfigManager.getProperty("metadata_data_storage_url") + "wrapper/";
    private String mdmGlobalGraphIri = "";


    public Conversion(String bdiGlobalGraphId) {
        SchemaIntegrationHelper schemaIntegrationHelper = new SchemaIntegrationHelper();
        String initGlobalGraphInfo = schemaIntegrationHelper.getIntegratedDataSourceInfo(bdiGlobalGraphId);
        if (!initGlobalGraphInfo.isEmpty()) {
            globalGraphInfo = (JSONObject) JSONValue.parse(initGlobalGraphInfo);
        }
        mdmGlobalGraphIri = Namespaces.G.val() + bdiGlobalGraphId;
        runFlow();
    }

    /**
     * This method performs conversion in steps
     * Global Graph
     * Wrappers
     * LAV Mappings
     * Sub Graph Mappings
     */
    private void runFlow() {
        /*schema_iri is IRI (namedGraph) of the BDI graph which need to be converted into MDM graph*/

        new MDMGlobalGraph(globalGraphInfo.getAsString("name"), globalGraphInfo.getAsString("schema_iri"), mdmGlobalGraphIri);

        new MDMWrapper(globalGraphInfo, mdmGlobalGraphIri);

        new MDMLavMapping(mdmGlobalGraphIri);

        //schemaIntegrationHelper.writeToFile("WRAPPER", "http://www.BDIOntology.com/schema/Bicycles");
    }
}
