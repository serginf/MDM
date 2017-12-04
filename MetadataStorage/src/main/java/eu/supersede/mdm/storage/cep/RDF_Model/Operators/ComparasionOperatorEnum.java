package eu.supersede.mdm.storage.cep.RDF_Model.Operators;

/**
 * Created by osboxes on 15/05/17.
 */
public enum ComparasionOperatorEnum {
    EQ,
    NE,
    GT,
    GE,
    LE,
    LT;

    public String toString() {
        switch (this) {
            case EQ: {
                return "eq";
            }
            case NE: {
                return "ne";
            }
            case GT: {
                return "gt";
            }
            case GE: {
                return "ge";
            }
            case LE: {
                return "le";
            }
            case LT: {
                return "lt";
            }
            default: {
                return "error";
            }
        }
    }
}
