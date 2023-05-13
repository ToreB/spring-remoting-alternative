package no.toreb.client.service;

import no.toreb.common.RemoteMethodInvocation;
import no.toreb.common.RemoteService;

public class StaticService extends AbstractRemoteService implements RemoteService {

    private final RemoteMethodInvocation<String> helloMethodInvocation =
            new RemoteMethodInvocation<>("hello", String.class, new Object[0]);
    private final RemoteMethodInvocation<String> byeMethodInvocation =
            new RemoteMethodInvocation<>("bye", String.class, new Object[0]);

    public StaticService(final String baseUrl) {
        super(baseUrl + "/static");
    }

    @Override
    public String hello() {
        return callRemote(helloMethodInvocation);
    }

    @Override
    public String bye() {
        return callRemote(byeMethodInvocation);
    }

    @Override
    public void doSomething(final String param1, final boolean param2) {
        callRemote(new RemoteMethodInvocation<>("doSomething", String.class, new Object[]{param1, param2}));
    }
}
