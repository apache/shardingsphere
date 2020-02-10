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

package org.apache.shardingsphere.core.shard.log;

import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.inOrder;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingSQLLoggerTest {
    
    private static final String SQL = "SELECT * FROM t_user";
    
    private Collection<String> dataSourceNames;
    
    private Collection<ExecutionUnit> executionUnits;
    
    @Mock
    private Logger logger;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        this.dataSourceNames = Arrays.asList("db1", "db2", "db3");
        this.executionUnits = mockExecutionUnits(dataSourceNames, SQL);
        Field field = ShardingSQLLogger.class.getDeclaredField("log");
        setFinalStatic(field, logger);
    }
    
    @Test
    public void assertLogSQLShard() {
        ShardingSQLLogger.logSQL(SQL, false, null, executionUnits);
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).info("Rule Type: sharding", new Object[]{});
        inOrder.verify(logger).info("Logic SQL: {}", new Object[]{SQL});
        inOrder.verify(logger).info("SQLStatement: {}", new Object[]{null});
        inOrder.verify(logger).info("Actual SQL: {} ::: {}", new Object[]{"db1", SQL});
        inOrder.verify(logger).info("Actual SQL: {} ::: {}", new Object[]{"db2", SQL});
        inOrder.verify(logger).info("Actual SQL: {} ::: {}", new Object[]{"db3", SQL});
    }
    
    @Test
    public void assertLogSQLShardWithParameters() {
        List<Object> parameters = executionUnits.iterator().next().getSqlUnit().getParameters();
        parameters.add("parameter");
        ShardingSQLLogger.logSQL(SQL, false, null, executionUnits);
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).info("Rule Type: sharding", new Object[]{});
        inOrder.verify(logger).info("Logic SQL: {}", new Object[]{SQL});
        inOrder.verify(logger).info("SQLStatement: {}", new Object[]{null});
        inOrder.verify(logger).info("Actual SQL: {} ::: {} ::: {}", "db1", SQL, parameters);
        inOrder.verify(logger).info("Actual SQL: {} ::: {}", new Object[]{"db2", SQL});
        inOrder.verify(logger).info("Actual SQL: {} ::: {}", new Object[]{"db3", SQL});
    }
    
    @Test
    public void assertLogSQLShardSimple() {
        ShardingSQLLogger.logSQL(SQL, true, null, executionUnits);
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).info("Rule Type: sharding", new Object[]{});
        inOrder.verify(logger).info("Logic SQL: {}", new Object[]{SQL});
        inOrder.verify(logger).info("SQLStatement: {}", new Object[]{null});
        inOrder.verify(logger).info("Actual SQL(simple): {} ::: {}", new Object[]{buildDataSourceNamesSet(), executionUnits.size()});
    }
    
    private Set<String> buildDataSourceNamesSet() {
        Set<String> dataSourceNamesSet = new HashSet<>(executionUnits.size());
        for (ExecutionUnit each : executionUnits) {
            dataSourceNamesSet.add(each.getDataSourceName());
        }
        return dataSourceNamesSet;
    }
    
    private Collection<ExecutionUnit> mockExecutionUnits(final Collection<String> dataSourceNames, final String sql) {
        List<ExecutionUnit> result = new LinkedList<>();
        for (String dsName : dataSourceNames) {
            result.addAll(mockOneShard(dsName, 1, sql));
        }
        return result;
    }
    
    private Collection<ExecutionUnit> mockOneShard(final String dataSourceName, final int size, final String sql) {
        Collection<ExecutionUnit> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(new ExecutionUnit(dataSourceName, new SQLUnit(sql, new ArrayList<>())));
        }
        return result;
    }
    
    private static void setFinalStatic(final Field field, final Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
