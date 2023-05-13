package no.toreb;

import lombok.extern.slf4j.Slf4j;
import no.toreb.client.service.DynamicServiceFactory;
import no.toreb.client.service.SpringRemotingServiceFactory;
import no.toreb.client.service.StaticService;
import no.toreb.common.RemoteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Slf4j
class PerformanceComparison implements CommandLineRunner {

    public static void main(final String[] args) {
        new SpringApplicationBuilder(PerformanceComparison.class)
                .web(WebApplicationType.NONE)
                .properties("logging.level.no.toreb.client=warn")
                .run(args);
    }

    @Override
    public void run(final String... args) throws Exception {
        final String baseUrl = "http://localhost:8080";
        final RemoteService staticService = new StaticService(baseUrl);
        final DynamicServiceFactory dynamicServiceFactory = new DynamicServiceFactory(baseUrl);
        final RemoteService dynamicService = dynamicServiceFactory.create(RemoteService.class);
        final SpringRemotingServiceFactory springRemotingServiceFactory =
                new SpringRemotingServiceFactory(baseUrl);
        final RemoteService springRemotingService = springRemotingServiceFactory.create(RemoteService.class,
                                                                                        "/remoting/remoteService");

        final ExecutorService executorService = Executors.newFixedThreadPool(1);

        final int iterations = 10000;

        log.info("Starting executions.");
        final Map<String, CompletableFuture<Duration>> executions = new HashMap<>();
        for (int i = 0; i < 2; i++) {
            final CompletableFuture<Duration> future1 =
                    CompletableFuture.supplyAsync(() -> time(staticService::hello, iterations),
                                                  executorService);
            executions.put("Static " + i, future1);

            final CompletableFuture<Duration> future2 =
                    CompletableFuture.supplyAsync(() -> time(dynamicService::hello, iterations),
                                                  executorService);
            executions.put("Dynamic " + i, future2);

            final CompletableFuture<Duration> future3 =
                    CompletableFuture.supplyAsync(() -> time(springRemotingService::hello, iterations),
                                                  executorService);
            executions.put("Remoting " + i, future3);
        }

        log.info("Waiting for executions to finish.");
        CompletableFuture.allOf(executions.values().toArray(CompletableFuture[]::new)).join();

        executorService.shutdown();

        executions.entrySet()
                  .stream()
                  .map(entry -> Map.entry(entry.getKey(), entry.getValue().join()))
                  .sorted(Map.Entry.comparingByValue())
                  .forEachOrdered(entry -> log.info("Duration {}: {} ms",
                                                    entry.getKey(), toMillis(entry.getValue())));
    }

    private static double toMillis(final Duration staticDuration) {
        return staticDuration.toNanos() / 1_000_000.0;
    }

    static Duration time(final Runnable runnable, final int iterations) {
        final long start = System.nanoTime();
        IntStream.range(0, iterations).forEach(i -> runnable.run());
        return Duration.ofNanos(System.nanoTime() - start);
    }
}
