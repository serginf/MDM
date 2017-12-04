package eu.supersede.mdm.storage.cep.manager;

/**
 * Created by osboxes on 31/05/17.
 */
public class FlumeChannel {

    private String agentName;
    private String channelName;
    private FlumeSink flumeSink;

    public FlumeChannel() {
    }

    public FlumeChannel(String agentName, String channelName, FlumeSink flumeSink) {
        this.agentName = agentName;
        this.channelName = channelName;
        this.flumeSink = flumeSink;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public FlumeSink getFlumeSink() {
        return flumeSink;
    }

    public void setFlumeSink(FlumeSink flumeSink) {
        this.flumeSink = flumeSink;
    }

    public String interpret() {
        String prefix = agentName + ManagerConstants.DOT + ManagerConstants.CHANNELS + ManagerConstants.DOT + channelName + ManagerConstants.DOT;
        return String.join("\n"
                , prefix + ManagerConstants.TYPE + "=" + ManagerConstants.CHANNEL_TPYE_MEMORY
                , prefix + ManagerConstants.CHANNEL_CAPACITY + "= 1000"
                , prefix + ManagerConstants.CHANNEL_TRANSACTION_CAPACITY + "= 100"
                , prefix + ManagerConstants.CHANNEL_KEEP_ALIVE + "= 3"
        );
    }
}
