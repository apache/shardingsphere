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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.ImportSchemaConfigurationStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandler;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ImportSchemaConfigurationHandlerTest {
    
    private final String filePath = "/yaml/import/config-sharding.yaml";
    
    private final String schemaName = "sharding_db";
    
    private final String scalingName = "default_scaling";
    
    @Before
    public void init() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getAllSchemaNames()).thenReturn(Collections.singletonList(schemaName));
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(shardingSphereMetaData.getSchema()).thenReturn(new ShardingSphereSchema(createTableMap()));
        when(shardingSphereMetaData.getResource().getDataSources()).thenReturn(createDataSourceMap());
        when(shardingSphereMetaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singletonList(createShardingRuleConfiguration()));
        when(contextManager.getMetaDataContexts().getMetaData(schemaName)).thenReturn(shardingSphereMetaData);
        ProxyContext.getInstance().init(contextManager);
    }
    
    @Test
    public void assertImportSchemaExecutor() throws SQLException {
        Map<String, DataSource> dataSourceMap = ProxyContext.getInstance().getContextManager().getDataSourceMap(schemaName);
        assertNotNull(dataSourceMap);
        Collection<RuleConfiguration> ruleConfigurations = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(schemaName).getRuleMetaData().getConfigurations();
        assertNotNull(ruleConfigurations);
        ImportSchemaConfigurationHandler handler = new ImportSchemaConfigurationHandler().init(getParameter(createSqlStatement(), mockConnectionSession()));
        ResponseHeader responseHeader = handler.execute();
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(createTableRuleConfiguration());
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "ds_inline"));
        result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.getKeyGenerators().put("snowflake", new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", new Properties()));
        Properties props = new Properties();
        props.setProperty("algorithm-expression", "ds_${order_id % 2}");
        result.getShardingAlgorithms().put("ds_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props));
        result.setScalingName(scalingName);
        result.getScaling().put(scalingName, null);
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", createDataSource("demo_ds_0"));
        result.put("ds_1", createDataSource("demo_ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String dbName) {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl(String.format("jdbc:mysql://127.0.0.1:3306/%s?serverTimezone=UTC&useSSL=false", dbName));
        result.setUsername("root");
        result.setPassword("");
        result.setConnectionTimeout(30000L);
        result.setIdleTimeout(60000L);
        result.setMaxLifetime(1800000L);
        result.setMaximumPoolSize(50);
        result.setMinimumIdle(1);
        return result;
    }
    
    private Map<String, TableMetaData> createTableMap() {
        Map<String, TableMetaData> result = new HashMap<>(1, 1);
        List<ColumnMetaData> columns = Collections.singletonList(new ColumnMetaData("order_id", 0, false, false, false));
        List<IndexMetaData> indexes = Collections.singletonList(new IndexMetaData("primary"));
        result.put("t_order", new TableMetaData("t_order", columns, indexes, Collections.emptyList()));
        return result;
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        return result;
    }
    
    private ImportSchemaConfigurationStatement createSqlStatement() {
        ImportSchemaConfigurationStatement result = new ImportSchemaConfigurationStatement();
        result.setFilePath(Optional.of(Objects.requireNonNull(ImportSchemaConfigurationHandlerTest.class.getResource(filePath)).getPath()));
        return result;
    }
    
    private ConnectionSession mockConnectionSession() {
        return mock(ConnectionSession.class);
    }
    
    private RALBackendHandler.HandlerParameter<ImportSchemaConfigurationStatement> getParameter(final ImportSchemaConfigurationStatement statement, final ConnectionSession connectionSession) {
        return new RALBackendHandler.HandlerParameter<ImportSchemaConfigurationStatement>().setStatement(statement).setConnectionSession(connectionSession);
    }
}
