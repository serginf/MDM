package eu.supersede.mdm.storage.cep.RDF_Model.event;

import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.Operand;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by osboxes on 14/05/17.
 */
public class Attribute extends Operand {

    private String name;
    private AttributeType attributeType;
    private EventSchema event;

    public Attribute() {
        super();
    }

    public Attribute(String name, AttributeType attributeType, EventSchema event) {
        super();
        this.name = name;
        this.event = event;
    }

    public Attribute(String IRI) {
        super(IRI);
    }

    public Attribute(String name, AttributeType attributeType, String IRI, EventSchema event) {
        super(IRI);
        this.name = name;
        this.event = event;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public EventSchema getEvent() {
        return event;
    }

    public void setEvent(EventSchema event) {
        this.event = event;
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                return event.interpret(context) + "." + name;
            }
            default: {
                return event.interpret(context) + "." + name;
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
                map.put("attribute", event.interpret(context) + "." + name);
                return map;
            }
            default: {
                map.put("attribute", event.interpret(context) + "." + name);
                return map;
            }
        }
    }
}
