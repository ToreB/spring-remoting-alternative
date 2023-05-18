package no.toreb.server.config;

import lombok.RequiredArgsConstructor;
import no.toreb.common.DataObject;
import no.toreb.common.RemoteMethodInvocation;
import no.toreb.common.RemoteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static no.toreb.server.config.ApiUtils.deserialize;
import static no.toreb.server.config.ApiUtils.serialize;
import static no.toreb.server.config.ApiUtils.time;

@RestController
@RequestMapping("/static")
@RequiredArgsConstructor
public class StaticApiController {

    private final RemoteService service;

    @PostMapping("/hello")
    byte[] hello() {
        return time("/static/hello", () -> serialize(service.hello()));
    }

    @PostMapping("/bye")
    byte[] bye() {
        return time("/static/bye", () -> serialize(service.bye()));
    }

    @PostMapping("/doSomething")
    void doSomething(final HttpServletRequest request) {
        time("/static/doSomething", () -> {
            final RemoteMethodInvocation<Void> methodInvocation = deserialize(request.getInputStream());
            final Object[] methodArguments = methodInvocation.getArguments();
            service.doSomething((String) methodArguments[0], (Boolean) methodArguments[1]);
            return null;
        });
    }

    @PostMapping("/exchange")
    byte[] exchange(final HttpServletRequest request) {
        return time("/static/exchange", () -> {
            final RemoteMethodInvocation<Void> methodInvocation = deserialize(request.getInputStream());
            final DataObject result = service.exchange((DataObject) methodInvocation.getArguments()[0]);
            return serialize(result);
        });
    }

    @PostMapping("/getSomething")
    Object getSomething() {
        return time("/static/getSomething", () -> {
            final Object result = service.getSomething();
            return serialize(result);
        });
    }
}
