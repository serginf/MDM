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
public class GroupBy extends FunctionOperand {

    public GroupBy(String functionIRI, String parameterIRI, Attribute attribute) {
        this.setFunctionName("group by");
        FunctionParameter parameter = new FunctionParameter(attribute, 0, parameterIRI);
        List<FunctionParameter> parameterSingleton = Collections.singletonList(parameter);

        this.setParameters(parameterSingleton);
        this.setFunctionMethod(null);
        this.setFunctionURL("");
        this.setIRI(functionIRI);
        this.setOperandType(OperandType.groupby);
    }

    public GroupBy(FunctionParameter attribute) {
        this.setFunctionName("group by");
        List<FunctionParameter> parameterSingleton = Collections.singletonList(attribute);
        this.setParameters(parameterSingleton);
        this.setOperandType(OperandType.groupby);
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                return this.getParameters().get(0).interpret(context);
            }
            default: {
                return this.getParameters().get(0).interpret(context);
            }
        }
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        Map<String, String> map = new HashMap<>();
        switch (context) {
            case ESPER: {

                map.put("group by", this.getParameters().get(0).interpret(context));
                return map;
            }
            default: {

                map.put("group by", this.getParameters().get(0).interpret(context));
                return map;
            }
        }
    }
}
