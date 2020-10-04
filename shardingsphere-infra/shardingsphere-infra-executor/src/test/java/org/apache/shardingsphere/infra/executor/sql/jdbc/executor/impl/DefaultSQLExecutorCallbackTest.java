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

package org.apache.shardingsphere.infra.executor.sql.jdbc.executor.impl;

import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.SQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.impl.DefaultSQLExecutorCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DefaultSQLExecutorCallbackTest {
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData metaData;
    
    private Collection<StatementExecuteUnit> units;
    
    @Before
    public void setUp() throws SQLException {
        when(preparedStatement.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/test");
        units = Collections.singletonList(
                new StatementExecuteUnit(new ExecutionUnit("ds", new SQLUnit("SELECT now()", Collections.emptyList())), ConnectionMode.CONNECTION_STRICTLY, preparedStatement));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecute() throws SQLException, NoSuchFieldException, IllegalAccessException {
        SQLExecutorCallback<?> sqlExecutorCallback = new DefaultSQLExecutorCallback<Integer>(DatabaseTypes.getActualDatabaseType("MySQL"), true) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return ((PreparedStatement) statement).executeUpdate();
            }
        };
        Field field = DefaultSQLExecutorCallback.class.getDeclaredField("CACHED_DATASOURCE_METADATA");
        field.setAccessible(true);
        Map<String, DataSourceMetaData> cachedDataSourceMetaData = (Map<String, DataSourceMetaData>) field.get(sqlExecutorCallback);
        assertThat(cachedDataSourceMetaData.size(), is(0));
        sqlExecutorCallback.execute(units, true, null);
        assertThat(cachedDataSourceMetaData.size(), is(1));
        sqlExecutorCallback.execute(units, true, null);
        assertThat(cachedDataSourceMetaData.size(), is(1));
    }
}
