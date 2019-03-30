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

package org.apache.shardingsphere.core.route;

import com.google.common.base.Joiner;
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
public final class SQLLoggerTest {

    private String sql;

    private Collection<String> dataSourceNames;

    private Collection<RouteUnit> routeUnits;

    @Mock
    private Logger logger;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        this.sql = "select * from user";
        this.dataSourceNames = Arrays.asList("db1", "db2", "db3");
        this.routeUnits = mockRouteUnits(dataSourceNames,sql);
        Field field = SQLLogger.class.getDeclaredField("log");
        setFinalStatic(field, logger);
    }

    @Test
    public void assertlogSQLShard() {
        SQLLogger.logSQL(sql, false, null, routeUnits);
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).info("Rule Type: sharding", new Object[]{});
        inOrder.verify(logger).info("Logic SQL: {}", new Object[]{sql});
        inOrder.verify(logger).info("SQLStatement: {}",new Object[]{null});
        inOrder.verify(logger).info("Actual SQL: {} ::: {}",new Object[]{"db1",sql});
        inOrder.verify(logger).info("Actual SQL: {} ::: {}",new Object[]{"db2",sql});
        inOrder.verify(logger).info("Actual SQL: {} ::: {}",new Object[]{"db3",sql});
    }

    @Test
    public void assertlogSQLShardSimple() {
        SQLLogger.logSQL(sql, true, null, routeUnits);
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).info("Rule Type: sharding", new Object[]{});
        inOrder.verify(logger).info("Logic SQL: {}", new Object[]{sql});
        inOrder.verify(logger).info("SQLStatement: {}",new Object[]{null});
        inOrder.verify(logger).info("Actual SQL(simple): {} ::: {}",new Object[]{buildDataSourceNamesSet(),routeUnits.size()});
    }

    @Test
    public void assertlogSQLMasterSlave() {
        SQLLogger.logSQL(sql, dataSourceNames);
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).info("Rule Type: master-slave",new Object[]{});
        inOrder.verify(logger).info("SQL: {} ::: DataSources: {}",new Object[]{sql,Joiner.on(",").join(dataSourceNames)});
    }

    private Set<String> buildDataSourceNamesSet() {
        Set<String> dataSourceNamesSet = new HashSet<>(routeUnits.size());
        for (RouteUnit each : routeUnits) {
            dataSourceNamesSet.add(each.getDataSourceName());
        }
        return dataSourceNamesSet;
    }

    private Collection<RouteUnit> mockRouteUnits(Collection<String> dataSourceNames, String sql) {
        List<RouteUnit> results = new LinkedList<>();
        for (String dsName : dataSourceNames) {
            results.addAll(mockOneShard(dsName, 1, sql));
        }
        return results;
    }

    private Collection<RouteUnit> mockOneShard(final String dsName, final int size, String sql) {
        Collection<RouteUnit> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(new RouteUnit(dsName, new SQLUnit(sql, new ArrayList<>())));
        }
        return result;
    }

    private static void setFinalStatic(Field field, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
