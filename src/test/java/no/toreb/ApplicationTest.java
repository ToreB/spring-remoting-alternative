package no.toreb;

import lombok.extern.slf4j.Slf4j;
import no.toreb.client.service.DynamicServiceFactory;
import no.toreb.client.service.StaticService;
import no.toreb.common.RemoteService;
import no.toreb.server.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
                classes = Application.class)
class ApplicationTest {

    private RemoteService staticService;
    private RemoteService dynamicService;

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() {
        final DynamicServiceFactory dynamicServiceFactory = new DynamicServiceFactory(getBaseUrl());
        staticService = new StaticService(getBaseUrl());
        dynamicService = dynamicServiceFactory.create(RemoteService.class);
    }

    @Test
    void test() {
        final String staticHello = staticService.hello();
        final String staticBye = staticService.bye();
        staticService.doSomething("value1", true);

        final String dynamicHello = dynamicService.hello();
        final String dynamicBye = dynamicService.bye();
        dynamicService.doSomething("value2", false);

        assertThat(staticHello)
                .isEqualTo(dynamicHello)
                .isEqualTo("hello from remote service");
        assertThat(staticBye)
                .isEqualTo(dynamicBye)
                .isEqualTo("bye from remote service");
    }
}
