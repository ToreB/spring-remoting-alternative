package no.toreb;

import lombok.extern.slf4j.Slf4j;
import no.toreb.client.service.DynamicWithHttpRequestHandlerServiceFactory;
import no.toreb.client.service.DynamicWithRouterFunctionServiceFactory;
import no.toreb.client.service.SpringRemotingServiceFactory;
import no.toreb.client.service.StaticService;
import no.toreb.common.DataObject;
import no.toreb.common.DataObject2;
import no.toreb.common.RemoteService;
import no.toreb.server.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
                classes = Application.class)
class ApplicationTest {

    private RemoteService staticService;
    private RemoteService dynamic1Service;
    private RemoteService springRemotingService;
    private RemoteService dynamic2Service;

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() {
        staticService = new StaticService(getBaseUrl());
        final DynamicWithRouterFunctionServiceFactory dynamicWithRouterFunctionServiceFactory =
                new DynamicWithRouterFunctionServiceFactory(getBaseUrl());
        dynamic1Service = dynamicWithRouterFunctionServiceFactory.create(RemoteService.class);
        final SpringRemotingServiceFactory springRemotingServiceFactory =
                new SpringRemotingServiceFactory(getBaseUrl());
        springRemotingService = springRemotingServiceFactory.create(RemoteService.class, "/remoting/remoteService");
        final DynamicWithHttpRequestHandlerServiceFactory dynamicWithHttpRequestHandlerServiceFactory =
                new DynamicWithHttpRequestHandlerServiceFactory(getBaseUrl());
        dynamic2Service = dynamicWithHttpRequestHandlerServiceFactory.create(RemoteService.class);
    }

    @Test
    void test() {
        final DataObject dataObject = DataObject.builder()
                                                .field1("value1")
                                                .field2(37)
                                                .field3(true)
                                                .field4(DataObject2.builder()
                                                                   .field1("value2")
                                                                   .strings(List.of("str1", "str2"))
                                                                   .build())
                                                .build();

        final String staticHello = staticService.hello();
        final String staticBye = staticService.bye();
        staticService.doSomething("value1", true);
        final DataObject staticExchange = staticService.exchange(dataObject);
        final Object staticGetSomething = staticService.getSomething();

        final String dynamic1Hello = dynamic1Service.hello();
        final String dynamic1Bye = dynamic1Service.bye();
        dynamic1Service.doSomething("value2", false);
        final DataObject dynamic1Exchange = dynamic1Service.exchange(dataObject);
        final Object dynamic1GetSomething = dynamic1Service.getSomething();

        final String remotingHello = springRemotingService.hello();
        final String remotingBye = springRemotingService.bye();
        springRemotingService.doSomething("value3", true);
        final DataObject remotingExchange = springRemotingService.exchange(dataObject);
        final Object remotingGetSomething = springRemotingService.getSomething();

        final String dynamic2Hello = dynamic2Service.hello();
        final String dynamic2Bye = dynamic2Service.bye();
        dynamic2Service.doSomething("value2", false);
        final DataObject dynamic2Exchange = dynamic2Service.exchange(dataObject);
        final Object dynamic2GetSomething = dynamic2Service.getSomething();

        assertThat(staticHello)
                .isEqualTo(dynamic1Hello)
                .isEqualTo(remotingHello)
                .isEqualTo(dynamic2Hello)
                .isEqualTo("hello from remote service");
        assertThat(staticBye)
                .isEqualTo(dynamic1Bye)
                .isEqualTo(remotingBye)
                .isEqualTo(dynamic2Bye)
                .isEqualTo("bye from remote service");
        assertThat(staticExchange)
                .isEqualTo(dynamic1Exchange)
                .isEqualTo(remotingExchange)
                .isEqualTo(dynamic2Exchange)
                .isEqualTo(dataObject.toBuilder().field3(false).build());
        assertThat(staticGetSomething)
                .isEqualTo(dynamic1GetSomething)
                .isEqualTo(remotingGetSomething)
                .isEqualTo(dynamic2GetSomething)
                .isEqualTo(DataObject2.builder().field1("value1").strings(List.of("s1", "s2", "s3")).build());
    }
}
