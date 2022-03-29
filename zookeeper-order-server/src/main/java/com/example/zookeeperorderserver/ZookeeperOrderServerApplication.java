package com.example.zookeeperorderserver;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ZookeeperOrderServerApplication {
    private  static  String ZK_SERVER_ADDR="172.16.164.151:2181,172.16.164.151:2182,172.16.164.151:2183";
    private static  int SESSION_TIMEOUT=30000;
    private static  String PATH="/servers";
    public  static List<String> addrList;

    private volatile ZooKeeper zooKeeper;

    public static void main(String[] args) {
        SpringApplication.run(ZookeeperOrderServerApplication.class, args);
    }


    @Bean
    public ZooKeeper zooKeeper() throws Exception{
        zooKeeper = new ZooKeeper(ZK_SERVER_ADDR, SESSION_TIMEOUT, new Watcher() {


            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("event = " + watchedEvent);
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("zookeeper客户端链接成功");
                    try {
                        getData();
                        //绑定永久事件监听
                        zooKeeper.addWatch(PATH, new Watcher() {
                            @Override
                            public void process(WatchedEvent watchedEvent) {
                                try {
                                    getData();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }, AddWatchMode.PERSISTENT);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            private void getData() throws KeeperException, InterruptedException{
                List<String> serverAddr = zooKeeper.getChildren(PATH, null);
                List<String> temList = new ArrayList<>();
                for (String path : serverAddr) {
                    //获取节点路径数据
                    byte[] data = zooKeeper.getData(PATH + "/" + path, null, new Stat());
                    String addrInfo = new String(data);
                    temList.add(addrInfo);
                }
                addrList = temList;
                System.out.println("获取到秒杀服务器的最新地址\n" + addrList);
            }

        });
        return zooKeeper;
    }


}
