package com.example.zookeeperseckillserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class OrderIDGenerator {

    private int count=0;

    public synchronized String getId(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-mm");
        String format = sdf.format(new Date());
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return format+"-"+(++count);
    }
}