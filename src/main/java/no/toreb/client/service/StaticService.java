package no.toreb.client.service;

import no.toreb.common.MethodRequest;
import no.toreb.common.RemoteService;
import org.springframework.stereotype.Service;

@Service
public class StaticService extends AbstractRemoteService implements RemoteService {

    public StaticService(final String baseUrl) {
        super(baseUrl + "/static");
    }

    @Override
    public String hello() {
        return callRemote(new MethodRequest<>("hello", String.class, new Object[0]));
    }

    @Override
    public String bye() {
        return callRemote(new MethodRequest<>("bye", String.class, new Object[0]));
    }

    @Override
    public void doSomething(final String param1, final boolean param2) {
        callRemote(new MethodRequest<>("doSomething", String.class, new Object[]{param1, param2}));
    }
}
