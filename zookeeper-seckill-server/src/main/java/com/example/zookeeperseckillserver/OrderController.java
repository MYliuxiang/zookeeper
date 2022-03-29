package com.example.zookeeperseckillserver;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class OrderController {

    private RestTemplate restTemplate=new RestTemplate();

    @Autowired
    private ZooKeeper zooKeeper;

    private String path="/locks";
    private String node="/orderIdLock";

    @RequestMapping("createOrder")
    public String createOrder() throws Exception{
        //获取id
        if(tryLock()){
            //调用业务方法
            String id = restTemplate.getForObject("http://localhost:8080/getId", String.class);
            System.out.println("获取到的id:"+id);
            //释放锁
            unlock();
        }else {
            waitLock();
        }
        return "success";
    }
    //尝试获取id, 如果获取到了, 返回true, 否则返回false
    //竞争锁资源
    public boolean tryLock(){
        try {
            //因为不是顺序节点, 对于同一个路径, 只能创建一次
            String path = zooKeeper.create(this.path+this.node,
                    null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //释放锁资源
    public void unlock(){
        try {
            Stat stat = zooKeeper.exists(this.path+this.node, false);
            zooKeeper.delete(this.path+this.node, stat.getVersion());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //阻塞状态
    public  void waitLock(){
        try {
            zooKeeper.getChildren(this.path, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    try {
                        createOrder();//重新创建订单
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}