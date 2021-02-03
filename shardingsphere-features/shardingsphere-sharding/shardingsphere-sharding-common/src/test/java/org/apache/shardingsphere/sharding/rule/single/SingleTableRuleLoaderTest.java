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

package org.apache.shardingsphere.sharding.rule.single;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SingleTableRuleLoaderTest {

    private static final String TABLE_TYPE = "TABLE";

    private static final String VIEW_TYPE = "VIEW";

    private static final String TABLE_NAME = "TABLE_NAME";

    private final Map<String, DataSource> dataSourceMap = new HashMap<>();

    @Mock
    private DatabaseType dbType;

    private DataSource initDataSource(final String dataSourceName, final Set<String> tables) throws SQLException {
        if (Strings.isNullOrEmpty(dataSourceName) || tables == null) {
            throw new IllegalArgumentException("dataSourceNam is empty or tables is null");
        }
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getCatalog()).thenReturn(dataSourceName);

        ResultSet resultSet = mock(ResultSet.class);
        List<String> tableList = Lists.newArrayList(tables);
        if (tableList.size() == 0) {
            when(resultSet.next()).thenReturn(false);
        } else if (tableList.size() == 1) {
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString(TABLE_NAME)).thenReturn(tableList.get(0));
        } else {
            List<String> subTableList = tableList.subList(1, tables.size());
            List<Boolean> subNextList = subTableList.stream()
                .map(item -> true)
                .collect(Collectors.toList());
            subNextList.add(false);
            String[] subTableArray = new String[subTableList.size()];
            Boolean[] subNextArray = new Boolean[subNextList.size()];
            subTableList.toArray(subTableArray);
            subNextList.toArray(subNextArray);
            when(resultSet.next()).thenReturn(true, subNextArray);
            when(resultSet.getString(TABLE_NAME)).thenReturn(tableList.get(0), subTableArray);
        }

        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(conn.getMetaData()).thenReturn(metaData);

        when(
            metaData.getTables(conn.getCatalog(), conn.getSchema(), null, new String[]{TABLE_TYPE, VIEW_TYPE})
        ).thenReturn(resultSet);
        return dataSource;
    }

    @Before
    public void init() throws SQLException {
        DataSource ds1 = initDataSource("ds1", Sets.newHashSet("employee", "dept", "salary"));
        DataSource ds2 = initDataSource("ds2", Sets.newHashSet("student", "teacher", "class", "salary"));
        dataSourceMap.put("ds1", ds1);
        dataSourceMap.put("ds2", ds2);
    }

    @Test
    public void assertLoad() {
        Map<String, SingleTableRule> singleTableRuleMap = SingleTableRuleLoader.load(dbType, dataSourceMap, Collections.emptyList());
        Set<String> tableSet = singleTableRuleMap.keySet();
        assertTrue(tableSet.contains("employee"));
        assertTrue(tableSet.contains("dept"));
        assertTrue(tableSet.contains("salary"));
        assertTrue(tableSet.contains("student"));
        assertTrue(tableSet.contains("teacher"));
        assertTrue(tableSet.contains("class"));
    }

    @Test
    public void assertLoadWithExcludeTable() {
        Map<String, SingleTableRule> singleTableRuleMap = SingleTableRuleLoader.load(dbType, dataSourceMap,
            Sets.newHashSet("salary", "employee", "student"));
        Set<String> tableSet = singleTableRuleMap.keySet();
        assertFalse(tableSet.contains("employee"));
        assertFalse(tableSet.contains("salary"));
        assertFalse(tableSet.contains("student"));
        assertTrue(tableSet.contains("dept"));
        assertTrue(tableSet.contains("teacher"));
        assertTrue(tableSet.contains("class"));
    }
}
