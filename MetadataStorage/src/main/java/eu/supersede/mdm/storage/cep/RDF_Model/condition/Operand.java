package eu.supersede.mdm.storage.cep.RDF_Model.condition;

import eu.supersede.mdm.storage.cep.Interpreter.Interpreter;

/**
 * Created by osboxes on 18/04/17.
 */
public abstract class Operand implements Interpreter {

    protected String IRI;

    protected OperandType operandType;

    public Operand(String IRI) {
        this.IRI = IRI;
        this.setOperandType(OperandType.other);
    }

    public Operand() {
        this.setOperandType(OperandType.other);
    }

    public String getIRI() {
        return IRI;
    }

    public void setIRI(String IRI) {
        this.IRI = IRI;
    }

    public OperandType getOperandType() {
        return operandType;
    }

    public void setOperandType(OperandType operandType) {
        this.operandType = operandType;
    }

    public enum OperandType {
        groupby,
        having,
        other
    }
}
