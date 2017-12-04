package eu.supersede.mdm.storage.cep.flume.selectors;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.channel.AbstractChannelSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class CEPFilterSelector extends AbstractChannelSelector {

    public static final String CONFIG_CHANNELS = "channels";

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory
            .getLogger(CEPFilterSelector.class);

    private static final List<Channel> EMPTY_LIST =
            Collections.emptyList();

    private String channelNames;

    private Map<String, List<Channel>> channelMapping;


    private Map<Channel, List<Attribute>> channels;

    private List<Channel> defaultChannels;

    private String eventName;
    private Map<String, String> attributes;
    private Set<String> attributeNames;
    private Schema schema;

    public static Schema makeSchema(Map attributes, String eventName) {

        List<Schema.Field> fields = new ArrayList();
        Set<Map.Entry> attSet = attributes.entrySet();
        for (Map.Entry entry : attSet) {
            switch ((String) entry.getValue()) {
                case CEPSelectorConstants.TYPE_INT: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.INT), null, null));
                    break;
                }
                case CEPSelectorConstants.TYPE_BOOLEAN: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.BOOLEAN), null, null));
                    break;
                }
                case CEPSelectorConstants.TYPE_STRING: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.STRING), null, null));
                    break;
                }
                case CEPSelectorConstants.TYPE_DOUBLE: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.DOUBLE), null, null));
                    break;
                }
                case CEPSelectorConstants.TYPE_FLOAT: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.FLOAT), null, null));
                    break;
                }
                case CEPSelectorConstants.TYPE_BYTES: {
                    fields.add(new Schema.Field((String) entry.getKey(), Schema.create(Schema.Type.BYTES), null, null));
                    break;
                }
                case CEPSelectorConstants.TYPE_LONG: {
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
    public List<Channel> getRequiredChannels(Event event) {

        DatumReader<GenericRecord> reader = new SpecificDatumReader<GenericRecord>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(event.getBody(), null);
        GenericRecord payload2 = null;
        try {
            payload2 = reader.read(null, decoder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Channel> channels = new ArrayList<>();

        for (Channel channel : this.channels.keySet()) {
            boolean possible = true;
            //List<Attribute> attributes = this.channels.get(channel);
            for (Attribute attribute : this.channels.get(channel)) {
                for (String key : attribute.getOperations().keySet()) {

                    String opt1 = attribute.getOperations().get(key);
                    String opt2 = payload2.get(attribute.getAttName()).toString();
                    switch (key) {
                        case "eq": {
                            if (!opt1.equals(opt2)) {
                                possible = false;
                            }
                            break;
                        }
                        case "nq": {
                            if (opt1.equals(opt2)) {
                                possible = false;
                            }
                            break;
                        }
                        case "gt": {
                            if (Integer.parseInt(opt1) >= Integer.parseInt(opt2)) {
                                System.out.println(Integer.parseInt(attribute.getOperations().get(key)));
                                possible = false;
                            }
                            break;
                        }
                        case "ge": {
                            if (Integer.parseInt(opt1) > Integer.parseInt(opt2)) {
                                possible = false;
                            }
                            break;
                        }
                        case "lt": {
                            if (Integer.parseInt(opt1) <= Integer.parseInt(opt2)) {
                                possible = false;
                            }
                            break;
                        }
                        case "le": {
                            if (Integer.parseInt(opt1) < Integer.parseInt(opt2)) {
                                possible = false;
                            }
                            break;
                        }
                    }
                }
            }
            if (possible) {
                channels.add(channel);
            }
        }
        return channels;
    }

    @Override
    public List<Channel> getOptionalChannels(Event event) {
        return EMPTY_LIST;
    }

    @Override
    public void configure(Context context) {
        this.channelNames = context.getString(CONFIG_CHANNELS);

        Map<String, Channel> channelNameMap = new HashMap<String, Channel>();
        for (Channel ch : getAllChannels()) {
            channelNameMap.put(ch.getName(), ch);
        }

        if (channelNameMap.size() > 0) {
            channels = new HashMap<>();

            List<Channel> configuredChannels = getChannelListFromNames(
                    channelNames,
                    channelNameMap);

            for (Channel channel : configuredChannels) {
                List<String> attributes = new ArrayList<String>();
                channels.put(channel, new ArrayList<>());
                Collections.addAll(attributes, context.getString(channel.getName()).split(" "));
                for (String attributeName : attributes) {
                    Attribute attribute = new Attribute();
                    attribute.setAttName(attributeName);
                    attribute.setOperations(context.getSubProperties(channel.getName() + "." + attributeName + "."));
                    channels.get(channel).add(attribute);
                }
            }
        }

        eventName = context.getString(CEPSelectorConstants.EVENT_NAME);
        String eventAttributes = context.getString(CEPSelectorConstants.EVENT_ATTRIBUTES);
        if (eventAttributes != null && !eventAttributes.isEmpty()) {
            String[] atts = eventAttributes.trim().split(" ");
            attributes = new HashMap<>();
            for (String attribute : atts) {
                attributes.put(attribute, context.getString(attribute + "." + CEPSelectorConstants.ATTRIBUTE_TYPE));
            }
            attributeNames = attributes.keySet();
            schema = makeSchema(attributes, eventName);
        }

    }

    //Given a list of channel names as space delimited string,
    //returns list of channels.
    @Override
    protected List<Channel> getChannelListFromNames(String channels, Map<String, Channel> channelNameMap) {
        List<Channel> configuredChannels = new ArrayList<Channel>();
        String[] chNames = channels.split(" ");
        for (String name : chNames) {
            Channel ch = channelNameMap.get(name);
            if (ch != null) {
                configuredChannels.add(ch);
            } else {
                throw new FlumeException("Selector channel not found: "
                        + name);
            }
        }
        return configuredChannels;
    }

    class Attribute {
        private String attName;
        private Map<String, String> operations;

        public Attribute() {
            operations = new HashMap<String, String>();
        }

        public String getAttName() {
            return attName;
        }

        public void setAttName(String attName) {
            this.attName = attName;
        }

        public Map<String, String> getOperations() {
            return operations;
        }

        public void setOperations(Map<String, String> operations) {
            this.operations = operations;
        }

        public void addOperation(String operation, String value) {
            this.operations.put(operation, value);
        }

    }

}
