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

package io.shardingsphere.core.executor;

import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.executor.fixture.EventCaller;
import io.shardingsphere.core.executor.fixture.ExecutorTestUtil;
import io.shardingsphere.core.executor.fixture.TestDMLExecutionEventListener;
import io.shardingsphere.core.executor.fixture.TestDQLExecutionEventListener;
import io.shardingsphere.core.executor.fixture.TestOverallExecutionEventListener;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractBaseExecutorTest {
    
    private ShardingExecuteEngine executeEngine;
    
    private SQLExecuteTemplate executeTemplate;
    
    @Mock
    private EventCaller eventCaller;
    
    private TestDQLExecutionEventListener dqlExecutionEventListener;
    
    private TestDMLExecutionEventListener dmlExecutionEventListener;
    
    private TestOverallExecutionEventListener overallExecutionEventListener;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ExecutorExceptionHandler.setExceptionThrown(false);
        executeEngine = new ShardingExecuteEngine(Runtime.getRuntime().availableProcessors());
        executeTemplate = new SQLExecuteTemplate(executeEngine);
        overallExecutionEventListener = new TestOverallExecutionEventListener(eventCaller);
        dqlExecutionEventListener = new TestDQLExecutionEventListener(eventCaller);
        dmlExecutionEventListener = new TestDMLExecutionEventListener(eventCaller);
        ShardingEventBusInstance.getInstance().register(overallExecutionEventListener);
        ShardingEventBusInstance.getInstance().register(dqlExecutionEventListener);
        ShardingEventBusInstance.getInstance().register(dmlExecutionEventListener);
    }
    
    @After
    public void tearDown() throws ReflectiveOperationException {
        ExecutorTestUtil.clear();
        ShardingEventBusInstance.getInstance().unregister(overallExecutionEventListener);
        ShardingEventBusInstance.getInstance().unregister(dqlExecutionEventListener);
        ShardingEventBusInstance.getInstance().unregister(dmlExecutionEventListener);
        executeEngine.close();
    }
}
