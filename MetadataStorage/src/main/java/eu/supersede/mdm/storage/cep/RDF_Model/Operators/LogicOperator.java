package eu.supersede.mdm.storage.cep.RDF_Model.Operators;

import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;
import java.util.Map;

/**
 * Created by osboxes on 15/05/17.
 */
public class LogicOperator extends Operator {

    private LogicOperatorEnum operator;

    public LogicOperator(LogicOperatorEnum operator) {
        super();
        this.operator = operator;
    }

    public LogicOperator(LogicOperatorEnum operator, String IRI) {
        super(IRI);
        this.operator = operator;
    }

    public LogicOperatorEnum getOperator() {
        return operator;
    }

    public void setOperator(LogicOperatorEnum operator) {
        this.operator = operator;
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                switch (operator) {
                    case Conjunction: {
                        return "AND";
                    }
                    case Disjunction: {
                        return "OR";
                    }
                    case Negation: {
                        return "NOT";
                    }
                }
            }
            default: {
                switch (operator) {
                    case Conjunction: {
                        return "AND";
                    }
                    case Disjunction: {
                        return "OR";
                    }
                    case Negation: {
                        return "NOT";
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
}
