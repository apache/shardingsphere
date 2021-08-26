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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.information;

import com.zaxxer.hikari.pool.HikariProxyResultSet;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.enums.InformationSchemataEnum;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SelectSchemataExecutorTest {
    
    private static final String SQL = "SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA";
    
    private SelectSchemataExecutor selectSchemataExecutor;
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException, SQLException {
        SQLStatement sqlStatement = new ShardingSphereSQLParserEngine("MySQL").parse(SQL, false);
        selectSchemataExecutor = new SelectSchemataExecutor((SelectStatement) sqlStatement, SQL);
        ResultSet resultSet = mock(HikariProxyResultSet.class);
        when(resultSet.getString(InformationSchemataEnum.SCHEMA_NAME.name())).thenReturn("demo_ds_0");
        when(resultSet.getString(InformationSchemataEnum.DEFAULT_CHARACTER_SET_NAME.name())).thenReturn("utf8mb4");
        when(resultSet.getString(InformationSchemataEnum.DEFAULT_COLLATION_NAME.name())).thenReturn("utf8mb4_0900_ai_ci");
        when(resultSet.next()).thenReturn(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getAllSchemaNames()).thenReturn(Collections.singletonList("sharding_db"));
        when(contextManager.getMetaDataContexts().getMetaData("sharding_db").getResource().getDataSourcesMetaData().getDataSourceMetaData("ds_0").getCatalog()).thenReturn("demo_ds_0");
        Map<String, DataSource> datasourceMap = mockDatasourceMap(resultSet);
        when(contextManager.getMetaDataContexts().getMetaData("sharding_db").getResource().getDataSources()).thenReturn(datasourceMap);
        ProxyContext.getInstance().init(contextManager);
    }
    
    private Map<String, DataSource> mockDatasourceMap(final ResultSet resultSet) throws SQLException {
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().prepareStatement(SQL).executeQuery()).thenReturn(resultSet);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", dataSource);
        return dataSourceMap;
    }
    
    @Test
    public void assertExecute() throws SQLException {
        selectSchemataExecutor.execute(mockBackendConnection());
        assertThat(selectSchemataExecutor.getQueryResultMetaData().getColumnCount(), is(3));
        while (selectSchemataExecutor.getMergedResult().next()) {
            assertThat(selectSchemataExecutor.getMergedResult().getValue(1, String.class), is("sharding_db"));
            assertThat(selectSchemataExecutor.getMergedResult().getValue(2, String.class), is("utf8mb4_0900_ai_ci"));
            assertThat(selectSchemataExecutor.getMergedResult().getValue(3, String.class), is("utf8mb4"));
        }
    }
    
    private BackendConnection mockBackendConnection() {
        BackendConnection result = mock(BackendConnection.class);
        return result;
    }
}
