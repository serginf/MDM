package eu.supersede.mdm.storage.cep.flume.interceptors;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.io.IOException;
import java.util.*;

public class DistributedInterceptor implements Interceptor {

    DatumReader<GenericRecord> reader;
    Decoder decoder;
    private String eventName;
    private Map<String, String> attributes;
    private Set<String> attributeNames;
    private Map<String, List<Attribute>> rulesMap;
    private Schema schema;


    public DistributedInterceptor(String eventName, Schema schema, Map<String, List<Attribute>> rulesMap) {
        this.eventName = eventName;
        this.schema = schema;
        this.rulesMap = rulesMap;
    }

    @Override
    public void close() {

    }

    @Override
    public void initialize() {
        reader = new SpecificDatumReader<GenericRecord>(schema);
    }

    @Override
    public Event intercept(Event event) {
        event.getHeaders().put("EventName", eventName);

        decoder = DecoderFactory.get().binaryDecoder(event.getBody(), null);
        GenericRecord payload2 = null;
        try {
            payload2 = reader.read(null, decoder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> rules = new ArrayList<>();

        for (String rule : this.rulesMap.keySet()) {
            boolean possible = true;
            for (Attribute attribute : this.rulesMap.get(rule)) {
                for (String key : attribute.getOperations().keySet()) {

                    String opt1 = attribute.getOperations().get(key);
                    String opt2 = payload2.get(attribute.getAttName()).toString();
                    String type = attribute.getType();


                    if (DIutils.compare(opt1, attribute.getType(), opt2, key)) {
                        possible = false;
                    }
                }
            }
            if (possible) {
                rules.add(rule);
            }
        }

        if (!rules.isEmpty()) {
            String rulesString = "";
            for (String rule : rules) {
                rulesString += rule + ";";
            }
            rulesString = rulesString.substring(0, rulesString.length() - 1);
            event.getHeaders().put("Rules", rulesString);
            return event;
        }
        return null;
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        for (Iterator<Event> iterator = events.iterator(); iterator.hasNext(); ) {
            Event next = intercept(iterator.next());
            if (next == null) {
                // remove the element
                iterator.remove();
            }
        }
        return events;
    }

    public static class Builder implements Interceptor.Builder {
        List<String> rules = new ArrayList<>();
        private String eventName;
        private Map<String, String> attributes;
        private Map<String, List<Attribute>> rulesMap;
        private String ruleNames;

        public static Schema makeSchema(Map attributes, String eventName) {

            List<Schema.Field> fields = new ArrayList();
            Set<Map.Entry> attSet = attributes.entrySet();
            for (Map.Entry entry : attSet) {
                switch ((String) entry.getValue()) {
                    case DistributedInterceptorConstants.TYPE_INT: {
                        fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.INT), null, null));
                        break;
                    }
                    case DistributedInterceptorConstants.TYPE_BOOLEAN: {
                        fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.BOOLEAN), null, null));
                        break;
                    }
                    case DistributedInterceptorConstants.TYPE_STRING: {
                        fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.STRING), null, null));
                        break;
                    }
                    case DistributedInterceptorConstants.TYPE_DOUBLE: {
                        fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.DOUBLE), null, null));
                        break;
                    }
                    case DistributedInterceptorConstants.TYPE_FLOAT: {
                        fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.FLOAT), null, null));
                        break;
                    }
                    case DistributedInterceptorConstants.TYPE_BYTES: {
                        fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.BYTES), null, null));
                        break;
                    }
                    case DistributedInterceptorConstants.TYPE_LONG: {
                        fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.LONG), null, null));
                        break;
                    }
                }
            }

            Schema schema = Schema.createRecord(eventName, null, "upc.cep", false);
            schema.setFields(fields);

            return (schema);
        }

        @Override
        public void configure(Context context) {


            eventName = context.getString("eventName");

            String eventAttributes = context.getString(DistributedInterceptorConstants.EVENT_ATTRIBUTES);
            if (eventAttributes != null && !eventAttributes.isEmpty()) {
                String[] atts = eventAttributes.trim().split(" ");
                attributes = new HashMap<>();
                for (String attribute : atts) {
                    attributes.put(attribute, context.getString(attribute + "." + DistributedInterceptorConstants.ATTRIBUTE_TYPE));
                }
            }


            this.ruleNames = context.getString(DistributedInterceptorConstants.RULES);

            if (this.ruleNames != null && this.ruleNames != "") {
                String[] temp = ruleNames.split(";");
                for (String r : temp) {
                    r = r.trim();
                    if (r != "") {
                        rules.add(r);
                    }
                }
            }

            if (rules.size() > 0) {
                rulesMap = new HashMap<>();


                for (String rule : rules) {
                    List<String> attributesList = new ArrayList<String>();
                    rulesMap.put(rule, new ArrayList<>());
                    String atts = context.getString(rule);
                    if (atts != null && atts != "") {
                        Collections.addAll(attributesList, atts.split(";"));
                        for (String attributeName : attributesList) {
                            Attribute attribute = new Attribute();
                            attribute.setAttName(attributeName);
                            attribute.setOperations(context.getSubProperties(rule + "." + attributeName + "."));
                            attribute.setType(attributes.get(attributeName));
                            rulesMap.get(rule).add(attribute);
                        }
                    }
                }
            }
        }

        @Override
        public Interceptor build() {
            return new DistributedInterceptor(eventName, makeSchema(attributes, eventName), rulesMap);
        }
    }


}