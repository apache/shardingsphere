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

package org.apache.shardingsphere.core.executor;

import lombok.RequiredArgsConstructor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ShardingExecuteEngineTest {
    
    private ShardingExecuteEngine shardingExecuteEngine = new ShardingExecuteEngine(10);
    
    private Collection<ShardingExecuteGroup<StatementExecuteUnit>> inputGroups;
    
    private MockGroupExecuteCallback firstCallback;
    
    private MockGroupExecuteCallback callback;
    
    private CountDownLatch latch;
    
    @Before
    public void setUp() {
        latch = new CountDownLatch(4);
        inputGroups = mockInputGroups(2, 2);
        firstCallback = new MockGroupExecuteCallback(latch);
        callback = new MockGroupExecuteCallback(latch);
    }
    
    @After
    public void tearDown() {
        shardingExecuteEngine.close();
    }
    
    private Collection<ShardingExecuteGroup<StatementExecuteUnit>> mockInputGroups(final int groupSize, final int unitSize) {
        Collection<ShardingExecuteGroup<StatementExecuteUnit>> result = new LinkedList<>();
        for (int i = 0; i < groupSize; i++) {
            List<StatementExecuteUnit> inputs = new LinkedList<>();
            for (int j = 0; j < unitSize; j++) {
                inputs.add(mock(StatementExecuteUnit.class));
            }
            result.add(new ShardingExecuteGroup<>(inputs));
        }
        return result;
    }
    
    @Test
    public void assertParallelExecuteWithoutFirstCallback() throws SQLException, InterruptedException {
        List<String> actual = shardingExecuteEngine.groupExecute(inputGroups, callback);
        latch.await();
        assertThat(actual.size(), is(4));
    }
    
    @Test
    public void assertParallelExecuteWithFirstCallback() throws SQLException, InterruptedException {
        List<String> actual = shardingExecuteEngine.groupExecute(inputGroups, firstCallback, callback, false);
        latch.await();
        assertThat(actual.size(), is(4));
    }
    
    @Test
    public void assertSerialExecute() throws SQLException, InterruptedException {
        List<String> actual = shardingExecuteEngine.groupExecute(inputGroups, firstCallback, callback, true);
        latch.await();
        assertThat(actual.size(), is(4));
    }
    
    @Test
    public void assertInputGroupIsEmpty() throws SQLException {
        CountDownLatch latch = new CountDownLatch(1);
        List<String> actual = shardingExecuteEngine.groupExecute(new LinkedList<ShardingExecuteGroup<StatementExecuteUnit>>(), new MockGroupExecuteCallback(latch));
        latch.countDown();
        assertThat(actual.size(), is(0));
    }
    
    @RequiredArgsConstructor
    private final class MockGroupExecuteCallback implements ShardingExecuteCallback<StatementExecuteUnit, String>, ShardingGroupExecuteCallback<StatementExecuteUnit, String> {
    
        private final CountDownLatch latch;
        
        @Override
        public String execute(final StatementExecuteUnit input, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) {
            latch.countDown();
            return "succeed";
        }
    
        @Override
        public Collection<String> execute(final Collection<StatementExecuteUnit> inputs, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) {
            List<String> result = new LinkedList<>();
            for (StatementExecuteUnit each : inputs) {
                latch.countDown();
                result.add("succeed");
            }
            return result;
        }
    }
}
