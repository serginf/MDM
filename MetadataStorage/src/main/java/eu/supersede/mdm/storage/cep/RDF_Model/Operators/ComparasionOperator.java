package eu.supersede.mdm.storage.cep.RDF_Model.Operators;


import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;

import java.util.Map;

/**
 * Created by osboxes on 18/04/17.
 */
public class ComparasionOperator extends Operator {

    private ComparasionOperatorEnum operator;

    public ComparasionOperator(ComparasionOperatorEnum operator) {
        super();
        this.operator = operator;
    }

    public ComparasionOperator(ComparasionOperatorEnum operator, String IRI) {
        super(IRI);
        this.operator = operator;
    }

    public ComparasionOperatorEnum getOperator() {
        return operator;
    }

    public void setOperator(ComparasionOperatorEnum operator) {
        this.operator = operator;
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                switch (operator) {
                    case EQ: {
                        return "=";
                    }
                    case NE: {
                        return "!=";
                    }
                    case GE: {
                        return ">=";
                    }
                    case GT: {
                        return ">";
                    }
                    case LE: {
                        return "<=";
                    }
                    case LT: {
                        return "<";
                    }
                }
            }
            default: {
                switch (operator) {
                    case EQ: {
                        return "=";
                    }
                    case NE: {
                        return "!=";
                    }
                    case GE: {
                        return ">=";
                    }
                    case GT: {
                        return ">";
                    }
                    case LE: {
                        return "<=";
                    }
                    case LT: {
                        return "<";
                    }
                }
            }
        }
        throw new InterpreterException("not supported");
    }

    @Override
    public String interpret(InterpreterContext context, Map<String, Object> props) throws InterpreterException {
        return null;
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        throw new InterpreterException("not supported");
    }

    public String toString() {
        switch (operator) {
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






