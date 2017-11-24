package io.shardingjdbc.orchestration.reg;

import io.shardingjdbc.orchestration.reg.base.ChangeEvent;
import io.shardingjdbc.orchestration.reg.base.ChangeListener;
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
        ChangeListener changeListener = mock(ChangeListener.class);
        registryCenter.watch("pms/abc", changeListener);
        registryCenter.persist("pms/abc/d", "100");
        TimeUnit.SECONDS.sleep(5);
        verify(changeListener, times(1)).onChange(ArgumentMatchers.argThat(new ArgumentMatcher<ChangeEvent>() {
            
            @Override
            public boolean matches(final ChangeEvent event) {
                return ChangeEvent.Type.UPDATED == event.getEventType()
                        && event.getKey().equals("/test/pms/abc/d")
                        && event.getValue().equals("100");
            }
        }));
    }
}
