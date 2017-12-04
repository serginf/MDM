package eu.supersede.mdm.storage.cep.manager;

import eu.supersede.mdm.storage.cep.RDF_Model.Rule;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.LiteralOperand;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.SimpleClause;
import eu.supersede.mdm.storage.cep.RDF_Model.event.*;

import java.util.*;

/**
 * Created by osboxes on 31/05/17.
 */
public class FlumeStream {

    private final String channelName = "memoryChannel";
    private final String sinkName = "avro-sink";
    EventSchema event;
    //    EventSchema atomicEvent;
    private String kafkaBootstrap;
    private String streamType;
    private String agentName;
    private String sourceName;

    public FlumeStream() {
    }

    public String getKafkaBootstrap() {
        return kafkaBootstrap;
    }

    public void setKafkaBootstrap(String kafkaBootstrap) {
        this.kafkaBootstrap = kafkaBootstrap;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public EventSchema getEvent() {
        return event;
    }

    public void setEvent(EventSchema event) {
        this.event = event;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String interpret(String topic, List<Rule> rules) {

        String result = "##### INIT #####\n";

        result = String.join("\n", result
                , agentName + ManagerConstants.DOT + ManagerConstants.SOURCES + " = " + sourceName
                , agentName + ManagerConstants.DOT + ManagerConstants.CHANNELS + " = " + channelName
                , agentName + ManagerConstants.DOT + ManagerConstants.SINKS + " = " + sinkName
        );

        String sourcePrefix = agentName + ManagerConstants.DOT + ManagerConstants.SOURCES + ManagerConstants.DOT + sourceName + ManagerConstants.DOT;
        String channelPrefix = agentName + ManagerConstants.DOT + ManagerConstants.CHANNELS + ManagerConstants.DOT + channelName + ManagerConstants.DOT;
        String sinkPrefix = agentName + ManagerConstants.DOT + ManagerConstants.SOURCES + ManagerConstants.DOT + sinkName + ManagerConstants.DOT;


        String eventAtts = "";
        for (Attribute attribute : event.getAttributes()) {
            eventAtts += " " + attribute.getName();
        }

        result = String.join("\n", result, "\n\n##### SOURCES #####\n");

        eventAtts = eventAtts.trim();
        result = String.join("\n", result
                , sourcePrefix + ManagerConstants.TYPE + " = upc.edu.cep.flume.sources.CEPKafkaSource"
                , sourcePrefix + ManagerConstants.SOURCE_EVENT_TYPE + " = " + ManagerConstants.SOURCE_EVENT_TYPE_JSON
                , sourcePrefix + ManagerConstants.SOURCE_KAFKA_BOOTSTRAP + " = " + kafkaBootstrap
                , sourcePrefix + ManagerConstants.SOURCE_TOPIC + " = " + topic
                , sourcePrefix + ManagerConstants.SOURCE_BATCH_SIZE + " = 100"
                , sourcePrefix + ManagerConstants.SOURCE_EVENT_NAME + " = " + event.getEventName()
                , sourcePrefix + ManagerConstants.SOURCE_ATTRIBUTES + " = " + eventAtts
        );
        for (Attribute attribute : event.getAttributes()) {
            result = String.join("\n", result
                    , sourcePrefix + attribute.getName() + ManagerConstants.DOT + ManagerConstants.TYPE + " = " + attribute.getAttributeType().toString());
        }


        result = String.join("\n", result

                , sourcePrefix + ManagerConstants.INTERCEPTORS + " = " + "TimestampInterceptor HostInterceptor DistributedInterceptor"
                , sourcePrefix + ManagerConstants.TIMESTAMP_INTERCEPTOR_TYPE + " = " + ManagerConstants.TIMESTAMP_INTERCEPTOR_TYPE_INSTANCE
                , sourcePrefix + ManagerConstants.HOST_INTERCEPTOR_TYPE + " = " + ManagerConstants.HOST_INTERCEPTOR_TYPE_INSTANCE
                , sourcePrefix + ManagerConstants.HOST_INTERCEPTOR_PRESERVEEXISTING + " = " + ManagerConstants.HOST_INTERCEPTOR_PRESERVEEXISTING_INSTANCE
                , sourcePrefix + ManagerConstants.HOST_INTERCEPTOR_HOSTHEADER + " = " + ManagerConstants.HOST_INTERCEPTOR_HOSTHEADER_INSTANCE
                , sourcePrefix + ManagerConstants.Distributed_INTERCEPTOR_EVENTNAME + " = " + event.getEventName()
                , sourcePrefix + ManagerConstants.Distributed_INTERCEPTOR_TYPE + " = " + ManagerConstants.Distributed_INTERCEPTOR_TYPE_INSTANCE
        );

        result = String.join("\n", result
                , sourcePrefix + ManagerConstants.CHANNELS + " = " + channelName
        );


        Map<Rule, Event> eventsWithFilters = new HashMap<>();
        Map<Rule, Event> allEvents = new HashMap<>();
        String rulesWithFilters = "";
        String allRules = "";
        boolean filter = false;
        for (Rule rule : rules) {
            Queue<CEPElement> CEPElementQueue = new ArrayDeque<>();
            CEPElementQueue.add(rule.getCEPElement());
            while (!CEPElementQueue.isEmpty()) {
                CEPElement head = CEPElementQueue.poll();
                if (head.getClass().equals(Event.class)) {
                    Event e = (Event) head;
                    if (e.getEventSchema().getIRI().equals(event.getIRI())) {
                        if (!e.getFilters().isEmpty()) {
                            eventsWithFilters.put(rule, e);
                            rulesWithFilters += ";" + rule.getIRI();
                            allEvents.put(rule, e);
                            allRules += ";" + rule.getIRI();
                        } else {
                            allEvents.put(rule, e);
                            allRules += ";" + rule.getIRI();
                        }
                    }
                } else if (!event.getClass().equals(TimeEvent.class)) {
                    for (CEPElement e : ((Pattern) head).getCEPElements()) {
                        CEPElementQueue.add(e);
                    }
                }
            }
        }

        if (rulesWithFilters.length() > 0) {
            rulesWithFilters = rulesWithFilters.substring(1);
        }
        if (allRules.length() > 0) {
            allRules = allRules.substring(1);
        }

        result = String.join("\n", result
                , sourcePrefix + ManagerConstants.Distributed_INTERCEPTOR_PREFIX + "rules" + " = " + allRules
        );

        for (Rule rule : eventsWithFilters.keySet()) {
            Set<String> attributes = new HashSet<>();
            for (SimpleClause sc : eventsWithFilters.get(rule).getFilters()) {
                LiteralOperand lo;
                Attribute a;
                if (sc.getOperand1().getClass().equals(LiteralOperand.class)) {
                    lo = (LiteralOperand) sc.getOperand1();
                    a = (Attribute) sc.getOperand2();
                } else {
                    lo = (LiteralOperand) sc.getOperand2();
                    a = (Attribute) sc.getOperand1();
                }
                attributes.add(a.getName());
                result = String.join("\n", result
                        , sourcePrefix + ManagerConstants.Distributed_INTERCEPTOR_PREFIX + rule.getIRI() + " = " + a.getName()
                        , sourcePrefix + ManagerConstants.Distributed_INTERCEPTOR_PREFIX + rule.getIRI() + ManagerConstants.DOT + a.getName() + ManagerConstants.DOT + sc.getOperator().toString() + " = " + lo.getValue()
                );
            }

            String attributesString = "";
            for (String attribute : attributes) {
                attributesString += ";" + attribute;
            }
            attributesString = attributesString.substring(1);

            result = String.join("\n", result
                    , sourcePrefix + ManagerConstants.Distributed_INTERCEPTOR_PREFIX + "attributes" + " = " + attributesString
            );
        }


//        result = String.join("\n", result
//                , sourcePrefix + ManagerConstants.CHANNELS + "=" + channelName
//        );


        result = String.join("\n", result
                , sourcePrefix + ManagerConstants.SELECTOR_TYPE + " = " + "upc.edu.cep.flume.selectors.DCEPFilterSelector"
                , sourcePrefix + ManagerConstants.SELECTOR_CHANNELS + " = " + channelName
                , sourcePrefix + ManagerConstants.SELECTOR_PREFIX + channelName + ManagerConstants.DOT + "rules" + " = " + allRules
        );

        result = String.join("\n", result, "\n\n##### CHANNELS #####\n");

        result = String.join("\n", result
                , channelPrefix + ManagerConstants.TYPE + "=" + ManagerConstants.CHANNEL_TPYE_MEMORY
                , channelPrefix + ManagerConstants.CHANNEL_CAPACITY + "= 100000"
                , channelPrefix + ManagerConstants.CHANNEL_TRANSACTION_CAPACITY + "= 5000"
                , channelPrefix + ManagerConstants.CHANNEL_KEEP_ALIVE + "= 3"
        );

        result = String.join("\n", result, "\n\n##### SINKS #####\n");

        result = String.join("\n", result
                , sinkPrefix + ManagerConstants.TYPE + "=" + "avro"
                , sinkPrefix + "hostname" + "= localhost"
                , sinkPrefix + "port" + "= 22224"
                , sinkPrefix + "channel" + "= memoryChannel"
                , sinkPrefix + "trust-all-certs" + "= true"
                , sinkPrefix + "batch-size" + "= 500"
        );

        return result;
    }
}