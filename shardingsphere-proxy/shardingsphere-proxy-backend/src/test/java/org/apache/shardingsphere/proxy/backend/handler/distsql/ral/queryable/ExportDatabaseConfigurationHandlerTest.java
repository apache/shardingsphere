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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ExportDatabaseConfigurationHandlerTest extends ProxyContextRestorer {
    
    @Before
    public void init() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        Map<String, ShardingSphereDatabase> databases = createDatabases();
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(databases);
        when(contextManager.getMetaDataContexts().getMetaData().containsDatabase("normal_db")).thenReturn(true);
        when(contextManager.getMetaDataContexts().getMetaData().containsDatabase("empty_db")).thenReturn(true);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("normal_db")).thenReturn(databases.get("normal_db"));
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("empty_db")).thenReturn(databases.get("empty_db"));
        ProxyContext.init(contextManager);
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(2, 1);
        result.put("normal_db", createNormalDatabase());
        result.put("empty_db", createEmptyDatabase());
        return result;
    }
    
    private ShardingSphereDatabase createNormalDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResource().getDataSources()).thenReturn(createDataSourceMap());
        when(result.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createShardingRuleConfiguration()));
        when(result.getSchema("normal_db")).thenReturn(new ShardingSphereSchema(createTables()));
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", createDataSource("demo_ds_0"));
        result.put("ds_1", createDataSource("demo_ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        MockedDataSource result = new MockedDataSource();
        result.setUrl(String.format("jdbc:mock://127.0.0.1/%s", name));
        result.setUsername("root");
        result.setPassword("");
        result.setMaxPoolSize(50);
        result.setMinPoolSize(1);
        return result;
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(createTableRuleConfiguration());
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "ds_inline"));
        result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.getKeyGenerators().put("snowflake", new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        result.getShardingAlgorithms().put("ds_inline", new AlgorithmConfiguration("INLINE", createProperties()));
        String scalingName = "default_scaling";
        result.setScalingName(scalingName);
        result.getScaling().put(scalingName, null);
        return result;
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        return result;
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("algorithm-expression", "ds_${order_id % 2}");
        return result;
    }
    
    private Map<String, ShardingSphereTable> createTables() {
        Collection<ShardingSphereColumn> columns = Collections.singleton(new ShardingSphereColumn("order_id", 0, false, false, false, true));
        Collection<ShardingSphereIndex> indexes = Collections.singleton(new ShardingSphereIndex("primary"));
        return Collections.singletonMap("t_order", new ShardingSphereTable("t_order", columns, indexes, Collections.emptyList()));
    }
    
    private ShardingSphereDatabase createEmptyDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResource().getDataSources()).thenReturn(Collections.emptyMap());
        when(result.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        return result;
    }
    
    @Test
    public void assertExecute() throws SQLException {
        ExportDatabaseConfigurationHandler handler = new ExportDatabaseConfigurationHandler();
        handler.init(new ExportDatabaseConfigurationStatement(new DatabaseSegment(0, 0, new IdentifierValue("normal_db")), null), mock(ConnectionSession.class));
        assertQueryResponseHeader((QueryResponseHeader) handler.execute());
        assertTrue(handler.next());
        assertRowData(handler.getRowData().getData());
        assertFalse(handler.next());
    }
    
    private void assertQueryResponseHeader(final QueryResponseHeader actual) {
        assertThat(actual.getQueryHeaders().size(), is(1));
        assertQueryHeader(actual.getQueryHeaders().get(0));
    }
    
    private void assertQueryHeader(final QueryHeader actual) {
        assertThat(actual.getSchema(), is(""));
        assertThat(actual.getTable(), is(""));
        assertThat(actual.getColumnLabel(), is("result"));
        assertThat(actual.getColumnName(), is("result"));
        assertThat(actual.getColumnType(), is(1));
        assertThat(actual.getColumnTypeName(), is("CHAR"));
        assertThat(actual.getColumnLength(), is(255));
        assertThat(actual.getDecimals(), is(0));
        assertFalse(actual.isSigned());
        assertFalse(actual.isPrimaryKey());
        assertFalse(actual.isNotNull());
        assertFalse(actual.isAutoIncrement());
    }
    
    private void assertRowData(final Collection<Object> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(loadExpectedRow()));
    }
    
    @SneakyThrows(IOException.class)
    private String loadExpectedRow() {
        StringBuilder result = new StringBuilder();
        String fileName = Objects.requireNonNull(ExportDatabaseConfigurationHandlerTest.class.getResource("/expected/export-database-configuration.yaml")).getFile();
        try (
                FileReader fileReader = new FileReader(fileName);
                BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                if (!line.startsWith("#") && !"".equals(line.trim())) {
                    result.append(line).append(System.lineSeparator());
                }
            }
        }
        return result.toString();
    }
    
    @Test
    public void assertExecuteWithEmptyDatabase() throws SQLException {
        ExportDatabaseConfigurationHandler handler = new ExportDatabaseConfigurationHandler();
        handler.init(new ExportDatabaseConfigurationStatement(new DatabaseSegment(0, 0, new IdentifierValue("empty_db")), null), mock(ConnectionSession.class));
        assertQueryResponseHeader((QueryResponseHeader) handler.execute());
        assertTrue(handler.next());
        Collection<Object> rowData = handler.getRowData().getData();
        assertThat(rowData.size(), is(1));
        assertThat(rowData.iterator().next(), is("databaseName: empty_db" + System.lineSeparator()));
        assertFalse(handler.next());
    }
    
    @Test(expected = UnknownDatabaseException.class)
    public void assertExecuteWithNotExistedDatabase() throws SQLException {
        ExportDatabaseConfigurationHandler handler = new ExportDatabaseConfigurationHandler();
        handler.init(new ExportDatabaseConfigurationStatement(new DatabaseSegment(0, 0, new IdentifierValue("not_exist_db")), null), mock(ConnectionSession.class));
        handler.execute();
    }
    
    @Test(expected = NoDatabaseSelectedException.class)
    public void assertExecuteWithNoDatabaseSelected() throws SQLException {
        ExportDatabaseConfigurationHandler handler = new ExportDatabaseConfigurationHandler();
        handler.init(new ExportDatabaseConfigurationStatement(null, null), mock(ConnectionSession.class));
        handler.execute();
    }
}
