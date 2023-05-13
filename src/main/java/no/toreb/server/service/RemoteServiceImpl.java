package no.toreb.server.service;

import lombok.extern.slf4j.Slf4j;
import no.toreb.common.RemoteService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RemoteServiceImpl implements RemoteService {

    @Override
    public String hello() {
        return "hello from remote service";
    }

    @Override
    public String bye() {
        return "bye from remote service";
    }

    @Override
    public void doSomething(final String param1, final boolean param2) {
        log.info("Do something: ({}, {})", param1, param2);
    }
}
