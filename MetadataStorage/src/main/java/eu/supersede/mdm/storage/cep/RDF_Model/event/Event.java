package eu.supersede.mdm.storage.cep.RDF_Model.event;

import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.SimpleClause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by osboxes on 14/04/17.
 */
public class Event extends CEPElement {

    private EventSchema eventSchema;

    private List<SimpleClause> filters;

    private String alias;


    public Event(String IRI) {
        super(IRI);
        this.filters = new ArrayList<>();
    }


    public Event() {
        super();
        this.filters = new ArrayList<>();
    }

    public EventSchema getEventSchema() {
        return eventSchema;
    }

    public void setEventSchema(EventSchema eventSchema) {
        this.eventSchema = eventSchema;
    }

    public List<SimpleClause> getFilters() {
        return filters;
    }

    public void setFilters(List<SimpleClause> filters) {
        this.filters = filters;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                return eventSchema.getEventName() + " = " + eventSchema.getEventName();
            }
            default: {
                return eventSchema.getEventName() + " = " + eventSchema.getEventName();
            }
        }
    }

    @Override
    public String interpret(InterpreterContext context, Map<String, Object> props) throws InterpreterException {

        Boolean onlyAlias = props.containsKey("only_alias") ? (Boolean) props.get("only_alias") : false;
        if (onlyAlias) {
            switch (context) {
                case ESPER: {
                    return this.getAlias();
                }
                default: {
                    return this.getAlias();
                }
            }
        } else {
            switch (context) {
                case ESPER: {
                    return this.getAlias() + " = " + eventSchema.getEventName();
                }
                default: {
                    return this.getAlias() + " = " + eventSchema.getEventName();
                }
            }
        }
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        Map<String, String> map = new HashMap<>();
        switch (context) {
            case ESPER: {
                map.put("simple event", eventSchema.getEventName());
                return map;
            }
            default: {
                map.put("simple event", eventSchema.getEventName());
                return map;
            }
        }
    }
}