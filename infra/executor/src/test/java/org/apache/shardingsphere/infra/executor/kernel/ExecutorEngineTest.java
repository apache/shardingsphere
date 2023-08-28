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

package org.apache.shardingsphere.infra.executor.kernel;

import org.apache.shardingsphere.infra.executor.kernel.fixture.ExecutorCallbackFixture;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class ExecutorEngineTest {
    
    private final ExecutorEngine executorEngine = ExecutorEngine.createExecutorEngineWithSize(10);
    
    private final CountDownLatch latch = new CountDownLatch(4);
    
    private ExecutionGroupContext<Object> executionGroupContext;
    
    private ExecutorCallbackFixture firstCallback;
    
    private ExecutorCallbackFixture callback;
    
    @BeforeEach
    void setUp() {
        executionGroupContext = createMockedExecutionGroups(2, 2);
        firstCallback = new ExecutorCallbackFixture(latch);
        callback = new ExecutorCallbackFixture(latch);
    }
    
    @AfterEach
    void tearDown() {
        executorEngine.close();
    }
    
    private ExecutionGroupContext<Object> createMockedExecutionGroups(final int groupSize, final int unitSize) {
        Collection<ExecutionGroup<Object>> result = new LinkedList<>();
        for (int i = 0; i < groupSize; i++) {
            result.add(new ExecutionGroup<>(createMockedInputs(unitSize)));
        }
        return new ExecutionGroupContext<>(result, mock(ExecutionGroupReportContext.class));
    }
    
    private List<Object> createMockedInputs(final int size) {
        List<Object> result = new LinkedList<>();
        for (int j = 0; j < size; j++) {
            result.add(mock(Object.class));
        }
        return result;
    }
    
    @Test
    void assertParallelExecuteWithFirstCallback() throws SQLException, InterruptedException {
        List<String> actual = executorEngine.execute(executionGroupContext, firstCallback, callback, false);
        latch.await();
        assertThat(actual.size(), is(4));
    }
    
    @Test
    void assertSerialExecute() throws SQLException, InterruptedException {
        List<String> actual = executorEngine.execute(executionGroupContext, firstCallback, callback, true);
        latch.await();
        assertThat(actual.size(), is(4));
    }
}
