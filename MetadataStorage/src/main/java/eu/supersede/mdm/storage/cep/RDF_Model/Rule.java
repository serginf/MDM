package eu.supersede.mdm.storage.cep.RDF_Model;

import eu.supersede.mdm.storage.cep.Interpreter.Interpreter;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;
import eu.supersede.mdm.storage.cep.RDF_Model.action.Action;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.Condition;
import eu.supersede.mdm.storage.cep.RDF_Model.event.CEPElement;
import eu.supersede.mdm.storage.cep.RDF_Model.window.Window;
import eu.supersede.mdm.storage.cep.RDF_Model.event.CEPElement;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by osboxes on 20/04/17.
 */
public class Rule implements Interpreter {

    String IRI;
    Condition condition;
    CEPElement CEPElement;
    Action action;
    Window window;

    public Rule() {
    }

    public Rule(Condition condition, CEPElement CEPElement, Action action, Window window, String IRI) {
        this.IRI = IRI;
        this.condition = condition;
        this.CEPElement = CEPElement;
        this.action = action;
        this.window = window;
    }

    public Condition getCondition() {
        return this.condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public CEPElement getCEPElement() {
        return CEPElement;
    }

    public void setCEPElement(CEPElement CEPElement) {
        this.CEPElement = CEPElement;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Window getWindow() {
        return window;
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    public String getIRI() {
        return IRI;
    }

    public void setIRI(String IRI) {
        this.IRI = IRI;
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                String rule = "select ";
                if (action != null) {
                    rule += action.interpret(context);
                } else {
                    rule += "*";
                }
                if (CEPElement != null)
                    rule += " from pattern [every " + CEPElement.interpret(context) + "]";
                if (window != null)
                    rule += window.interpret(context) + " ";
                if (condition != null) {
                    Map<String, String> cons = condition.interpretToMap(context);
                    if (cons.get("where") != null) {
                        rule += "where " + cons.get("where") + " ";
                    }
                    if (cons.get("group by") != null) {
                        rule += "group by " + cons.get("group by") + " ";
                    }
                    if (cons.get("having") != null) {
                        rule += "having " + cons.get("having") + " ";
                    }
                }
                return rule;
            }
        }
        return null;
    }

    @Override
    public String interpret(InterpreterContext context, Map<String, Object> props) throws InterpreterException {
        return null;
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        Map<String, String> rule = new HashMap<>();
        switch (context) {
            case ESPER: {
                rule.put("rule", interpret(context));
                break;
            }
        }
        return rule;
    }
}
