package eu.supersede.mdm.storage.cep.RDF_Model.condition;

import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;
import eu.supersede.mdm.storage.cep.RDF_Model.event.Attribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by osboxes on 25/05/17.
 */
public class Min extends FunctionOperand {

    public Min(String functionIRI, String parameterIRI, Attribute attribute) {
        this.setFunctionName("min");
        FunctionParameter parameter = new FunctionParameter(attribute, 0, parameterIRI);
        List<FunctionParameter> parameterSingleton = Collections.singletonList(parameter);

        this.setParameters(parameterSingleton);
        this.setFunctionMethod(null);
        this.setFunctionURL("");
        this.setIRI(functionIRI);
        this.setOperandType(OperandType.having);
    }

    public Min(FunctionParameter attribute) {
        this.setFunctionName("min");
        List<FunctionParameter> parameterSingleton = Collections.singletonList(attribute);
        this.setParameters(parameterSingleton);
        this.setOperandType(OperandType.having);
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                return "min(" + this.getParameters().get(0).interpret(context) + ")";
            }
            default: {
                return "min(" + this.getParameters().get(0).interpret(context) + ")";
            }
        }
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        Map<String, String> map = new HashMap<>();
        switch (context) {
            case ESPER: {

                map.put("min", "min(" + this.getParameters().get(0).interpret(context) + ")");
                return map;
            }
            default: {

                map.put("min", "min(" + this.getParameters().get(0).interpret(context) + ")");
                return map;
            }
        }
    }
}
