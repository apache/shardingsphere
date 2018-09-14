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

package io.shardingsphere.core.transport;

import org.apache.servicecomb.saga.transports.SQLTransport;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class JDBCTransportFactoryTest {
    
    private final JDBCTransportFactory jdbcTransportFactory = new JDBCTransportFactory();
    
    @Test
    public void assertGetTransport() {
        SQLTransport jdbcSqlTransport = jdbcTransportFactory.getTransport();
        assertNull(jdbcSqlTransport);
        
        jdbcTransportFactory.cacheTransport(null);
        jdbcSqlTransport = jdbcTransportFactory.getTransport();
        assertThat(jdbcSqlTransport, instanceOf(JDBCSqlTransport.class));
        
        jdbcTransportFactory.remove();
        jdbcSqlTransport = jdbcTransportFactory.getTransport();
        assertNull(jdbcSqlTransport);
    }
    
    @Test
    public void assertGetTransportWithMultiThread() {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            executorService.submit(new Tester());
        }
        executorService.shutdown();
    }
    
    private class Tester implements Runnable {
        @Override
        public void run() {
            assertGetTransport();
        }
    }
}
