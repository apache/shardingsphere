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

package org.apache.shardingsphere.proxy.backend.handler.admin.postgresql.executor;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SelectTableExecutorTest extends ProxyContextRestorer {
    
    @Before
    public void setUp() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(new HashMap<>(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    @Test
    public void assertSelectSchemataExecute() throws SQLException {
        Map<String, ShardingSphereDatabase> databases = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases();
        databases.put("public", createDatabase());
        String sql = "SELECT c.oid, n.nspname AS schemaname, c.relname AS tablename from pg_tablespace";
        SelectTableExecutor selectSchemataExecutor = new SelectTableExecutor(sql);
        selectSchemataExecutor.execute(mock(ConnectionSession.class));
        assertThat(selectSchemataExecutor.getQueryResultMetaData().getColumnCount(), is(3));
        int count = 0;
        while (selectSchemataExecutor.getMergedResult().next()) {
            count++;
            if ("t_order".equals(selectSchemataExecutor.getMergedResult().getValue(1, String.class))) {
                assertThat(selectSchemataExecutor.getMergedResult().getValue(2, String.class), is("0000"));
                assertThat(selectSchemataExecutor.getMergedResult().getValue(3, String.class), is("public"));
            } else {
                fail("expected : `t_order`");
            }
        }
        assertThat(count, is(1));
    }
    
    private ShardingSphereDatabase createDatabase() throws SQLException {
        return new ShardingSphereDatabase("public", new PostgreSQLDatabaseType(), new ShardingSphereResource("sharding_db", Collections.singletonMap("foo_ds", new MockedDataSource(mockConnection()))),
                new ShardingSphereRuleMetaData(Collections.emptyList()), Collections.singletonMap("public",
                        new ShardingSphereSchema(Collections.singletonMap("t_order", mock(ShardingSphereTable.class)), Collections.emptyMap())));
    }
    
    private Connection mockConnection() throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getURL()).thenReturn("jdbc:mysql://localhost:3306/foo_ds");
        ResultSet resultSet = mockResultSet();
        when(result.prepareStatement(any(String.class)).executeQuery()).thenReturn(resultSet);
        return result;
    }
    
    private ResultSet mockResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        Map<String, String> expectedResultSetMap = createExpectedResultSetMap();
        List<String> keys = new ArrayList<>(expectedResultSetMap.keySet());
        for (int i = 0; i < keys.size(); i++) {
            when(result.getMetaData().getColumnName(i + 1)).thenReturn(keys.get(i));
            when(result.getMetaData().getColumnLabel(i + 1)).thenReturn(keys.get(i));
            when(result.getString(i + 1)).thenReturn(expectedResultSetMap.get(keys.get(i)));
        }
        when(result.next()).thenReturn(true, false);
        when(result.getMetaData().getColumnCount()).thenReturn(expectedResultSetMap.size());
        return result;
    }
    
    private Map<String, String> createExpectedResultSetMap() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("tablename", "t_order_1");
        result.put("c.oid", "0000");
        result.put("schemaname", "public");
        return result;
    }
}
