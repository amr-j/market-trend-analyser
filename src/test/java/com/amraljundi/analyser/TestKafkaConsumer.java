package com.amraljundi.analyser;

import com.amraljundi.analyser.event.SectorDataFetched;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

@Component
public class TestKafkaConsumer {

    private final List<SectorDataFetched> receivedEvents = new CopyOnWriteArrayList<>();
    // TODO - explain what this is
    private volatile CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = "sector-data-fetched", groupId = "test-consumer")
    public void consume(SectorDataFetched event) {
        receivedEvents.add(event);
        latch.countDown();
    }

    public List<SectorDataFetched> getReceivedEvents() {
        return receivedEvents;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void reset() {
        receivedEvents.clear();
        latch = new CountDownLatch(1);
    }
}
