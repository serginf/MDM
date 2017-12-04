package eu.supersede.mdm.storage.cep.kafka.utils;

import com.google.common.base.Charsets;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 */
public class configCreator {

    public static void create(String agentName, File file) throws Exception {
        EnsurePath ensurePath = new EnsurePath("/flume" + "/agentName");
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
        client.start();
        ensurePath.ensure(client.getZookeeperClient());
        BufferedReader reader = new BufferedReader(new FileReader(file));  ///home/osboxes/apache-flume-1.7.0-bin/conf/agent3.properties"));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        client.setData().forPath("/flume" + "/agentName", stringBuilder.toString().getBytes(Charsets.UTF_8));
    }

    public static void create(String agentName, String configuration) throws Exception {
        EnsurePath ensurePath = new EnsurePath("/flume" + "/agentName");
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
        client.start();
        ensurePath.ensure(client.getZookeeperClient());

        client.setData().forPath("/flume" + "/agentName", configuration.getBytes(Charsets.UTF_8));
    }

}