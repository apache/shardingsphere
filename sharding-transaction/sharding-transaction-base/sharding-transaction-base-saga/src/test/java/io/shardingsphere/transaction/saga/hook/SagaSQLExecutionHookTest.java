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

package io.shardingsphere.transaction.saga.hook;

import com.google.common.collect.Lists;
import io.shardingsphere.core.executor.ShardingExecuteDataMap;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.transaction.core.constant.ExecutionResult;
import io.shardingsphere.transaction.saga.SagaSubTransaction;
import io.shardingsphere.transaction.saga.SagaTransaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SagaSQLExecutionHookTest {
    
    private final SagaSQLExecutionHook sagaSQLExecutionHook = new SagaSQLExecutionHook();
    
    private final RouteUnit routeUnit = new RouteUnit("", new SQLUnit("", Lists.<List<Object>>newArrayList()));
    
    @Mock
    private SagaTransaction sagaTransaction;
    
    @Before
    public void setUp() {
        ShardingExecuteDataMap.setDataMap(new HashMap<String, Object>() {{ put("transaction", sagaTransaction); }});
    }
    
    @Test
    public void assertStart() {
        sagaSQLExecutionHook.start(routeUnit, null, true, ShardingExecuteDataMap.getDataMap());
        verify(sagaTransaction).recordResult(any(SagaSubTransaction.class), eq(ExecutionResult.EXECUTING));
    }
    
    @Test
    public void assertFinishSuccess() {
        sagaSQLExecutionHook.start(routeUnit, null, true, ShardingExecuteDataMap.getDataMap());
        sagaSQLExecutionHook.finishSuccess();
        verify(sagaTransaction).recordResult(any(SagaSubTransaction.class), eq(ExecutionResult.SUCCESS));
    }
    
    @Test
    public void assertFinishFailure() {
        sagaSQLExecutionHook.start(routeUnit, null, true, ShardingExecuteDataMap.getDataMap());
        sagaSQLExecutionHook.finishFailure(new RuntimeException());
        verify(sagaTransaction).recordResult(any(SagaSubTransaction.class), eq(ExecutionResult.FAILURE));
        assertFalse(ExecutorExceptionHandler.isExceptionThrown());
    }
}