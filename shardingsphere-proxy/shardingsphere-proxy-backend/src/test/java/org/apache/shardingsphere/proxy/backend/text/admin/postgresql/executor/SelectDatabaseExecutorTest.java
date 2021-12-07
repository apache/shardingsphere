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
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCConnectionSession;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
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
public final class SelectDatabaseExecutorTest {
    
    private static final ResultSet RESULT_SET = mock(HikariProxyResultSet.class);

    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException, SQLException {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new HashMap<>(), mock(ShardingSphereRuleMetaData.class), 
                mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizerContext.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
    }
    
    private void mockResultSet(final Map<String, String> mockMap, final Boolean... values) throws SQLException {
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
    
    private ShardingSphereMetaData getMetaData() throws SQLException {
        return new ShardingSphereMetaData("sharding_db",
                new ShardingSphereResource(mockDatasourceMap(), mockDataSourcesMetaData(), mock(CachedDatabaseMetaData.class), mock(MySQLDatabaseType.class)),
                mock(ShardingSphereRuleMetaData.class), mock(ShardingSphereSchema.class)
        );
    }
    
    private Map<String, DataSource> mockDatasourceMap() throws SQLException {
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().prepareStatement(any(String.class)).executeQuery()).thenReturn(RESULT_SET);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", dataSource);
        return dataSourceMap;
    }
    
    private DataSourcesMetaData mockDataSourcesMetaData() {
        DataSourcesMetaData meta = mock(DataSourcesMetaData.class, RETURNS_DEEP_STUBS);
        when(meta.getDataSourceMetaData("ds_0").getCatalog()).thenReturn("demo_ds_0");
        return meta;
    }
    
    @Test
    public void assertSelectSchemataExecute() throws SQLException {
        final String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate, d.datallowconn, pg_get_userbyid(d.datdba) AS databaseowner," 
                + " d.datcollate, d.datctype, shobj_description(d.oid, 'pg_database') AS description, d.datconnlimit, t.spcname, d.encoding, pg_encoding_to_char(d.encoding) AS encodingname " 
                + "FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        final SQLStatement sqlStatement = new ShardingSphereSQLParserEngine("MySQL", sqlParserRule).parse(sql, false);
        Map<String, String> mockResultSetMap = new HashMap<>();
        mockResultSetMap.put("databasename", "demo_ds_0");
        mockResultSetMap.put("databaseowner", "postgres");
        mockResultSetMap.put("datconnlimit", "-1");
        mockResultSetMap.put("datctype", "en_US.utf8");
        mockResultSet(mockResultSetMap, true, false);
        Map<String, ShardingSphereMetaData> metaDataMap = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataMap();
        metaDataMap.put("sharding_db", getMetaData());
        metaDataMap.put("test", mock(ShardingSphereMetaData.class));
        SelectDatabaseExecutor selectSchemataExecutor = new SelectDatabaseExecutor((SelectStatement) sqlStatement, sql);
        selectSchemataExecutor.execute(mock(JDBCConnectionSession.class));
        assertThat(selectSchemataExecutor.getQueryResultMetaData().getColumnCount(), is(mockResultSetMap.size()));
        int count = 0;
        while (selectSchemataExecutor.getMergedResult().next()) {
            count++;
            if ("sharding_db".equals(selectSchemataExecutor.getMergedResult().getValue(1, String.class))) {
                assertThat(selectSchemataExecutor.getMergedResult().getValue(2, String.class), is("postgres"));
                assertThat(selectSchemataExecutor.getMergedResult().getValue(3, String.class), is("-1"));
            } else if ("test".equals(selectSchemataExecutor.getMergedResult().getValue(1, String.class))) {
                assertThat(selectSchemataExecutor.getMergedResult().getValue(2, String.class), is(""));
                assertThat(selectSchemataExecutor.getMergedResult().getValue(3, String.class), is(""));
            } else {
                fail("expected : `sharding_db` or `test`");
            }
        }
        assertThat(count, is(2));
    }
    
    @Test
    public void assertSelectSchemataWithoutDataSourceExecute1() throws SQLException {
        final String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate, d.datallowconn, pg_get_userbyid(d.datdba) AS databaseowner, " 
                + "d.datcollate, d.datctype, shobj_description(d.oid, 'pg_database') AS description, d.datconnlimit, t.spcname, d.encoding, pg_encoding_to_char(d.encoding) AS encodingname " 
                + "FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        final SQLStatement sqlStatement = new ShardingSphereSQLParserEngine("MySQL", sqlParserRule).parse(sql, false);
        Map<String, String> mockResultSetMap = new HashMap<>();
        mockResultSetMap.put("databasename", "demo_ds_0");
        mockResultSetMap.put("databaseowner", "postgres");
        mockResultSetMap.put("datconnlimit", "-1");
        mockResultSetMap.put("datctype", "en_US.utf8");
        mockResultSet(mockResultSetMap, false);
        Map<String, ShardingSphereMetaData> metaDataMap = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataMap();
        metaDataMap.put("sharding_db", mock(ShardingSphereMetaData.class));
        SelectDatabaseExecutor selectSchemataExecutor = new SelectDatabaseExecutor((SelectStatement) sqlStatement, sql);
        selectSchemataExecutor.execute(mock(JDBCConnectionSession.class));
        while (selectSchemataExecutor.getMergedResult().next()) {
            assertThat(selectSchemataExecutor.getMergedResult().getValue(1, String.class), is("sharding_db"));
        }
    }
    
    @Test
    public void assertSelectSchemataWithoutDataSourceExecuteAndWithColumnProjectionSegment() throws SQLException {
        final String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        final SQLStatement sqlStatement = new ShardingSphereSQLParserEngine("MySQL", sqlParserRule).parse(sql, false);
        Map<String, String> mockResultSetMap = new HashMap<>();
        mockResultSetMap.put("databasename", "demo_ds_0");
        mockResultSetMap.put("databaseowner", "postgres");
        mockResultSetMap.put("datconnlimit", "-1");
        mockResultSetMap.put("datctype", "en_US.utf8");
        mockResultSet(mockResultSetMap, false);
        Map<String, ShardingSphereMetaData> metaDataMap = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataMap();
        metaDataMap.put("sharding_db", mock(ShardingSphereMetaData.class));
        SelectDatabaseExecutor selectSchemataExecutor = new SelectDatabaseExecutor((SelectStatement) sqlStatement, sql);
        selectSchemataExecutor.execute(mock(JDBCConnectionSession.class));
        while (selectSchemataExecutor.getMergedResult().next()) {
            assertThat(selectSchemataExecutor.getMergedResult().getValue(1, String.class), is(""));
            assertThat(selectSchemataExecutor.getMergedResult().getValue(2, String.class), is("sharding_db"));
            assertThat(selectSchemataExecutor.getMergedResult().getValue(3, String.class), is(""));
            assertThat(selectSchemataExecutor.getMergedResult().getValue(4, String.class), is(""));
        }
    }
    
    @Test
    public void assertSelectSchemataInNoSchemaExecute() throws SQLException {
        final String sql = "SELECT d.oid, d.datname AS databasename, d.datacl, d.datistemplate FROM pg_database d LEFT JOIN pg_tablespace t ON d.dattablespace = t.oid;";
        final SQLStatement sqlStatement = new ShardingSphereSQLParserEngine("MySQL", sqlParserRule).parse(sql, false);
        SelectDatabaseExecutor selectSchemataExecutor = new SelectDatabaseExecutor((SelectStatement) sqlStatement, sql);
        selectSchemataExecutor.execute(mock(JDBCConnectionSession.class));
        assertThat(selectSchemataExecutor.getQueryResultMetaData().getColumnCount(), is(0));
    }
}
