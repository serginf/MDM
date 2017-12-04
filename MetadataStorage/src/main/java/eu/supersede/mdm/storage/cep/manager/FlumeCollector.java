package eu.supersede.mdm.storage.cep.manager;

import eu.supersede.mdm.storage.cep.RDF_Model.Rule;
import eu.supersede.mdm.storage.cep.RDF_Model.event.EventSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osboxes on 30/05/17.
 */
public class FlumeCollector {


    String sourceName = "avro-source";

    public String interpret(String agentName, List<EventSchema> events, List<Rule> rules, String kafkaBootstrap, String streamType, boolean restart, String deletedRules) throws Exception {

        List<FlumeSink> flumeSinks = new ArrayList<>();
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

        String channelNames = "";
        for (FlumeChannel flumeChannel : flumeChannels) {
            channelNames += flumeChannel.getChannelName() + " ";
        }

        String sinkNames = "";
        for (FlumeSink flumeSink : flumeSinks) {
            sinkNames += flumeSink.getSinkName() + " ";
        }

        String result = "##### INIT #####";

        result = String.join("\n", result
                , agentName + ManagerConstants.DOT + ManagerConstants.SOURCES + " = " + sourceName
                , agentName + ManagerConstants.DOT + ManagerConstants.CHANNELS + " = " + channelNames
                , agentName + ManagerConstants.DOT + ManagerConstants.SINKS + " = " + sinkNames
        );


        String sourcePrefix = agentName + ManagerConstants.DOT + ManagerConstants.SOURCES + ManagerConstants.DOT + sourceName + ManagerConstants.DOT;

        result = String.join("\n", result
                , sourcePrefix + ManagerConstants.TYPE + " = " + "avro"
                , sourcePrefix + "bind" + " = " + "localhost"
                , sourcePrefix + "port" + " = " + "22224"
                , sourcePrefix + "channels" + " = " + channelNames
        );


        result = String.join("\n", result
                , sourcePrefix + ManagerConstants.SELECTOR_TYPE + " = " + "upc.edu.cep.flume.selectors.DCEPFilterSelector"
                , sourcePrefix + ManagerConstants.SELECTOR_CHANNELS + " = " + channelNames
        );

        for (FlumeChannel flumeChannel : flumeChannels) {
            result = String.join("\n", result
                    , sourcePrefix + ManagerConstants.SELECTOR_PREFIX + flumeChannel.getChannelName() + ManagerConstants.DOT + "rules" + " = " + flumeChannel.getFlumeSink().getSinkName()
            );
        }

        String channels = "", sinks = "";
        for (FlumeSink sink : flumeSinks)
            sinks += " " + sink.getSinkName();
        for (FlumeChannel channel : flumeChannels)
            channels += " " + channel.getChannelName();

        result = String.join("\n", result, "\n##### Channels #####\n");
        for (FlumeChannel channel : flumeChannels) {
            result = String.join("\n", result, channel.interpret(), "\n");
        }
        result = String.join("\n", result, "\n##### Sinks #####\n");
        for (FlumeSink sink : flumeSinks) {
            result = String.join("\n", result, sink.interpret(deletedRules), "\n");
        }
        return result;
    }
}
