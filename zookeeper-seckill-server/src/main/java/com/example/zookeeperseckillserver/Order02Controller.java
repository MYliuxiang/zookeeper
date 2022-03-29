package com.example.zookeeperseckillserver;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RestController
public class Order02Controller {

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ZooKeeper zooKeeper;

    private String path = "/locks02";
    private String node = "/orderIdLock";

    @RequestMapping("createOrder02")
    public String createOrder() throws Exception {
        String currentPath = zooKeeper.create(this.path + this.node,
                null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        currentPath=currentPath.substring(currentPath.lastIndexOf("/")+1);
        //获取id
        if (tryLock(currentPath)) {
            //调用业务方法
            String id = restTemplate.getForObject("http://localhost:8080/getId", String.class);
            System.out.println("获取到的id:" + id);
            //释放锁
            unlock(currentPath);
        } else {
            waitLock(currentPath);
        }
        return "success";
    }

    //尝试获取id, 如果获取到了, 返回true, 否则返回false
    //竞争锁资源
    public boolean tryLock(String currentPath) {
        try {
            //获取到所有的节点
            List<String> children = zooKeeper.getChildren(this.path, false);
            Collections.sort(children);
            if (StringUtils.pathEquals(currentPath, children.get(0))) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //释放锁资源
    public void unlock(String currentPath) {
        try {
            Stat stat = zooKeeper.exists(this.path + "/" + currentPath, false);
            zooKeeper.delete(this.path + "/" + currentPath, stat.getVersion());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //阻塞状态
    public void waitLock(String currentPath) {
        try {
            CountDownLatch count = new CountDownLatch(1);
            List<String> children = zooKeeper.getChildren(this.path, false);
            //获取到前一个节点
            Collections.sort(children);
            int index = children.indexOf(currentPath);
            if (index > 0) {
                String preNode = children.get(index - 1);
                //前一个节点删除操作
                zooKeeper.getData(this.path + "/" + preNode, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType() == Event.EventType.NodeDeleted) {
                            try {
                                String id = restTemplate.getForObject("http://localhost:8080/getId", String.class);
                                System.out.println("获取到的id:" + id);
                                //释放锁
                                unlock(currentPath);
                                count.countDown();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Stat());
            }
            count.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}