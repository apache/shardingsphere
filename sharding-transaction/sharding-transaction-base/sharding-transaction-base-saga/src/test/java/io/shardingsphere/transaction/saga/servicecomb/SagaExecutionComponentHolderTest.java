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

package io.shardingsphere.transaction.saga.servicecomb;

import io.shardingsphere.transaction.saga.SagaConfiguration;
import org.apache.servicecomb.saga.core.application.SagaExecutionComponent;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;

public class SagaExecutionComponentHolderTest {
    
    private final SagaConfiguration sagaConfiguration = new SagaConfiguration();
    
    private final SagaExecutionComponentHolder holder = new SagaExecutionComponentHolder();
    
    private Map<String, SagaExecutionComponent> sagaCaches;
    
    private Map<String, ExecutorService> executorCaches;
    
    @Before
    public void setUp() throws Exception {
        Field sagaCachesField = SagaExecutionComponentHolder.class.getDeclaredField("sagaCaches");
        sagaCachesField.setAccessible(true);
        sagaCaches = (Map<String, SagaExecutionComponent>) sagaCachesField.get(holder);
        Field executorCachesField = SagaExecutionComponentHolder.class.getDeclaredField("executorCaches");
        executorCachesField.setAccessible(true);
        executorCaches = (Map<String, ExecutorService>) executorCachesField.get(holder);
    }
    
    @Test
    public void assertGetSagaExecutionComponent() {
        SagaExecutionComponent sagaExecutionComponent = holder.getSagaExecutionComponent(sagaConfiguration);
        assertEquals(sagaCaches.size(), 1);
        assertEquals(executorCaches.size(), 1);
        assertEquals(holder.getSagaExecutionComponent(sagaConfiguration), sagaExecutionComponent);
    }
    
    @Test
    public void assertRemoveSagaExecutionComponent() {
        holder.getSagaExecutionComponent(sagaConfiguration);
        assertEquals(sagaCaches.size(), 1);
        assertEquals(executorCaches.size(), 1);
        holder.removeSagaExecutionComponent(sagaConfiguration);
        assertEquals(sagaCaches.size(), 0);
        assertEquals(executorCaches.size(), 0);
    }
}