package eu.supersede.mdm.storage.cep.flume.selectors;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.channel.AbstractChannelSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DCEPFilterSelector extends AbstractChannelSelector {

    public static final String CONFIG_CHANNELS = "channels";

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory
            .getLogger(DCEPFilterSelector.class);

    private static final List<Channel> EMPTY_LIST =
            Collections.emptyList();

    private String channelNames;

    private Map<String, Channel> channels;


    @Override
    public List<Channel> getRequiredChannels(Event event) {

        String[] rules = event.getHeaders().get("Rules").trim().split(";");

        Set<Channel> channelsSet = new HashSet<>();

        for (String r : rules) {
            r = r.trim();
            if (!r.equals("")) {
                if (channels.get(r) != null)
                    channelsSet.add(channels.get(r));
            }
        }

        List<Channel> aaa = new ArrayList<>(channelsSet);

        return aaa;
    }

    @Override
    public List<Channel> getOptionalChannels(Event event) {
        return EMPTY_LIST;
    }

    @Override
    public void configure(Context context) {
        this.channelNames = context.getString(CONFIG_CHANNELS);

        this.channels = new HashMap<>();

        Map<String, Channel> channelNameMap = new HashMap<String, Channel>();
        for (Channel ch : getAllChannels()) {
            channelNameMap.put(ch.getName(), ch);
        }

        if (channelNameMap.size() > 0) {

            List<Channel> configuredChannels = getChannelListFromNames(
                    channelNames,
                    channelNameMap);

            for (Channel channel : configuredChannels) {
                String[] rules = context.getString(channel.getName() + "." + DCEPSelectorConstants.RULES).trim().split(";");
                for (String r : rules) {
                    r = r.trim();
                    if (!r.equals("")) {
                        channels.put(r, channel);

                    }
                }
            }
        }
    }

}
