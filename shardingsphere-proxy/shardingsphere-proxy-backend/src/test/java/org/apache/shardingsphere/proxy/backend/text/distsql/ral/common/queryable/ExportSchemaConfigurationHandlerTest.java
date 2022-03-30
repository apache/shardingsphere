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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ExportSchemaConfigurationStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandler.HandlerParameter;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ExportSchemaConfigurationHandlerTest {
    
    @Before
    public void init() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getAllSchemaNames()).thenReturn(Collections.singletonList("sharding_db"));
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(shardingSphereMetaData.getDefaultSchema()).thenReturn(new ShardingSphereSchema(createTableMap()));
        when(shardingSphereMetaData.getResource().getDataSources()).thenReturn(createDataSourceMap());
        when(shardingSphereMetaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singletonList(createShardingRuleConfiguration()));
        when(contextManager.getMetaDataContexts().getMetaData("sharding_db")).thenReturn(shardingSphereMetaData);
        ProxyContext.getInstance().init(contextManager);
    }
    
    @Test
    public void assertExportSchemaExecutor() throws SQLException {
        ExportSchemaConfigurationHandler handler = new ExportSchemaConfigurationHandler().init(getParameter(createSqlStatement(), mockConnectionSession()));
        handler.execute();
        handler.next();
        List<Object> data = new ArrayList<>(handler.getRowData());
        assertThat(data.size(), is(1));
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
        String scalingName = "default_scaling";
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
    
    private ExportSchemaConfigurationStatement createSqlStatement() {
        return new ExportSchemaConfigurationStatement(new SchemaSegment(0, 0, new IdentifierValue("sharding_db")), Optional.empty());
    }
    
    private ConnectionSession mockConnectionSession() {
        return mock(ConnectionSession.class);
    }
    
    private HandlerParameter<ExportSchemaConfigurationStatement> getParameter(final ExportSchemaConfigurationStatement statement, final ConnectionSession connectionSession) {
        return new HandlerParameter<ExportSchemaConfigurationStatement>().setStatement(statement).setConnectionSession(connectionSession);
    }
}
