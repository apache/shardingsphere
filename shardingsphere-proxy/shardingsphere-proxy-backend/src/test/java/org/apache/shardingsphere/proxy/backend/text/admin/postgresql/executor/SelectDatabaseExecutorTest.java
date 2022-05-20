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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.parser.ParserConfiguration;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
public final class SelectDatabaseExecutorTest extends ProxyContextRestorer {
    
    private static final ResultSet RESULT_SET = mock(HikariProxyResultSet.class);
    
    private final ParserConfiguration parserConfig = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()).toParserConfiguration();
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException, SQLException {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new HashMap<>(), mock(ShardingSphereRuleMetaData.class),
                mock(OptimizerContext.class), new ConfigurationProperties(new Properties()));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
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
    
    private ShardingSphereDatabase getDatabase() throws SQLException {
        return new ShardingSphereDatabase("sharding_db", new PostgreSQLDatabaseType(),
                new ShardingSphereResource(mockDatasourceMap(), mockDataSourcesMetaData(), mock(CachedDatabaseMetaData.class), new PostgreSQLDatabaseType()),
                mock(ShardingSphereRuleMetaData.class), new ShardingSphereDatabaseMetaData(Collections.emptyMap()));
    }
    
    private ShardingSphereDatabase getEmptyDatabaseMetaData(final String schemaName) {
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ruleMetaData.getRules()).thenReturn(Collections.emptyList());
        return new ShardingSphereDatabase(schemaName, new PostgreSQLDatabaseType(),
                new ShardingSphereResource(Collections.emptyMap(), mockDataSourcesMetaData(), mock(CachedDatabaseMetaData.class), new PostgreSQLDatabaseType()),
                ruleMetaData, new ShardingSphereDatabaseMetaData(Collections.emptyMap()));
    }
    
    private Map<String, DataSource> mockDatasourceMap() throws SQLException {
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().prepareStatement(any(String.class)).executeQuery()).thenReturn(RESULT_SET);
        return Collections.singletonMap("ds_0", dataSource);
    }
    
    private DataSourcesMetaData mockDataSourcesMetaData() {
        DataSourcesMetaData result = mock(DataSourcesMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDataSourceMetaData("ds_0").getCatalog()).thenReturn("demo_ds_0");
        return result;
    }
    
    @Test
    public void assertSelectDatabaseExecute() throws SQLException {
        final String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate, d.datallowconn, pg_get_userbyid(d.datdba) AS databaseowner,"
                + " d.datcollate, d.datctype, shobj_description(d.oid, 'pg_database') AS description, d.datconnlimit, t.spcname, d.encoding, pg_encoding_to_char(d.encoding) AS encodingname "
                + "FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        final SQLStatement sqlStatement = new ShardingSphereSQLParserEngine("PostgreSQL", parserConfig).parse(sql, false);
        Map<String, String> mockResultSetMap = new HashMap<>();
        mockResultSetMap.put("databasename", "demo_ds_0");
        mockResultSetMap.put("databaseowner", "postgres");
        mockResultSetMap.put("datconnlimit", "-1");
        mockResultSetMap.put("datctype", "en_US.utf8");
        mockResultSet(mockResultSetMap);
        Map<String, ShardingSphereDatabase> databaseMap = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getDatabaseMap();
        databaseMap.put("sharding_db", getDatabase());
        databaseMap.put("test", getEmptyDatabaseMetaData("test"));
        SelectDatabaseExecutor selectDatabaseExecutor = new SelectDatabaseExecutor((SelectStatement) sqlStatement, sql);
        selectDatabaseExecutor.execute(mock(ConnectionSession.class));
        assertThat(selectDatabaseExecutor.getQueryResultMetaData().getColumnCount(), is(mockResultSetMap.size()));
        int count = 0;
        while (selectDatabaseExecutor.getMergedResult().next()) {
            count++;
            if ("sharding_db".equals(selectDatabaseExecutor.getMergedResult().getValue(1, String.class))) {
                assertThat(selectDatabaseExecutor.getMergedResult().getValue(2, String.class), is("postgres"));
                assertThat(selectDatabaseExecutor.getMergedResult().getValue(3, String.class), is("-1"));
            } else if ("test".equals(selectDatabaseExecutor.getMergedResult().getValue(1, String.class))) {
                assertThat(selectDatabaseExecutor.getMergedResult().getValue(2, String.class), is(""));
                assertThat(selectDatabaseExecutor.getMergedResult().getValue(3, String.class), is(""));
            } else {
                fail("expected : `sharding_db` or `test`");
            }
        }
        assertThat(count, is(2));
    }
    
    @Test
    public void assertSelectDatabaseWithoutDataSourceExecute() throws SQLException {
        final String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate, d.datallowconn, pg_get_userbyid(d.datdba) AS databaseowner, "
                + "d.datcollate, d.datctype, shobj_description(d.oid, 'pg_database') AS description, d.datconnlimit, t.spcname, d.encoding, pg_encoding_to_char(d.encoding) AS encodingname "
                + "FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        final SQLStatement sqlStatement = new ShardingSphereSQLParserEngine("PostgreSQL", parserConfig).parse(sql, false);
        Map<String, String> mockResultSetMap = new HashMap<>(4, 1);
        mockResultSetMap.put("databasename", "demo_ds_0");
        mockResultSetMap.put("databaseowner", "postgres");
        mockResultSetMap.put("datconnlimit", "-1");
        mockResultSetMap.put("datctype", "en_US.utf8");
        mockResultSet(mockResultSetMap);
        Map<String, ShardingSphereDatabase> databaseMap = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getDatabaseMap();
        databaseMap.put("sharding_db", getEmptyDatabaseMetaData("sharding_db"));
        SelectDatabaseExecutor selectDatabaseExecutor = new SelectDatabaseExecutor((SelectStatement) sqlStatement, sql);
        selectDatabaseExecutor.execute(mock(ConnectionSession.class));
        while (selectDatabaseExecutor.getMergedResult().next()) {
            assertThat(selectDatabaseExecutor.getMergedResult().getValue(1, String.class), is("sharding_db"));
        }
    }
    
    @Test
    public void assertSelectDatabaseWithoutDataSourceExecuteAndWithColumnProjectionSegment() throws SQLException {
        final String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        final SQLStatement sqlStatement = new ShardingSphereSQLParserEngine("PostgreSQL", parserConfig).parse(sql, false);
        Map<String, String> mockResultSetMap = new HashMap<>(4, 1);
        mockResultSetMap.put("databasename", "demo_ds_0");
        mockResultSetMap.put("databaseowner", "postgres");
        mockResultSetMap.put("datconnlimit", "-1");
        mockResultSetMap.put("datctype", "en_US.utf8");
        mockResultSet(mockResultSetMap);
        Map<String, ShardingSphereDatabase> databaseMap = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getDatabaseMap();
        databaseMap.put("sharding_db", getEmptyDatabaseMetaData("sharding_db"));
        SelectDatabaseExecutor selectDatabaseExecutor = new SelectDatabaseExecutor((SelectStatement) sqlStatement, sql);
        selectDatabaseExecutor.execute(mock(ConnectionSession.class));
        while (selectDatabaseExecutor.getMergedResult().next()) {
            assertThat(selectDatabaseExecutor.getMergedResult().getValue(1, String.class), is(""));
            assertThat(selectDatabaseExecutor.getMergedResult().getValue(2, String.class), is("sharding_db"));
            assertThat(selectDatabaseExecutor.getMergedResult().getValue(3, String.class), is(""));
            assertThat(selectDatabaseExecutor.getMergedResult().getValue(4, String.class), is(""));
        }
    }
    
    @Test
    public void assertSelectDatabaseInNoSchemaExecute() throws SQLException {
        String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        SQLStatement sqlStatement = new ShardingSphereSQLParserEngine("PostgreSQL", parserConfig).parse(sql, false);
        SelectDatabaseExecutor selectDatabaseExecutor = new SelectDatabaseExecutor((SelectStatement) sqlStatement, sql);
        selectDatabaseExecutor.execute(mock(ConnectionSession.class));
        assertThat(selectDatabaseExecutor.getQueryResultMetaData().getColumnCount(), is(0));
    }
}
