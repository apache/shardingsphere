/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.transport.base;

import io.shardingsphere.api.config.SagaConfiguration;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.apache.servicecomb.saga.transports.SQLTransport;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ProxyShardingTransportFactoryTest {
    
    private final ProxyShardingTransportFactory transportFactory = new ProxyShardingTransportFactory();
    
    private final SagaConfiguration config = new SagaConfiguration();
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Map<String, LogicSchema> logicSchemas = Collections.singletonMap(ShardingConstant.LOGIC_SCHEMA_NAME, mock(LogicSchema.class));
        Field field = GlobalRegistry.class.getDeclaredField("logicSchemas");
        field.setAccessible(true);
        field.set(GlobalRegistry.getInstance(), logicSchemas);
    }
    
    @Test
    public void assertGetTransport() {
        SQLTransport jdbcSqlTransport = transportFactory.getTransport();
        assertNull(jdbcSqlTransport);
        
        transportFactory.cacheTransport(new SagaTransactionEvent(null, ShardingConstant.LOGIC_SCHEMA_NAME, null, config));
        jdbcSqlTransport = transportFactory.getTransport();
        assertThat(jdbcSqlTransport, instanceOf(ProxySQLTransport.class));
        
        transportFactory.remove();
        jdbcSqlTransport = transportFactory.getTransport();
        assertNull(jdbcSqlTransport);
    }
    
    @Test
    public void assertGetTransportWithMultiThread() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Future<Boolean>> futures = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            futures.add(executorService.submit(new Tester()));
        }
        for (Future<Boolean> future : futures) {
            assertThat(future.get(), is(true));
        }
        executorService.shutdown();
    }
    
    private class Tester implements Callable<Boolean> {
        @Override
        public Boolean call() {
            assertGetTransport();
            return true;
        }
    }
}
