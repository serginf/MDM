package eu.supersede.mdm.storage.cep.RDF_Model.event;

import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;
import eu.supersede.mdm.storage.cep.RDF_Model.Operators.LogicOperator;
import eu.supersede.mdm.storage.cep.RDF_Model.Operators.LogicOperatorEnum;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by osboxes on 15/05/17.
 */
public class LogicPattern extends Pattern {

    protected LogicOperator logicOperator;

    public LogicPattern() {
        super();
    }

    public LogicPattern(LogicOperator logicOperator, LinkedList<CEPElement> CEPElements) {
        super(CEPElements);
        this.logicOperator = logicOperator;
    }

    public LogicPattern(String IRI) {
        super(IRI);
    }

    public LogicPattern(LogicOperator logicOperator, LinkedList<CEPElement> CEPElements, String IRI) {
        super(CEPElements, IRI);
        this.logicOperator = logicOperator;
    }


    public LogicOperator getLogicOperator() {
        return logicOperator;
    }

    public void setLogicOperator(LogicOperator temporalOperator) {
        this.logicOperator = temporalOperator;
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        LinkedList<CEPElement> copy = new LinkedList<>(CEPElements);
        switch (context) {
            case ESPER: {
                if (logicOperator.getOperator().equals(LogicOperatorEnum.Conjunction) || logicOperator.getOperator().equals(LogicOperatorEnum.Disjunction)) {

                    CEPElement head = copy.pollFirst();
                    String logicalEvent = "(" + head.interpret(context) + ")";
                    head = copy.pollFirst();
                    while (head != null) {
                        logicalEvent += logicOperator.interpret(context);
                        logicalEvent += "(" + head.interpret(context) + ")";
                        head = copy.pollFirst();
                    }
                    return logicalEvent;
                }
                if (logicOperator.getOperator().equals(LogicOperatorEnum.Negation)) {
                    return logicOperator.interpret(context) + "(" + copy.pollFirst().interpret(context) + ")";
                }
                throw new InterpreterException("wrong logical operator");
            }
            default: {

                if (logicOperator.getOperator().equals(LogicOperatorEnum.Conjunction) || logicOperator.getOperator().equals(LogicOperatorEnum.Disjunction)) {
                    CEPElement head = copy.pollFirst();
                    String logicalEvent = "(" + head.interpret(context) + ")";
                    head = copy.pollFirst();
                    while (head != null) {
                        logicalEvent += logicOperator.interpret(context);
                        logicalEvent += "(" + head.interpret(context) + ")";
                        head = copy.pollFirst();
                    }
                    return logicalEvent;
                }
                if (logicOperator.getOperator().equals(LogicOperatorEnum.Negation)) {
                    return logicOperator.interpret(context) + " (" + copy.pollFirst().interpret(context) + ")";
                }
                throw new InterpreterException("wrong logical operator");
            }
        }
    }

    @Override
    public String interpret(InterpreterContext context, Map<String, Object> props) throws InterpreterException {
        return null;
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        Map<String, String> map = new HashMap<>();
        switch (context) {
            case ESPER: {
                map.put("complex logical event", interpret(context));
                return map;
            }
            default: {
                map.put("complex logical event", interpret(context));
                return map;
            }
        }
    }
}
