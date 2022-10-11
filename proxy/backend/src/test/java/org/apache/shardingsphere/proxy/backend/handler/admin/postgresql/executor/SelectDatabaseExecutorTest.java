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
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResources;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SelectDatabaseExecutorTest extends ProxyContextRestorer {
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    @Before
    public void setUp() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(new HashMap<>(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())), new ShardingSphereData());
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    @Test
    public void assertSelectDatabaseExecute() throws SQLException {
        addShardingDatabase();
        addEmptyDatabase();
        String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate, d.datallowconn, pg_get_userbyid(d.datdba) AS databaseowner,"
                + " d.datcollate, d.datctype, shobj_description(d.oid, 'pg_database') AS description, d.datconnlimit, t.spcname, d.encoding, pg_encoding_to_char(d.encoding) AS encodingname "
                + "FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        SelectDatabaseExecutor selectDatabaseExecutor = new SelectDatabaseExecutor((SelectStatement) sqlParserRule.getSQLParserEngine("PostgreSQL").parse(sql, false), sql);
        selectDatabaseExecutor.execute(mock(ConnectionSession.class));
        assertThat(selectDatabaseExecutor.getQueryResultMetaData().getColumnCount(), is(4));
        int count = 0;
        while (selectDatabaseExecutor.getMergedResult().next()) {
            count++;
            if ("sharding_db".equals(selectDatabaseExecutor.getMergedResult().getValue(1, String.class))) {
                assertThat(selectDatabaseExecutor.getMergedResult().getValue(2, String.class), is("postgres"));
                assertThat(selectDatabaseExecutor.getMergedResult().getValue(3, String.class), is("-1"));
            } else if ("empty_db".equals(selectDatabaseExecutor.getMergedResult().getValue(1, String.class))) {
                assertThat(selectDatabaseExecutor.getMergedResult().getValue(2, String.class), is(""));
                assertThat(selectDatabaseExecutor.getMergedResult().getValue(3, String.class), is(""));
            } else {
                fail("expected : `sharding_db` or `empty_db`");
            }
        }
        assertThat(count, is(2));
    }
    
    private void addShardingDatabase() throws SQLException {
        Map<String, ShardingSphereDatabase> databases = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases();
        databases.put("sharding_db", new ShardingSphereDatabase("sharding_db", new PostgreSQLDatabaseType(),
                new ShardingSphereResources("sharding_db", Collections.singletonMap("foo_ds", new MockedDataSource(mockConnection()))), mock(ShardingSphereRuleMetaData.class),
                Collections.emptyMap()));
    }
    
    private void addEmptyDatabase() {
        Map<String, ShardingSphereDatabase> databases = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases();
        databases.put("empty_db", new ShardingSphereDatabase("empty_db", new PostgreSQLDatabaseType(),
                new ShardingSphereResources("sharding_db", Collections.emptyMap()), new ShardingSphereRuleMetaData(Collections.emptyList()), Collections.emptyMap()));
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
    
    @Test
    public void assertSelectDatabaseWithoutDataSourceExecute() throws SQLException {
        addEmptyDatabase();
        String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate, d.datallowconn, pg_get_userbyid(d.datdba) AS databaseowner, "
                + "d.datcollate, d.datctype, shobj_description(d.oid, 'pg_database') AS description, d.datconnlimit, t.spcname, d.encoding, pg_encoding_to_char(d.encoding) AS encodingname "
                + "FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        SelectDatabaseExecutor selectDatabaseExecutor = new SelectDatabaseExecutor((SelectStatement) sqlParserRule.getSQLParserEngine("PostgreSQL").parse(sql, false), sql);
        selectDatabaseExecutor.execute(mock(ConnectionSession.class));
        while (selectDatabaseExecutor.getMergedResult().next()) {
            assertThat(selectDatabaseExecutor.getMergedResult().getValue(1, String.class), is("empty_db"));
        }
    }
    
    @Test
    public void assertSelectDatabaseWithoutDataSourceExecuteAndWithColumnProjectionSegment() throws SQLException {
        addEmptyDatabase();
        String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        SelectDatabaseExecutor selectDatabaseExecutor = new SelectDatabaseExecutor((SelectStatement) sqlParserRule.getSQLParserEngine("PostgreSQL").parse(sql, false), sql);
        selectDatabaseExecutor.execute(mock(ConnectionSession.class));
        while (selectDatabaseExecutor.getMergedResult().next()) {
            assertThat(selectDatabaseExecutor.getMergedResult().getValue(1, String.class), is(""));
            assertThat(selectDatabaseExecutor.getMergedResult().getValue(2, String.class), is("empty_db"));
            assertThat(selectDatabaseExecutor.getMergedResult().getValue(3, String.class), is(""));
            assertThat(selectDatabaseExecutor.getMergedResult().getValue(4, String.class), is(""));
        }
    }
    
    @Test
    public void assertSelectDatabaseInNoSchemaExecute() throws SQLException {
        String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        SelectDatabaseExecutor selectDatabaseExecutor = new SelectDatabaseExecutor((SelectStatement) sqlParserRule.getSQLParserEngine("PostgreSQL").parse(sql, false), sql);
        selectDatabaseExecutor.execute(mock(ConnectionSession.class));
        assertThat(selectDatabaseExecutor.getQueryResultMetaData().getColumnCount(), is(0));
    }
}
