package eu.supersede.mdm.storage.service.impl.model;

public class LavObj{
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

    public String getLAVMappingID() {
        return LAVMappingID;
    }

    public String getWrapperIRI() {
        return wrapperIRI;
    }

    public String getDataSourceIRI() {
        return dataSourceIRI;
    }
}