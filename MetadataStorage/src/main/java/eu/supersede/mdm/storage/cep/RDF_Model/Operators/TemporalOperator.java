package eu.supersede.mdm.storage.cep.RDF_Model.Operators;

/**
 * Created by osboxes on 15/05/17.
 */
public abstract class TemporalOperator extends Operator {

    private TemporalOperatorEnum operator;

    public TemporalOperator(TemporalOperatorEnum operator) {
        this.operator = operator;
    }

    public TemporalOperator() {
    }

    public TemporalOperator(TemporalOperatorEnum operator, String IRI) {
        super(IRI);
        this.operator = operator;
    }

    public TemporalOperator(String IRI) {
        super(IRI);
    }

    public TemporalOperatorEnum getOperator() {
        return operator;
    }

    public void setOperator(TemporalOperatorEnum operator) {
        this.operator = operator;
    }
}
