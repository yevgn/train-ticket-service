package ru.antonov.train_ticker_service.ticket.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.antonov.trainticketservice.TrainTicketServiceApplication;
import ru.antonov.trainticketservice.ticket.service.NotificationClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = TrainTicketServiceApplication.class)
public class NotificationClientTest {
    @Autowired
    private NotificationClient notificationClient;

    @Test
    void bulkheadTest() throws Exception {
        int total = 5;

        TicketPurchasedNotificationRequestDto request = TicketPurchasedNotificationRequestDto.builder()
                .ticketId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .userEmail("test")
                .cruiseId(UUID.randomUUID())
                .departureStationLocation("test")
                .departureStationName("test")
                .arrivalStationName("test")
                .arrivalStationLocation("test")
                .purchasedAt(LocalDateTime.now())
                .fare(1000.0f)
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(total);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            futures.add(executor.submit(() -> {
                start.await();
                try {
                    notificationClient.sendTicketPurchasedNotification(request);
                    return "SUCCESS";
                } catch (Exception e) {
                    return "REJECTED";
                }
            }));
        }

        start.countDown();

        Thread.sleep(3000L);

        long success = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return "REJECTED";
                    }
                })
                .filter("SUCCESS"::equals)
                .count();

        assertThat(success).isLessThanOrEqualTo(2);
    }
//
//    @Test
//    void bulkheadTest() throws Exception {
//        int total = 5;
//
//        TicketPurchasedNotificationRequestDto request = TicketPurchasedNotificationRequestDto.builder()
//                .ticketId(UUID.randomUUID())
//                .userId(UUID.randomUUID())
//                .userEmail("test")
//                .cruiseId(UUID.randomUUID())
//                .departureStationLocation("test")
//                .departureStationName("test")
//                .arrivalStationName("test")
//                .arrivalStationLocation("test")
//                .purchasedAt(LocalDateTime.now())
//                .fare(1000.0f)
//                .build();
//
//        CountDownLatch startSignal = new CountDownLatch(1);
//
//        List<CompletableFuture<String>> futures = IntStream.range(0, total)
//                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
//                    try {
//                        startSignal.await();
//                        notificationClient.sendTicketPurchasedNotification(request);
//                        return "SUCCESS";
//                    } catch (Exception e) {
//                        return "REJECTED";
//                    }
//                }))
//                .toList();
//
//        // одновременно запускаем
//        startSignal.countDown();
//
//        List<String> results = futures.stream()
//                .map(CompletableFuture::join)
//                .toList();
//
//        long success = results.stream().filter(r -> r.equals("SUCCESS")).count();
//
//        assertThat(success).isLessThanOrEqualTo(2);
//    }
}
