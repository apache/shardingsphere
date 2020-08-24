/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.metrics.facade.executor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class MetricsThreadPoolExecutorTest {
    
    @Test
    public void assertInit() {
        MetricsThreadPoolExecutor executor = new MetricsThreadPoolExecutor("test", 8, 100);
        assertThat(executor.getName(), is("test"));
        assertThat(executor.getCorePoolSize(), is(8));
        assertThat(executor.getMaximumPoolSize(), is(8));
        assertThat(executor.getQueue().getClass().getName(), is(LinkedBlockingQueue.class.getName()));
        assertFalse(executor.isShutdown());
        assertThat(executor.getKeepAliveTime(TimeUnit.MILLISECONDS), is(0L));
        executor.shutdown();
    }
    
    @Test
    public void assertRejectedExecutionHandler() {
        MetricsThreadPoolExecutor executor = new MetricsThreadPoolExecutor("test", 1, 1);
        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
            
            });
        }
        executor.shutdown();
    }
}

