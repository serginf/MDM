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
public class Sum extends FunctionOperand {

    public Sum(String functionIRI, String parameterIRI, Attribute attribute) {
        this.setFunctionName("sum");
        FunctionParameter parameter = new FunctionParameter(attribute, 0, parameterIRI);
        List<FunctionParameter> parameterSingleton = Collections.singletonList(parameter);

        this.setParameters(parameterSingleton);
        this.setFunctionMethod(null);
        this.setFunctionURL("");
        this.setIRI(functionIRI);
        this.setOperandType(OperandType.having);
    }

    public Sum(FunctionParameter attribute) {
        this.setFunctionName("sum");
        List<FunctionParameter> parameterSingleton = Collections.singletonList(attribute);
        this.setParameters(parameterSingleton);
        this.setOperandType(OperandType.having);
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        Map<String, String> map = new HashMap<>();
        switch (context) {
            case ESPER: {

                map.put("sum", "sum(" + this.getParameters().get(0).interpret(context) + ")");
                return map;
            }
            default: {

                map.put("sum", "sum(" + this.getParameters().get(0).interpret(context) + ")");
                return map;
            }
        }
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                return "sum(" + this.getParameters().get(0).interpret(context) + ")";
            }
            default: {
                return "sum(" + this.getParameters().get(0).interpret(context) + ")";
            }
        }
    }
}
