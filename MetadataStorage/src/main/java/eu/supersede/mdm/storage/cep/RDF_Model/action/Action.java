package eu.supersede.mdm.storage.cep.RDF_Model.action;

import eu.supersede.mdm.storage.cep.Interpreter.Interpreter;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.Operand;
import eu.supersede.mdm.storage.cep.RDF_Model.event.Event;

import java.util.*;

/**
 * Created by osboxes on 17/04/17.
 */
public class Action implements Interpreter {

    private Event associatedEvent;
    private String assocociatedEventTopic;
    private List<Operand> actionAttributes;
    private ActionType actionType;
    private String serviceURL;
    private List<String> topicNames;
    private String IRI;

    public Action(Event associatedEvent, String associatedEventTopic, List<Operand> actionAttributes, ActionType actionType, String serviceURL, List<String> topicNames, String IRI) {
        this.associatedEvent = associatedEvent;
        this.assocociatedEventTopic = associatedEventTopic;
        this.actionAttributes = actionAttributes;
        this.actionType = actionType;
        this.serviceURL = serviceURL;
        this.topicNames = topicNames;
        this.IRI = IRI;
    }

    public Action() {
        this.actionAttributes = new ArrayList<>();
        this.topicNames = new ArrayList<String>();
    }

    public String getIRI() {
        return IRI;
    }

    public void setIRI(String IRI) {
        this.IRI = IRI;
    }

    public Event getAssociatedEvent() {
        return associatedEvent;
    }

    public void setAssociatedEvent(Event associatedEvent) {
        this.associatedEvent = associatedEvent;
    }

    public String getAssocociatedEventTopic() {
        return assocociatedEventTopic;
    }

    public void setAssocociatedEventTopic(String assocociatedEventTopic) {
        this.assocociatedEventTopic = assocociatedEventTopic;
    }

    public List<Operand> getActionAttributes() {
        return actionAttributes;
    }

    public void setActionAttributes(List<Operand> actionAttributes) {
        this.actionAttributes = actionAttributes;
    }

    public void addActionAttribute(Operand operand) {
        actionAttributes.add(operand);
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public List<String> getTopicNames() {
        return topicNames;
    }

    public void setTopicNames(String[] topicNames) {
        this.topicNames = new ArrayList<String>();
        Collections.addAll(this.topicNames, topicNames);
    }

    public void setTopicNames(List<String> topicNames) {
        this.topicNames = topicNames;
    }

    public void addTopics(String[] topicNames) {
        //this.topicNames= new ArrayList<String>();
        Collections.addAll(this.topicNames, topicNames);
    }

    public void addTopic(String topicName) {
        //this.topicNames= new ArrayList<String>();
        Collections.addAll(this.topicNames, topicName);
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        String action = "";
        switch (context) {
            case ESPER: {
                for (Operand attribute : actionAttributes) {
                    action += attribute.interpret(context);
                    action += ", ";
                }
                if (actionAttributes.size() > 0) {
                    action = action.substring(0, action.lastIndexOf(','));
                }
                return action;
            }
            default: {
                for (Operand attribute : actionAttributes) {
                    action += attribute.interpret(context);
                    action += ", ";
                }
                if (actionAttributes.size() > 0) {
                    action = action.substring(0, action.lastIndexOf(','));
                }
                return action;
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
                map.put("action", this.interpret(context));
                return map;
            }
            default: {

                map.put("action", this.interpret(context));
                return map;
            }

        }
    }

    public enum ActionType {
        GET,
        POST,
        KAFKA,
        GET_AND_KAFKA,
        POST_AND_KAFKA
    }
}
