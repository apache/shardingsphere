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

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.AbstractDatabaseMetadataExecutor.DefaultDatabaseMetadataExecutor;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

@RunWith(MockitoJUnitRunner.class)
public final class SelectInformationExecutorTest extends ProxyContextRestorer {
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Before
    public void setUp() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(new HashMap<>(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())), mock(OptimizerContext.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        when(connectionSession.getGrantee()).thenReturn(new Grantee("root", "127.0.0.1"));
    }
    
    @Test
    public void assertSelectSchemataExecute() throws SQLException {
        Map<String, String> expectedResultSetMap = new HashMap<>();
        expectedResultSetMap.put("SCHEMA_NAME", "foo_ds");
        expectedResultSetMap.put("DEFAULT_CHARACTER_SET_NAME", "utf8mb4_0900_ai_ci");
        expectedResultSetMap.put("DEFAULT_COLLATION_NAME", "utf8mb4");
        Map<String, ShardingSphereDatabase> databases = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases();
        databases.put("sharding_db", createDatabase(expectedResultSetMap));
        databases.put("database_without_authority", createEmptyDatabase("database_without_authority"));
        String sql = "SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA";
        SelectInformationSchemataExecutor executor = new SelectInformationSchemataExecutor((SelectStatement) sqlParserRule.getSQLParserEngine("MySQL").parse(sql, false), sql);
        executor.execute(connectionSession);
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(expectedResultSetMap.size()));
        int count = 0;
        while (executor.getMergedResult().next()) {
            count++;
            if ("sharding_db".equals(executor.getMergedResult().getValue(1, String.class))) {
                assertThat(executor.getMergedResult().getValue(2, String.class), is("utf8mb4"));
                assertThat(executor.getMergedResult().getValue(3, String.class), is("utf8mb4_0900_ai_ci"));
            } else {
                fail("expected : `sharding_db`");
            }
        }
        assertThat(count, is(1));
    }
    
    @Test
    public void assertSelectSchemataInSchemaWithoutDataSourceExecute() throws SQLException {
        Map<String, ShardingSphereDatabase> databases = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases();
        databases.put("empty_db", createEmptyDatabase("empty_db"));
        String sql = "SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME, DEFAULT_ENCRYPTION FROM information_schema.SCHEMATA";
        SelectInformationSchemataExecutor executor = new SelectInformationSchemataExecutor((SelectStatement) sqlParserRule.getSQLParserEngine("MySQL").parse(sql, false), sql);
        executor.execute(connectionSession);
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(4));
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, String.class), is("empty_db"));
            assertThat(executor.getMergedResult().getValue(2, String.class), is(""));
            assertThat(executor.getMergedResult().getValue(3, String.class), is(""));
            assertThat(executor.getMergedResult().getValue(4, String.class), is(""));
        }
    }
    
    @Test
    public void assertSelectSchemataInNoSchemaExecute() throws SQLException {
        String sql = "SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME, DEFAULT_ENCRYPTION FROM information_schema.SCHEMATA";
        SelectInformationSchemataExecutor executor = new SelectInformationSchemataExecutor((SelectStatement) sqlParserRule.getSQLParserEngine("MySQL").parse(sql, false), sql);
        executor.execute(connectionSession);
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(0));
    }
    
    @Test
    public void assertSelectSchemaAliasExecute() throws SQLException {
        Map<String, String> expectedResultSetMap = new HashMap<>();
        expectedResultSetMap.put("sn", "foo_ds");
        expectedResultSetMap.put("DEFAULT_CHARACTER_SET_NAME", "utf8mb4");
        Map<String, ShardingSphereDatabase> databases = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases();
        databases.put("foo_ds", createDatabase(expectedResultSetMap));
        databases.put("empty_db", createEmptyDatabase("empty_db"));
        String sql = "SELECT SCHEMA_NAME AS sn, DEFAULT_CHARACTER_SET_NAME FROM information_schema.SCHEMATA";
        DefaultDatabaseMetadataExecutor executor = new DefaultDatabaseMetadataExecutor(sql);
        executor.execute(connectionSession);
        assertThat(executor.getRows().get(0).get("sn"), is("foo_ds"));
        assertThat(executor.getRows().get(0).get("DEFAULT_CHARACTER_SET_NAME"), is("utf8mb4"));
    }
    
    @Test
    public void assertDefaultExecute() throws SQLException {
        Map<String, ShardingSphereDatabase> databases = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases();
        databases.put("sharding_db", createDatabase(Collections.singletonMap("support_ndb", "0")));
        String sql = "SELECT COUNT(*) AS support_ndb FROM information_schema.ENGINES WHERE Engine = 'ndbcluster'";
        DefaultDatabaseMetadataExecutor executor = new DefaultDatabaseMetadataExecutor(sql);
        executor.execute(connectionSession);
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, String.class), is("0"));
        }
    }
    
    private ShardingSphereDatabase createDatabase(final Map<String, String> expectedResultSetMap) throws SQLException {
        return new ShardingSphereDatabase("sharding_db", new MySQLDatabaseType(), new ShardingSphereResource(
                Collections.singletonMap("foo_ds", new MockedDataSource(mockConnection(expectedResultSetMap)))), mock(ShardingSphereRuleMetaData.class), Collections.emptyMap());
    }
    
    private ShardingSphereDatabase createEmptyDatabase(final String databaseName) {
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(mock(AuthorityRule.class, RETURNS_DEEP_STUBS)));
        return new ShardingSphereDatabase(databaseName, new MySQLDatabaseType(), new ShardingSphereResource(Collections.emptyMap()), ruleMetaData, Collections.emptyMap());
    }
    
    private Connection mockConnection(final Map<String, String> expectedResultSetMap) throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getURL()).thenReturn("jdbc:mysql://localhost:3306/foo_ds");
        ResultSet resultSet = mockResultSet(expectedResultSetMap);
        when(result.prepareStatement(any(String.class)).executeQuery()).thenReturn(resultSet);
        return result;
    }
    
    private ResultSet mockResultSet(final Map<String, String> expectedResultSetMap) throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
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
}
