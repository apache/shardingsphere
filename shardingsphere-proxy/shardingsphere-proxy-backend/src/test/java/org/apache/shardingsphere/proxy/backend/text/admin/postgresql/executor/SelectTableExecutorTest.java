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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor;

import com.zaxxer.hikari.pool.HikariProxyResultSet;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
    
    private static final ResultSet RESULT_SET = mock(HikariProxyResultSet.class);
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException, SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new HashMap<>(), mock(ShardingSphereRuleMetaData.class),
                mock(OptimizerContext.class), new ConfigurationProperties(new Properties()));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private ShardingSphereDatabase getDatabaseMetaData() throws SQLException {
        return new ShardingSphereDatabase("sharding_db", new PostgreSQLDatabaseType(), new ShardingSphereResource(mockDatasourceMap()),
                mock(ShardingSphereRuleMetaData.class), Collections.singletonMap("public", new ShardingSphereSchema(Collections.singletonMap("t_order", mock(TableMetaData.class)))));
    }
    
    private Map<String, DataSource> mockDatasourceMap() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mysql://localhost:3306/foo_ds");
        when(connection.prepareStatement(any(String.class)).executeQuery()).thenReturn(RESULT_SET);
        return Collections.singletonMap("ds_0", new MockedDataSource(connection));
    }
    
    @Test
    public void assertSelectSchemataExecute() throws SQLException {
        final String sql = "SELECT c.oid, n.nspname AS schemaname, c.relname AS tablename from pg_tablespace";
        Map<String, String> mockResultSetMap = new LinkedHashMap<>();
        mockResultSetMap.put("tablename", "t_order_1");
        mockResultSetMap.put("c.oid", "0000");
        mockResultSetMap.put("schemaname", "public");
        mockResultSet(mockResultSetMap);
        Map<String, ShardingSphereDatabase> databaseMap = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getDatabaseMap();
        databaseMap.put("public", getDatabaseMetaData());
        SelectTableExecutor selectSchemataExecutor = new SelectTableExecutor(sql);
        selectSchemataExecutor.execute(mock(ConnectionSession.class));
        assertThat(selectSchemataExecutor.getQueryResultMetaData().getColumnCount(), is(mockResultSetMap.size()));
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
    
    private void mockResultSet(final Map<String, String> mockMap) throws SQLException {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        List<String> keys = new ArrayList<>(mockMap.keySet());
        for (int i = 0; i < keys.size(); i++) {
            when(metaData.getColumnName(i + 1)).thenReturn(keys.get(i));
            when(metaData.getColumnLabel(i + 1)).thenReturn(keys.get(i));
            when(RESULT_SET.getString(i + 1)).thenReturn(mockMap.get(keys.get(i)));
        }
        when(RESULT_SET.next()).thenReturn(true, false);
        when(metaData.getColumnCount()).thenReturn(mockMap.size());
        when(RESULT_SET.getMetaData()).thenReturn(metaData);
    }
}
