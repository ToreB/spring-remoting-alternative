package no.toreb;

import lombok.extern.slf4j.Slf4j;
import no.toreb.client.service.DynamicWithHttpRequestHandlerServiceFactory;
import no.toreb.client.service.DynamicWithRouterFunctionServiceFactory;
import no.toreb.client.service.SpringRemotingServiceFactory;
import no.toreb.client.service.StaticService;
import no.toreb.common.DataObject;
import no.toreb.common.DataObject2;
import no.toreb.common.RemoteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.supplyAsync;

// The application needs to run before running PerformanceComparison. Run the class Application first.
@Slf4j
class PerformanceComparison implements CommandLineRunner {

    public static void main(final String[] args) {
        new SpringApplicationBuilder(PerformanceComparison.class)
                .web(WebApplicationType.NONE)
                .properties("logging.level.no.toreb.client=warn")
                .run(args);
    }

    @Override
    public void run(final String... args) {
        final int requests = 10000;
        final int simultaneousRequests = 1;
        final int iterations = 2;

        final String baseUrl = "http://localhost:8080";
        final RemoteService staticService = new StaticService(baseUrl);
        final DynamicWithRouterFunctionServiceFactory dynamicWithRouterFunctionServiceFactory =
                new DynamicWithRouterFunctionServiceFactory(baseUrl);
        final RemoteService dynamic1Service = dynamicWithRouterFunctionServiceFactory.create(RemoteService.class);
        final SpringRemotingServiceFactory springRemotingServiceFactory =
                new SpringRemotingServiceFactory(baseUrl);
        final RemoteService springRemotingService = springRemotingServiceFactory.create(RemoteService.class,
                                                                                        "/remoting/remoteService");
        final DynamicWithHttpRequestHandlerServiceFactory dynamicWithHttpRequestHandlerServiceFactory =
                new DynamicWithHttpRequestHandlerServiceFactory(baseUrl);
        final RemoteService dynamic2Service = dynamicWithHttpRequestHandlerServiceFactory.create(RemoteService.class);

        final DataObject dataObject = DataObject.builder()
                                                .field1("value1")
                                                .field2(37)
                                                .field3(true)
                                                .field4(DataObject2.builder()
                                                                   .field1("value2")
                                                                   .strings(List.of("str1", "str2"))
                                                                   .build())
                                                .build();

        final ExecutorService executorService = Executors.newFixedThreadPool(simultaneousRequests);

        log.info("Starting executions.");
        final Map<String, CompletableFuture<Duration>> executions = new HashMap<>();
        for (int i = 0; i < iterations; i++) {
            final CompletableFuture<Duration> future1 =
                    supplyAsync(testPerformance(() -> staticService.exchange(dataObject), requests),
                                executorService);
            executions.put("Static (RestController) " + i, future1);
            final CompletableFuture<Duration> future2 =
                    supplyAsync(testPerformance(() -> dynamic1Service.exchange(dataObject), requests),
                                executorService);
            executions.put("Dynamic (RouterFunction) " + i, future2);

            final CompletableFuture<Duration> future3 =
                    supplyAsync(testPerformance(() -> springRemotingService.exchange(dataObject),
                                                requests),
                                executorService);
            executions.put("Spring Remoting " + i, future3);

            final CompletableFuture<Duration> future4 =
                    supplyAsync(testPerformance(() -> dynamic2Service.exchange(dataObject), requests),
                                executorService);
            executions.put("Dynamic (HttpRequestHandler) " + i, future4);
        }

        log.info("Waiting for executions to finish.");
        CompletableFuture.allOf(executions.values().toArray(CompletableFuture[]::new)).join();

        executorService.shutdown();

        log.info("");
        log.info("Results with {} iterations per service, with {} requests running simultaneously:",
                 requests, simultaneousRequests);
        executions.entrySet()
                  .stream()
                  .map(entry -> Map.entry(entry.getKey(), entry.getValue().join()))
                  .sorted(Map.Entry.comparingByValue())
                  .forEachOrdered(entry -> {
                      final double millis = toMillis(entry.getValue());

                      log.info("{}: {} \t {}",
                               "%-35s".formatted(entry.getKey()),
                               "Total %12.4f ms".formatted(millis),
                               "Average per request %10.4f ms".formatted(millis / requests));
                  });
    }

    @SuppressWarnings("SameParameterValue")
    private Supplier<Duration> testPerformance(final Runnable function, final int requests) {
        return () -> {
            // Small warm-up before timing starts
            IntStream.range(0, 10).forEach(i -> function.run());
            return time(function, requests);
        };
    }

    private static double toMillis(final Duration staticDuration) {
        return staticDuration.toNanos() / 1_000_000.0;
    }

    @SuppressWarnings("SameParameterValue")
    private static Duration time(final Runnable runnable, final int requests) {
        final long start = System.nanoTime();
        IntStream.range(0, requests).forEach(i -> runnable.run());
        return Duration.ofNanos(System.nanoTime() - start);
    }
}
