package eu.supersede.mdm.storage.cep.manager;

import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;
import eu.supersede.mdm.storage.cep.RDF_Model.Rule;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.Operand;
import eu.supersede.mdm.storage.cep.RDF_Model.event.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by osboxes on 31/05/17.
 */
public class FlumeSink {
    private FlumeChannel flumeChannel;
    private Rule rule;
    private String sinkName;
    private String agentName;
    private boolean restart;

    public FlumeSink() {
    }

    public FlumeChannel getFlumeChannel() {
        return flumeChannel;
    }

    public void setFlumeChannel(FlumeChannel flumeChannel) {
        this.flumeChannel = flumeChannel;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public String getSinkName() {
        return sinkName;
    }

    public void setSinkName(String sinkName) {
        this.sinkName = sinkName;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public boolean isRestart() {
        return restart;
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }

    public String interpret(String deletedRules) throws InterpreterException {
        String prefix = agentName + ManagerConstants.DOT + ManagerConstants.SINKS_PREFIX + sinkName + ManagerConstants.DOT;
        String result = "";
        if (deletedRules != "" || deletedRules != null) {
            result = String.join("\n", result
                    , prefix + ManagerConstants.CEP_SINK_DELETED_RULES + " = " + deletedRules
            );
        }

        result = String.join("\n", result
                , prefix + ManagerConstants.CEP_SINK_RULE_ID + " = " + rule.getIRI()
                , prefix + ManagerConstants.CEP_SINK_EXPRESSION + " = " + rule.interpret(InterpreterContext.ESPER)
                , prefix + ManagerConstants.TYPE + " = upc.edu.cep.flume.sinks.CEPSinkOldVersion"
                , prefix + ManagerConstants.CEP_SINK_RESTART + " = " + restart
                , prefix + ManagerConstants.CEP_SINK_CHANNEL + " = " + flumeChannel.getChannelName()
        );

        List<Event> simpleEvents = new ArrayList<>();
        Queue<CEPElement> CEPElementQueue = new ArrayDeque<>();
        CEPElementQueue.add(rule.getCEPElement());
        while (!CEPElementQueue.isEmpty()) {
            CEPElement CEPElement = CEPElementQueue.poll();
            if (CEPElement.getClass().equals(Event.class)) {
                simpleEvents.add((Event) CEPElement);
            } else if (!CEPElement.getClass().equals(TimeEvent.class)) {
                for (CEPElement e : ((Pattern) CEPElement).getCEPElements()) {
                    CEPElementQueue.add(e);
                }
            }
        }

        String events = "";
        String actions = "";
        for (Event e : simpleEvents) {
            events += " " + e.getEventSchema().getEventName();
        }
        for (Operand o : rule.getAction().getActionAttributes()) {
            actions += " " + o.interpret(InterpreterContext.ESPER);
        }
        result = String.join("\n", result
                , prefix + ManagerConstants.CEP_SINK_EVENT_NAMES + " = " + events
                , prefix + ManagerConstants.CEP_SINK_ACTIONS + " = " + actions
        );
        for (Event e : simpleEvents) {
            String eventAtts = "";
            for (Attribute attribute : e.getEventSchema().getAttributes()) {
                eventAtts += " " + attribute.getName();
            }
            eventAtts = eventAtts.trim();
            result = String.join("\n", result
                    , prefix + e.getEventSchema().getEventName() + ManagerConstants.DOT + ManagerConstants.CEP_SINK_EVENT_ATTRIBUTES + " = " + eventAtts
            );
            for (Attribute attribute : e.getEventSchema().getAttributes()) {
                result = String.join("\n", result
                        , prefix + e.getEventSchema().getEventName() + ManagerConstants.DOT + attribute.getName() + ManagerConstants.DOT + ManagerConstants.TYPE + " = " + attribute.getAttributeType().toString());
            }
        }

        return result;
    }
}