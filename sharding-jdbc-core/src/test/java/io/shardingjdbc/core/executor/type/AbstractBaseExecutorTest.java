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

package io.shardingjdbc.core.executor.type;

import io.shardingjdbc.core.executor.ExecutorEngine;
import io.shardingjdbc.core.executor.fixture.EventCaller;
import io.shardingjdbc.core.executor.fixture.ExecutorTestUtil;
import io.shardingjdbc.core.executor.fixture.TestDMLExecutionEventListener;
import io.shardingjdbc.core.executor.fixture.TestDQLExecutionEventListener;
import io.shardingjdbc.core.executor.fixture.TestOverallExecutionEventListener;
import io.shardingjdbc.core.executor.threadlocal.ExecutorExceptionHandler;
import io.shardingjdbc.core.util.EventBusInstance;
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
    
    private TestOverallExecutionEventListener overallExecutionEventListener;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ExecutorExceptionHandler.setExceptionThrown(false);
        executorEngine = new ExecutorEngine(Runtime.getRuntime().availableProcessors());
        overallExecutionEventListener = new TestOverallExecutionEventListener(eventCaller);
        dqlExecutionEventListener = new TestDQLExecutionEventListener(eventCaller);
        dmlExecutionEventListener = new TestDMLExecutionEventListener(eventCaller);
        EventBusInstance.getInstance().register(overallExecutionEventListener);
        EventBusInstance.getInstance().register(dqlExecutionEventListener);
        EventBusInstance.getInstance().register(dmlExecutionEventListener);
    }
    
    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        ExecutorTestUtil.clear();
        EventBusInstance.getInstance().unregister(overallExecutionEventListener);
        EventBusInstance.getInstance().unregister(dqlExecutionEventListener);
        EventBusInstance.getInstance().unregister(dmlExecutionEventListener);
        executorEngine.close();
    }
}
