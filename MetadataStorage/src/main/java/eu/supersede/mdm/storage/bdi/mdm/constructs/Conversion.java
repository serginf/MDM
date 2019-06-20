package eu.supersede.mdm.storage.bdi.mdm.constructs;

import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.resources.bdi.SchemaIntegrationHelper;
import eu.supersede.mdm.storage.util.RDFUtil;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.util.logging.Logger;

/**
 * Created by Kashif-Rabbani in June 2019
 */
public class Conversion {
    private static final Logger LOGGER = Logger.getLogger(Conversion.class.getName());
    //private JSONObject wrapper = new JSONObject();
    private JSONObject globalGraphInfo = new JSONObject();
    //private String postWrapperUrl = ConfigManager.getProperty("metadata_data_storage_url") + "wrapper/";
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
        LOGGER.info("Creating MDM Global Graph");
        MDMGlobalGraph mdmGlobalGraph = new MDMGlobalGraph(globalGraphInfo.getAsString("name"), globalGraphInfo.getAsString("schema_iri"), mdmGlobalGraphIri); /*schema_iri is IRI (namedGraph) of the BDI graph which need to be converted into MDM graph*/
        LOGGER.info("Creating MDM Wrappers");
        new MDMWrapper(globalGraphInfo, mdmGlobalGraphIri);
        LOGGER.info("Creating MDM LAV Mappings including subgraphs covering");
        new MDMLavMapping(mdmGlobalGraphIri, mdmGlobalGraph.getNodesIds());
    }

    /**
     * This method is for testing
     */
    private void seeTheTriplesOfNamedGraph() {
        String getClassProperties = " SELECT * WHERE { GRAPH <" + mdmGlobalGraphIri + "> { ?s ?p ?o .}} ";
        RDFUtil.runAQuery(RDFUtil.sparqlQueryPrefixes + getClassProperties, mdmGlobalGraphIri).forEachRemaining(triple -> {
            //System.out.print(triple.get("s") + "\t" + triple.get("p") + "\t" + triple.get("o") + "\n");
        });
    }

}
