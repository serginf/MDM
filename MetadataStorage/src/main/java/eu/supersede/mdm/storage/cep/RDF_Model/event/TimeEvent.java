package eu.supersede.mdm.storage.cep.RDF_Model.event;

import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;

import java.util.HashMap;
import java.util.Map;

//import java.security.Timestamp;

/**
 * Created by osboxes on 23/05/17.
 */
public class TimeEvent extends CEPElement {

    String timestamp;

    public TimeEvent(String IRI, String timestamp) {
        super(IRI);
        this.timestamp = timestamp;
    }

    public TimeEvent(String IRI) {
        super(IRI);
    }
//
//    public TimeEvent( String timestamp) {
//        super();
//        this.timestamp = timestamp;
//    }

    public TimeEvent() {
        super();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                return timestamp.toString();
            }
            default: {
                return timestamp.toString();
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
                map.put("time event", timestamp.toString());
                return map;
            }
            default: {
                map.put("time event", timestamp.toString());
                return map;
            }

        }
    }
}
