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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
public final class SelectDatabaseExecutorTest {
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    @Test
    public void assertSelectDatabaseExecute() throws SQLException {
        String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate, d.datallowconn, pg_get_userbyid(d.datdba) AS databaseowner,"
                + " d.datcollate, d.datctype, shobj_description(d.oid, 'pg_database') AS description, d.datconnlimit, t.spcname, d.encoding, pg_encoding_to_char(d.encoding) AS encodingname "
                + "FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        ShardingSphereDatabase shardingDatabase = createShardingDatabase();
        ShardingSphereDatabase emptyDatabase = createEmptyDatabase();
        ContextManager contextManager = mockContextManager(shardingDatabase, emptyDatabase);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Arrays.asList("sharding_db", "empty_db"));
        when(ProxyContext.getInstance().getDatabase("sharding_db")).thenReturn(shardingDatabase);
        when(ProxyContext.getInstance().getDatabase("empty_db")).thenReturn(emptyDatabase);
        SelectDatabaseExecutor executor = new SelectDatabaseExecutor((SelectStatement) sqlParserRule.getSQLParserEngine("PostgreSQL").parse(sql, false), sql);
        executor.execute(mock(ConnectionSession.class));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(4));
        int count = 0;
        while (executor.getMergedResult().next()) {
            count++;
            if ("sharding_db".equals(executor.getMergedResult().getValue(1, String.class))) {
                assertThat(executor.getMergedResult().getValue(2, String.class), is("postgres"));
                assertThat(executor.getMergedResult().getValue(3, String.class), is("-1"));
            } else if ("empty_db".equals(executor.getMergedResult().getValue(1, String.class))) {
                assertThat(executor.getMergedResult().getValue(2, String.class), is(""));
                assertThat(executor.getMergedResult().getValue(3, String.class), is(""));
            } else {
                fail("expected : `sharding_db` or `empty_db`");
            }
        }
        assertThat(count, is(2));
    }
    
    @Test
    public void assertSelectDatabaseWithoutDataSourceExecute() throws SQLException {
        String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate, d.datallowconn, pg_get_userbyid(d.datdba) AS databaseowner, "
                + "d.datcollate, d.datctype, shobj_description(d.oid, 'pg_database') AS description, d.datconnlimit, t.spcname, d.encoding, pg_encoding_to_char(d.encoding) AS encodingname "
                + "FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        ContextManager contextManager = mockContextManager(createEmptyDatabase());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("empty_db"));
        SelectDatabaseExecutor executor = new SelectDatabaseExecutor((SelectStatement) sqlParserRule.getSQLParserEngine("PostgreSQL").parse(sql, false), sql);
        executor.execute(mock(ConnectionSession.class));
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, String.class), is("empty_db"));
        }
    }
    
    @Test
    public void assertSelectDatabaseWithoutDataSourceExecuteAndWithColumnProjectionSegment() throws SQLException {
        String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        ContextManager contextManager = mockContextManager(createEmptyDatabase());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("empty_db"));
        SelectDatabaseExecutor executor = new SelectDatabaseExecutor((SelectStatement) sqlParserRule.getSQLParserEngine("PostgreSQL").parse(sql, false), sql);
        executor.execute(mock(ConnectionSession.class));
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, String.class), is(""));
            assertThat(executor.getMergedResult().getValue(2, String.class), is("empty_db"));
            assertThat(executor.getMergedResult().getValue(3, String.class), is(""));
            assertThat(executor.getMergedResult().getValue(4, String.class), is(""));
        }
    }
    
    @Test
    public void assertSelectDatabaseInNoSchemaExecute() throws SQLException {
        String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.emptyList());
        SelectDatabaseExecutor executor = new SelectDatabaseExecutor((SelectStatement) sqlParserRule.getSQLParserEngine("PostgreSQL").parse(sql, false), sql);
        executor.execute(mock(ConnectionSession.class));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(0));
    }
    
    private static ContextManager mockContextManager(final ShardingSphereDatabase... databases) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(Arrays.stream(databases).collect(Collectors.toMap(ShardingSphereDatabase::getName, each -> each, (key, value) -> value)),
                        mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private ShardingSphereDatabase createShardingDatabase() throws SQLException {
        return new ShardingSphereDatabase("sharding_db", new PostgreSQLDatabaseType(),
                new ShardingSphereResourceMetaData("sharding_db", Collections.singletonMap("foo_ds", new MockedDataSource(mockConnection()))), mock(ShardingSphereRuleMetaData.class),
                Collections.emptyMap());
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
        Map<String, String> result = new HashMap<>();
        result.put("databasename", "foo_ds");
        result.put("databaseowner", "postgres");
        result.put("datconnlimit", "-1");
        result.put("datctype", "en_US.utf8");
        return result;
    }
    
    private ShardingSphereDatabase createEmptyDatabase() {
        return new ShardingSphereDatabase("empty_db", new PostgreSQLDatabaseType(),
                new ShardingSphereResourceMetaData("empty_db", Collections.emptyMap()), new ShardingSphereRuleMetaData(Collections.emptyList()), Collections.emptyMap());
    }
}
