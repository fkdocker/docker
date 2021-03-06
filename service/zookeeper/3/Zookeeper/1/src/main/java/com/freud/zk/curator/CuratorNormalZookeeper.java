package com.freud.zk.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

public class CuratorNormalZookeeper {
    private static final int SECOND = 1000;

    public static void main(String[] args) throws Exception {
        String root = "/hifreud";
        String path = root + "/sayhi";
        String path2 = root + "/sayhello";
        String data = "hi freud!";
        String dataAgain = "hi freud again!";

        RetryPolicy rp = new ExponentialBackoffRetry(SECOND, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181").sessionTimeoutMs(5 * SECOND).connectionTimeoutMs(3 * SECOND).retryPolicy(rp).build();
        client.start();
        System.out.println("Server connected...");

        client.getCuratorListenable().addListener((curatorFramework, curatorEvent) -> System.out.println("Curator framework operations: " + curatorEvent.getType()));
        client.getConnectionStateListenable().addListener((curatorFramework, connectionState) -> System.out.println("Connection state change to: " + connectionState.name()));
        System.out.println("Listener added success...");

        if (client.checkExists().forPath(path) == null) {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, data.getBytes());
            System.out.println("Create node [" + path + "] with data [" + data + "]");
        }
        if (client.checkExists().forPath(path2) == null) {
            client.create().withMode(CreateMode.PERSISTENT).forPath(path2, data.getBytes());
            System.out.println("Create node [" + path2 + "] with data [" + data + "]");
        }

        if (client.checkExists().forPath(path) != null) {
            Stat stat = new Stat();
            System.out.println("Read from node [" + path + "] data: " + new String(client.getData().storingStatIn(stat).forPath(path)));
            System.out.println("\tversion: " + stat.getVersion());
            System.out.println("\tczxid: " + stat.getCzxid());
            System.out.println("\taversion: " + stat.getAversion());
            System.out.println("\tmzxid: " + stat.getMzxid());
        }
        if (client.checkExists().forPath(path) != null) {
            client.setData().forPath(path, dataAgain.getBytes());
            System.out.println("Set data to node [" + path + "] data: " + dataAgain);
        }
        if (client.checkExists().forPath(path) != null) {
            Stat stat = new Stat();
            System.out.println("Read from node [" + path + "] data: " + new String(client.getData().storingStatIn(stat).forPath(path)));
            System.out.println("\tversion: " + stat.getVersion());
            System.out.println("\tczxid: " + stat.getCzxid());
            System.out.println("\taversion: " + stat.getAversion());
            System.out.println("\tmzxid: " + stat.getMzxid());
        }

        if (client.checkExists().forPath(path2) != null) {
            client.delete().guaranteed().forPath(path2);
            System.out.println("Delete node [" + path2 + "].");
        }
        if (client.checkExists().forPath(root) != null) {
            client.delete().deletingChildrenIfNeeded().forPath(root);
            System.out.println("Delete node [" + root + "] use recursion.");
        }

        client.close();
        System.out.println("Server closed...");
    }
}
