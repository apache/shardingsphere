/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.executor.type;

import com.dangdang.ddframe.rdb.sharding.executor.ExecutorEngine;
import com.dangdang.ddframe.rdb.sharding.executor.fixture.EventCaller;
import com.dangdang.ddframe.rdb.sharding.executor.fixture.ExecutorTestUtil;
import com.dangdang.ddframe.rdb.sharding.executor.fixture.TestDMLExecutionEventListener;
import com.dangdang.ddframe.rdb.sharding.executor.fixture.TestDQLExecutionEventListener;
import com.dangdang.ddframe.rdb.sharding.executor.threadlocal.ExecutorExceptionHandler;
import com.dangdang.ddframe.rdb.sharding.util.EventBusInstance;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractBaseExecutorTest {
    
    private ExecutorEngine executorEngine;
    
    @Mock
    private EventCaller eventCaller;
    
    private TestDQLExecutionEventListener dqlExecutionEventListener;
    
    private TestDMLExecutionEventListener dmlExecutionEventListener;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ExecutorExceptionHandler.setExceptionThrown(false);
        executorEngine = new ExecutorEngine(Runtime.getRuntime().availableProcessors());
        dqlExecutionEventListener = new TestDQLExecutionEventListener(eventCaller);
        dmlExecutionEventListener = new TestDMLExecutionEventListener(eventCaller);
        EventBusInstance.getInstance().register(dqlExecutionEventListener);
        EventBusInstance.getInstance().register(dmlExecutionEventListener);
    }
    
    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        ExecutorTestUtil.clear();
        EventBusInstance.getInstance().unregister(dqlExecutionEventListener);
        EventBusInstance.getInstance().unregister(dmlExecutionEventListener);
        executorEngine.close();
    }
}
