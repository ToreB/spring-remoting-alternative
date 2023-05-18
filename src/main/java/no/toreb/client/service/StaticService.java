package no.toreb.client.service;

import no.toreb.common.DataObject;
import no.toreb.common.RemoteMethodInvocation;
import no.toreb.common.RemoteService;

public class StaticService extends AbstractRemoteService implements RemoteService {

    private final RemoteMethodInvocation<String> helloMethodInvocation =
            new RemoteMethodInvocation<>("hello", String.class, new Class<?>[0], new Object[0]);
    private final RemoteMethodInvocation<String> byeMethodInvocation =
            new RemoteMethodInvocation<>("bye", String.class, new Class[0], new Object[0]);

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
        callRemote(new RemoteMethodInvocation<>("doSomething",
                                                String.class,
                                                new Class<?>[] {String.class, boolean.class},
                                                new Object[] {param1, param2}));
    }

    @Override
    public DataObject exchange(final DataObject dataObject) {
        return callRemote(new RemoteMethodInvocation<>("exchange",
                                                       DataObject.class,
                                                       new Class[] {DataObject.class},
                                                       new Object[] {dataObject}));
    }

    @Override
    public Object getSomething() {
        return callRemote(new RemoteMethodInvocation<Object>("getSomething",
                                                             Object.class,
                                                             new Class[0],
                                                             new Object[0]));
    }
}
