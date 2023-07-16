package com.nowcoder.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;
    //处理事件
    public void fileEvent(Event event){
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
