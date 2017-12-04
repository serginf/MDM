package eu.supersede.mdm.storage.cep.RDF_Model.condition;

import eu.supersede.mdm.storage.cep.Interpreter.Interpreter;

/**
 * Created by osboxes on 17/04/17.
 */
public abstract class Condition implements Interpreter {

    protected String IRI;

    public Condition(String IRI) {
        this.IRI = IRI;
    }

    public Condition() {
    }

    public String getIRI() {
        return IRI;
    }

    public void setIRI(String IRI) {
        this.IRI = IRI;
    }

    protected boolean hasGroupBy() {
        if (this.getClass().equals(ComplexPredicate.class)) {
            ComplexPredicate complexPredicate = (ComplexPredicate) this;
            for (Condition condition : complexPredicate.conditions) {
                if (condition.hasGroupBy()) {
                    return true;
                }
            }
        } else {
            SimpleClause simpleClause = (SimpleClause) this;
            if (simpleClause.getOperand1().getOperandType() == Operand.OperandType.groupby) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasHaving() {
        if (this.getClass().equals(ComplexPredicate.class)) {
            ComplexPredicate complexPredicate = (ComplexPredicate) this;
            for (Condition condition : complexPredicate.conditions) {
                if (condition.hasHaving()) {
                    return true;
                }
            }
        } else {
            SimpleClause simpleClause = (SimpleClause) this;
            if (simpleClause.getOperand1().getOperandType() == Operand.OperandType.having) {
                return true;
            }
            if (simpleClause.getOperand2() != null && simpleClause.getOperand2().getOperandType() == Operand.OperandType.having) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasWhere() {

        if (this.getClass().equals(ComplexPredicate.class)) {
            ComplexPredicate complexPredicate = (ComplexPredicate) this;
            for (Condition condition : complexPredicate.conditions) {
                if (condition.hasWhere()) {
                    return true;
                }
            }
        } else {
            SimpleClause simpleClause = (SimpleClause) this;
            if (simpleClause.getOperand1().getOperandType() == Operand.OperandType.other
                    && simpleClause.getOperand2() != null
                    && simpleClause.getOperand2().getOperandType() == Operand.OperandType.other) {
                return true;
            }
        }
        return false;

    }
}
