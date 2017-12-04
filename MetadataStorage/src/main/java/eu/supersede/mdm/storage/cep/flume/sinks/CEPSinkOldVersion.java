package eu.supersede.mdm.storage.cep.flume.sinks;

import com.espertech.esper.client.*;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventHelper;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**

 */
public class CEPSinkOldVersion extends AbstractSink implements Configurable {

    private static final Logger logger = LoggerFactory.getLogger(CEPSinkOldVersion.class);
    String rulesStatment;
    private EPServiceProvider epService;
    private String expression;
    private Map<String, SinkEvent> events;
    private EPStatement statement;
    private boolean restart;
    private String ruleID;
    private String monitor = "";
    private String[] action;

    public static Schema makeSchema(Map attributes, String eventName) {

        List<Schema.Field> fields = new ArrayList();
        Set<Map.Entry> attSet = attributes.entrySet();
        for (Map.Entry entry : attSet) {
            switch ((String) entry.getValue()) {
                case CEPSinkConstants.TYPE_BOOLEAN: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.BOOLEAN), null, null));
                    break;
                }
                case CEPSinkConstants.TYPE_STRING: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.STRING), null, null));
                    break;
                }
                case CEPSinkConstants.TYPE_DOUBLE: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.DOUBLE), null, null));
                    break;
                }
                case CEPSinkConstants.TYPE_FLOAT: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.FLOAT), null, null));
                    break;
                }
                case CEPSinkConstants.TYPE_BYTES: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.BYTES), null, null));
                    break;
                }
                case CEPSinkConstants.TYPE_LONG: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.LONG), null, null));
                    break;
                }
                case CEPSinkConstants.TYPE_INT: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.INT), null, null));
                    break;
                }
            }
        }

        Schema schema = Schema.createRecord(eventName, null, "upc.cep", false);
        schema.setFields(fields);

        return (schema);
    }

    @Override
    public synchronized void start() {
        super.start();

//        Configuration config = new Configuration();
//        config.addEventType("com.edu.cep.CEPElements.LogEvent",LogEvent.class.getName());
//        epService = EPServiceProviderManager.getDefaultProvider(config);

        //Creating a Statement
        //String expression = "select count(a) from pattern [every a=EventA where timer:within(2 sec)].win:time(2 hour)"; //time_batch

        if (restart) {
            if (epService.getEPAdministrator().getStatement(ruleID) == null) {
                monitor += "restart null";
                statement = epService.getEPAdministrator().createEPL(expression, ruleID);
                MyListener listener = new MyListener();
                statement.addListener(listener);
            } else {
                monitor += "restart not null";
                epService.getEPAdministrator().getStatement(ruleID).stop();
                while (!epService.getEPAdministrator().getStatement(ruleID).isStopped()) {
                }
                epService.getEPAdministrator().getStatement(ruleID).destroy();
                while (!epService.getEPAdministrator().getStatement(ruleID).isDestroyed()) {
                }
                statement = epService.getEPAdministrator().createEPL(expression, ruleID);
                MyListener listener = new MyListener();
                statement.addListener(listener);
            }
        } else {
            if (epService.getEPAdministrator().getStatement(ruleID) == null) {
                monitor += "no restart null";
                statement = epService.getEPAdministrator().createEPL(expression, ruleID);
                MyListener listener = new MyListener();
                statement.addListener(listener);
            } else {
                monitor += "no restart not null";
                this.stop();
            }
        }

//or test
        //EPStatement statement = epService.getEPAdministrator().createPattern("every (spike= EventA or error= Event2)");
//        String expression = "select * from pattern [every (spike= EventA or error= Event2)]"; //time_batch
//        EPStatement statement = epService.getEPAdministrator().createEPL(expression);

//and test
        //String expression = "select * from pattern [every (spike= EventA and error= Event2 where timer:within(10 sec))]"; //time_batch
        //EPStatement statement = epService.getEPAdministrator().createEPL(expression);

        // Adding a Listener


    }

    @Override
    public void configure(Context context) {
        // Configuration

        Configuration config = new Configuration();
        //config.addEventType("com.edu.cep.CEPElements.LogEvent",LogEvent.class.getName());
        rulesStatment = context.getString(CEPSinkConstants.DELETED_RULES);
        expression = context.getString(CEPSinkConstants.EXPRESSION);

        epService = EPServiceProviderManager.getDefaultProvider(config);
        events = new HashMap<>();
        String[] eventNames = context.getString(CEPSinkConstants.EVENT_NAME).trim().split(" ");
        action = context.getString(CEPSinkConstants.ACTIONS).trim().split(" ");
        for (String eventName : eventNames) {
            String eventAttributes = context.getString(eventName + "." + CEPSinkConstants.EVENT_ATTRIBUTES);
            SinkEvent sinkEvent = new SinkEvent();
            if (eventAttributes != null && !eventAttributes.isEmpty()) {
                String[] atts = eventAttributes.trim().split(" ");
                Map attributes = new HashMap<>();
                for (String attribute : atts) {
                    attributes.put(attribute, context.getString(eventName + "." + attribute + "." + CEPSinkConstants.ATTRIBUTE_TYPE));
                    System.out.println("att: **" + attribute + "*******************");
                    System.out.println("type: **" + attributes.get(attribute) + "*******************");
                    System.out.println("eventName: **" + eventName + "*******************");
                }
                restart = context.getBoolean(CEPSinkConstants.RESTART, false);
                ruleID = context.getString(CEPSinkConstants.RULE_ID);
                events.put(eventName, new SinkEvent(attributes, attributes.keySet(), makeSchema(attributes, eventName)));
                if (restart || !epService.getEPAdministrator().getConfiguration().isEventTypeExists(eventName)) {
                    ConfigurationEventTypeAvro avroEvent = new ConfigurationEventTypeAvro(events.get(eventName).getSchema());
                    epService.getEPAdministrator().getConfiguration().addEventTypeAvro(eventName, avroEvent);
                }
            }
        }
        if (rulesStatment != null) {
            String[] rules = rulesStatment.trim().split(" ");
            if (rules.length > 0) {
                deleteRules(rules);
            }
        }
    }

    private void deleteRules(String[] rules) {
        for (String rule : rules) {
            EPStatement statement = epService.getEPAdministrator().getStatement(rule);
            if (statement != null && !statement.isDestroyed()) {
                epService.getEPAdministrator().getStatement(rule).stop();

                epService.getEPAdministrator().getStatement(rule).destroy();
            }
        }
    }

    @Override
    public synchronized void stop() {
        super.stop();
        //epService.removeAllStatementStateListeners();
        //monitor = " old??";
//
//        epService.removeAllServiceStateListeners();
//        epService.removeAllStatementStateListeners();
        //       statement.destroy();
    }

    @Override
    public Status process() throws EventDeliveryException {

        Status status = Status.READY;
        Channel channel = getChannel();
        Transaction tx = null;
        try {
            System.out.println("begin " + channel.getName());
            tx = channel.getTransaction();
            tx.begin();

            Event event = channel.take();

            if (event != null) {
                System.out.println("rules are: " + event.getHeaders().get("Rules") + "in the channel: " + getChannel().getName());
                Map<String, String> headers = event.getHeaders();

                String line = EventHelper.dumpEvent(event);
                logger.debug(line);
                String eventName = headers.get("EventName");
                byte[] body = event.getBody();
                //System.out.println("body is " + new String(body));
                DatumReader<GenericRecord> reader = new SpecificDatumReader<GenericRecord>(events.get(eventName).getSchema());
                Decoder decoder = DecoderFactory.get().binaryDecoder(body, null);
                GenericRecord payload2 = null;
                payload2 = reader.read(null, decoder);
                //System.out.println("Message received : " + payload2.get("mylog"));
                //System.out.println("Message received : " + payload2.get("yourlog"));
                epService.getEPRuntime().sendEventAvro(payload2, eventName);
                /*System.out.println(headers.get("hostname"));
                System.out.println(Long.parseLong(headers.get("timestamp")));*/
                // Sending CEPElements
//                LogEvent cepEvent = new LogEvent();
//                cepEvent.setLog("w");
//                if (headers != null) {
//                    cepEvent.setHostname(headers.get("hostname"));
//                    cepEvent.setTimestamp(Long.parseLong(headers.get("timestamp")));
//                }
//
//                epService.getEPRuntime().sendEvent(cepEvent);

            } else {
                status = Status.BACKOFF;
            }

            tx.commit();
        } catch (Exception e) {
            System.out.println(e.toString());
            logger.error("can't process CEPElements, drop it!", e);
            if (tx != null) {
                tx.commit();// commit to drop bad event, otherwise it will enter dead loop.
            }

            throw new EventDeliveryException(e);
        } finally {
            if (tx != null) {
                tx.close();
            }
        }
        return status;
    }

    /**
     * Here are Biz codes.
     */
    public class MyListener implements UpdateListener {

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {
            try {
                if (newEvents == null) {

                    return;
                }
                EventBean event = newEvents[0];

                System.out.println("Count: " + event.get(action[0]) + monitor);
                System.out.println(statement.getName());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}