package eu.supersede.mdm.storage.cep.kafka.utils;

import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

/**
 * Created by osboxes on 14/04/17.
 */
public class TopicCreator {
    public static void main(String[] args) {
        String zookeeperConnect = "localhost:2181";
        int sessionTimeoutMs = 10 * 1000;
        int connectionTimeoutMs = 8 * 1000;
        // Note: You must initialize the ZkClient with ZKStringSerializer.  If you don't, then
        // createTopic() will only seem to work (it will return without error).  The topic will exist in
        // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the
        // topic.
        ZkClient zkClient = new ZkClient(
                zookeeperConnect,
                sessionTimeoutMs,
                connectionTimeoutMs,
                ZKStringSerializer$.MODULE$);

        // Security for Kafka was added in Kafka 0.9.0.0
        boolean isSecureKafkaCluster = false;
        ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperConnect), isSecureKafkaCluster);

        String aaa = "hi";
        zkUtils.zkClient().writeData("/ggg", "sss");

//        String topic = "logtest";
//        int partitions = 2;
//        int replication = 1;
//        Properties topicConfig = new Properties(); // add per-topic configurations settings here
//        AdminUtils.deleteTopic(zkUtils,topic);
//        AdminUtils.createTopic(zkUtils, topic, partitions, replication, topicConfig);

        zkClient.close();
    }
}
