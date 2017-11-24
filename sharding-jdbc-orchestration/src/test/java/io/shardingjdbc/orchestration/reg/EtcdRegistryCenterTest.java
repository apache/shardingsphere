package io.shardingjdbc.orchestration.reg;

import io.shardingjdbc.orchestration.reg.base.DataChangedEvent;
import io.shardingjdbc.orchestration.reg.base.EventListener;
import io.shardingjdbc.orchestration.reg.etcd.EtcdRegistryCenter;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdClient;
import io.shardingjdbc.orchestration.reg.stub.EtcdClientStub;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EtcdRegistryCenterTest {
    private EtcdClient etcdClient;

    @Before
    public void beforeTest() {
        etcdClient = new EtcdClientStub();

        // uncomment below lines to test on real etcd
//        etcdClient = EtcdClientBuilder.newBuilder()
//                .endpoints("http://localhost:2379")
//                .maxRetry(1)
//                .timeout(200)
//                .span(200)
//                .build();
    }

    @Test
    public void testChangeListener() throws Exception {
        EtcdRegistryCenter registryCenter = new EtcdRegistryCenter("test", 2000, etcdClient);
        EventListener eventListener = mock(EventListener.class);
        registryCenter.watch("pms/abc", eventListener);
        registryCenter.persist("pms/abc/d", "100");
        TimeUnit.SECONDS.sleep(5);
        verify(eventListener, times(1)).onChange(ArgumentMatchers.argThat(new ArgumentMatcher<DataChangedEvent>() {
            
            @Override
            public boolean matches(final DataChangedEvent event) {
                return DataChangedEvent.Type.UPDATED == event.getEventType()
                        && event.getKey().equals("/test/pms/abc/d")
                        && event.getValue().equals("100");
            }
        }));
    }
}
