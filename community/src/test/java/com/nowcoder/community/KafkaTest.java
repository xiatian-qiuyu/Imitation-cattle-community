package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTest {
    @Autowired
    private KafkaProducer kafkaProducer;
    @Test
    public void testKafka(){
        kafkaProducer.sendMessage("test","唱");
        kafkaProducer.sendMessage("test","跳");
        kafkaProducer.sendMessage("test","rap");
        kafkaProducer.sendMessage("test","篮球");
        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

/**
 * 生产者
 */
@Component
class KafkaProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic,content);
    }
}

/**
 * 消费者
 */
@Component
class KafkaConsumer{
    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}