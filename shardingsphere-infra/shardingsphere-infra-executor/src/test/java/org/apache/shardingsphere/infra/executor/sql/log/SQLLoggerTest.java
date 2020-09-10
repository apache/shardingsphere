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

package org.apache.shardingsphere.infra.executor.sql.log;

import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
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
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class SQLLoggerTest {
    
    private static final String SQL = "SELECT * FROM t_user";
    
    private Collection<ExecutionUnit> executionUnits;
    
    @Mock
    private Logger logger;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        executionUnits = mockExecutionUnits(Arrays.asList("db1", "db2", "db3"), SQL);
        setFinalStatic(SQLLogger.class.getDeclaredField("log"), logger);
    }
    
    private Collection<ExecutionUnit> mockExecutionUnits(final Collection<String> dataSourceNames, final String sql) {
        return dataSourceNames.stream().map(each -> new ExecutionUnit(each, new SQLUnit(sql, new ArrayList<>()))).collect(Collectors.toList());
    }
    
    @Test
    public void assertLogNormalSQLWithoutParameter() {
        SQLLogger.logSQL(SQL, false, new ExecutionContext(null, executionUnits, mock(RouteContext.class)));
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).info("Logic SQL: {}", new Object[]{SQL});
        inOrder.verify(logger).info("SQLStatement: {}", new Object[]{null});
        inOrder.verify(logger).info("Actual SQL: {} ::: {}", new Object[]{"db1", SQL});
        inOrder.verify(logger).info("Actual SQL: {} ::: {}", new Object[]{"db2", SQL});
        inOrder.verify(logger).info("Actual SQL: {} ::: {}", new Object[]{"db3", SQL});
    }
    
    @Test
    public void assertLogNormalSQLWithParameters() {
        List<Object> parameters = executionUnits.iterator().next().getSqlUnit().getParameters();
        parameters.add("parameter");
        SQLLogger.logSQL(SQL, false, new ExecutionContext(null, executionUnits, mock(RouteContext.class)));
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).info("Logic SQL: {}", new Object[]{SQL});
        inOrder.verify(logger).info("SQLStatement: {}", new Object[]{null});
        inOrder.verify(logger).info("Actual SQL: {} ::: {} ::: {}", "db1", SQL, parameters);
        inOrder.verify(logger).info("Actual SQL: {} ::: {}", new Object[]{"db2", SQL});
        inOrder.verify(logger).info("Actual SQL: {} ::: {}", new Object[]{"db3", SQL});
    }
    
    @Test
    public void assertLogSimpleSQL() {
        SQLLogger.logSQL(SQL, true, new ExecutionContext(null, executionUnits, mock(RouteContext.class)));
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).info("Logic SQL: {}", new Object[]{SQL});
        inOrder.verify(logger).info("SQLStatement: {}", new Object[]{null});
        inOrder.verify(logger).info("Actual SQL(simple): {} ::: {}", new Object[]{buildDataSourceNames(), executionUnits.size()});
    }
    
    private Collection<String> buildDataSourceNames() {
        return executionUnits.stream().map(ExecutionUnit::getDataSourceName).collect(Collectors.toCollection(() -> new HashSet<>(executionUnits.size())));
    }
    
    private static void setFinalStatic(final Field field, final Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
