package eu.supersede.mdm.storage.cep.RDF_Model.event;


/**
 * Created by osboxes on 14/05/17.
 */
public enum AttributeType {
    TYPE_STRING,
    TYPE_DOUBLE,
    TYPE_BOOLEAN,
    TYPE_FLOAT,
    TYPE_BYTES,
    TYPE_LONG,
    TYPE_INTEGER;

    public String toString() {
        switch (this) {
            case TYPE_STRING: {
                return "string";
            }
            case TYPE_DOUBLE: {
                return "double";
            }
            case TYPE_BOOLEAN: {
                return "boolean";
            }
            case TYPE_FLOAT: {
                return "float";
            }
            case TYPE_BYTES: {
                return "bytes";
            }
            case TYPE_LONG: {
                return "long";
            }
            case TYPE_INTEGER: {
                return "int";
            }
            default: {
                return "error";
            }
        }
    }
}
