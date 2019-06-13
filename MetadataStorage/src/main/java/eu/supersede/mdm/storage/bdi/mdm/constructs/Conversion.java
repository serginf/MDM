package eu.supersede.mdm.storage.bdi.mdm.constructs;

import eu.supersede.mdm.storage.model.Namespaces;
import eu.supersede.mdm.storage.resources.bdi.SchemaIntegrationHelper;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class Conversion {
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
        new MDMGlobalGraph(globalGraphInfo.getAsString("name"), globalGraphInfo.getAsString("schema_iri"), mdmGlobalGraphIri); /*schema_iri is IRI (namedGraph) of the BDI graph which need to be converted into MDM graph*/

        new MDMWrapper(globalGraphInfo, mdmGlobalGraphIri);

        new MDMLavMapping(mdmGlobalGraphIri);
    }
}
