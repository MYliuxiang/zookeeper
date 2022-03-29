package com.example.zookeeperseckillserver;

import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ZookeeperSeckillServerApplication {
    private  static  String ZK_SERVER_ADDR="172.16.164.151:2181,172.16.164.151:2182,172.16.164.151:2183";
    private static  int SESSION_TIMEOUT=30000;
    private static  String PATH = "/servers";
    private static String SUB_PATH = "/seckillServer";

    @Value("${server.host}")
    private String host;

    @Value("${server.port}")
    private String port;

    private ZooKeeper zooKeeper;

    public static void main(String[] args) {
        SpringApplication.run(ZookeeperSeckillServerApplication.class, args);
    }

    @Bean
    public ZooKeeper zooKeeper() throws Exception{
        zooKeeper = new ZooKeeper(ZK_SERVER_ADDR, SESSION_TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("event = " + watchedEvent);
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("zookeeper客户端连接成功");

                    try {
                        zooKeeper.create(PATH + SUB_PATH,(host + ":" + port).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    System.out.println(1234);
                }

            }
        });

        return zooKeeper;

    }

}
