package eu.supersede.mdm.storage.cep.manager;

import eu.supersede.mdm.storage.cep.RDF_Model.Rule;
import eu.supersede.mdm.storage.cep.RDF_Model.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by osboxes on 30/05/17.
 */
public class Manager {

    private Map<FlumeStream, List<FlumeSink>> sourceSinksMap = new HashMap<>();

    public String CreateConfiguration(String agentName, List<EventSchema> events, List<Rule> rules, String kafkaBootstrap, String streamType, boolean restart, String deletedRules) throws Exception {
//        BASE64Encoder encoder = new BASE64Encoder();
//        MessageDigest md = MessageDigest.getInstance("MD5");
//        md.update(someString.getBytes());
//        byte[] bMac = md.digest();
//        String anotherString = encoder.encodeBuffer(bMac);

        List<FlumeSink> flumeSinks = new ArrayList<>();
        List<FlumeStream> flumeStreams = new ArrayList<>();
        List<FlumeChannel> flumeChannels = new ArrayList<>();

        for (Rule rule : rules) {
            String iri = rule.getIRI();
            FlumeChannel flumeChannel = new FlumeChannel();
            FlumeSink flumeSink = new FlumeSink();
            flumeChannel.setAgentName(agentName);
            flumeChannel.setChannelName("Channel" + iri);
            flumeSink.setSinkName("Sink" + iri);
            flumeSink.setRule(rule);
            flumeSink.setFlumeChannel(flumeChannel);
            flumeSink.setAgentName(agentName);
            flumeSink.setRestart(restart);
            flumeChannel.setFlumeSink(flumeSink);
            flumeChannels.add(flumeChannel);
            flumeSinks.add(flumeSink);
        }

        for (EventSchema event : events) {
            FlumeStream flumeStream = new FlumeStream();
            flumeStream.setEvent(event);
            flumeStream.setStreamType(streamType);
            flumeStream.setKafkaBootstrap(kafkaBootstrap);
            flumeStream.setSourceName("Source" + event.getIRI());
            flumeStream.setAgentName(agentName);
            flumeStreams.add(flumeStream);
            sourceSinksMap.put(flumeStream, new ArrayList<>());
        }
        for (FlumeSink flumeSink : flumeSinks) {
            fillMap(flumeSink, flumeSink.getRule().getCEPElement());
        }

        String sources = "", channels = "", sinks = "";
        for (FlumeSink sink : flumeSinks)
            sinks += " " + sink.getSinkName();
        for (FlumeStream source : flumeStreams)
            sources += " " + source.getSourceName();
        for (FlumeChannel channel : flumeChannels)
            channels += " " + channel.getChannelName();
        String result = String.join("\n"
                , agentName + ManagerConstants.DOT + ManagerConstants.SOURCES + " = " + sources
                , agentName + ManagerConstants.DOT + ManagerConstants.CHANNELS + " = " + channels
                , agentName + ManagerConstants.DOT + ManagerConstants.SINKS + " = " + sinks
        );

        result = String.join("\n", result, "\n", "#### Sources ####", "\n");
        for (FlumeStream source : flumeStreams) {
            //   result = String.join("\n", result, source.interpret(sourceSinksMap), "\n");
        }
        result = String.join("\n", result, "#### Channels ####", "\n");
        for (FlumeChannel channel : flumeChannels) {
            result = String.join("\n", result, channel.interpret(), "\n");
        }
        result = String.join("\n", result, "#### Sinks ####");
        for (FlumeSink sink : flumeSinks) {
            result = String.join("\n", result, sink.interpret(deletedRules), "\n");
        }

        return result;
    }

    private void fillMap(FlumeSink sink, CEPElement CEPElement) {
        if (CEPElement.getClass().equals(Event.class)) {
            for (FlumeStream flumeStream : sourceSinksMap.keySet()) {
                if (flumeStream.getEvent().equals(((Event) CEPElement).getEventSchema())) {
                    if (!sourceSinksMap.get(flumeStream).contains(sink)) {
                        sourceSinksMap.get(flumeStream).add(sink);
                    }
                }
            }
        } else if (!CEPElement.getClass().equals(TimeEvent.class)) {
            for (CEPElement e : ((Pattern) CEPElement).getCEPElements()) {
                fillMap(sink, e);
            }
        }
    }
}
