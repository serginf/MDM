package eu.supersede.mdm.storage.cep.RDF_Model.condition;

import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;

import java.util.*;

/**
 * Created by osboxes on 18/04/17.
 */
public class FunctionOperand extends Operand {

    private String functionName;
    private String functionURL;
    private Method functionMethod;
    private List<FunctionParameter> parameters;

    public FunctionOperand(String functionName, List<FunctionParameter> parameters, String functionURL, Method functionMethod) {
        super();
        this.functionName = functionName;
        this.functionURL = functionURL;
        this.functionMethod = functionMethod;
        this.parameters = parameters;
    }


    public FunctionOperand() {
        super();
        parameters = new ArrayList<>();
    }

    public FunctionOperand(String functionName, List<FunctionParameter> parameters, String functionURL, Method functionMethod, String IRI) {
        super(IRI);
        this.functionName = functionName;
        this.functionURL = functionURL;
        this.functionMethod = functionMethod;
        this.parameters = parameters;
    }


    public FunctionOperand(String IRI) {
        super(IRI);
        parameters = new ArrayList<>();
    }

    public List<FunctionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<FunctionParameter> innerOperand) {
        this.parameters = innerOperand;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionURL() {
        return functionURL;
    }

    public void setFunctionURL(String functionURL) {
        this.functionURL = functionURL;
    }

    public Method getFunctionMethod() {
        return functionMethod;
    }

    public void setFunctionMethod(Method functionMethod) {
        this.functionMethod = functionMethod;
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        Collections.sort(parameters);
        switch (context) {
            case ESPER: {
                String function = functionName + "(";
                for (FunctionParameter parameter : parameters) {
                    function += parameter.interpret(context) + ",";

                }
                function = function.substring(0, function.length() - 1) + ")";
                return function;
            }
            default: {
                String function = functionName + "(";
                for (FunctionParameter parameter : parameters) {
                    function += parameter.interpret(context) + ",";

                }
                function = function.substring(0, function.length() - 1) + ")";
                return function;
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
                String function = functionName + "(";
                for (FunctionParameter parameter : parameters) {
                    function += parameter.interpret(context) + ",";

                }
                function = function.substring(0, function.length() - 1) + ")";
                map.put("function", function);
                return map;
            }
            default: {
                String function = functionName + "(";
                for (FunctionParameter parameter : parameters) {
                    function += parameter.interpret(context) + ",";

                }
                function = function.substring(0, function.length() - 1) + ")";
                map.put("function", function);
                return map;
            }
        }
    }


    public enum Method {GET, POST}
}
