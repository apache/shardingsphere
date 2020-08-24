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

package org.apache.shardingsphere.driver.executor.batch;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class BatchExecutionUnitTest {
    
    private static final String DATA_SOURCE_NAME = "ds";
    
    private static final String SQL = "SELECT * FROM table WHERE id = ?";
    
    @Test
    public void assertGetParameterSets() {
        BatchExecutionUnit batchExecutionUnit = new BatchExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit(SQL, Lists.newArrayList(1))));
        List<List<Object>> actual = batchExecutionUnit.getParameterSets();
        assertThat(actual.size(), is(1));
        assertTrue(actual.get(0).isEmpty());
        batchExecutionUnit.mapAddBatchCount(0);
        actual = batchExecutionUnit.getParameterSets();
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).size(), is(1));
        assertThat(actual.get(0).get(0), is(1));
    }
    
    @Test
    public void assertEquals() {
        BatchExecutionUnit actual = new BatchExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit(SQL, Lists.newArrayList(1))));
        BatchExecutionUnit expected = new BatchExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit(SQL, Lists.newArrayList(2))));
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertToString() {
        BatchExecutionUnit actual = new BatchExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit(SQL, Lists.newArrayList(1))));
        assertThat(actual.toString(), is(String.format("BatchExecutionUnit(executionUnit=ExecutionUnit"
                + "(dataSourceName=%s, sqlUnit=SQLUnit(sql=%s, parameters=[%d])), jdbcAndActualAddBatchCallTimesMap={}, actualCallAddBatchTimes=0)", DATA_SOURCE_NAME, SQL, 1)));
    }
}
