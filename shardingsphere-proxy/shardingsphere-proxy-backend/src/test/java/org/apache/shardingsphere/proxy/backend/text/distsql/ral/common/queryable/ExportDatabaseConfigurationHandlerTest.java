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
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandler.HandlerParameter;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ExportDatabaseConfigurationHandlerTest extends ProxyContextRestorer {
    
    @Before
    public void init() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchemas().get("sharding_db")).thenReturn(new ShardingSphereSchema(createTableMap()));
        when(database.getResource().getDataSources()).thenReturn(createDataSourceMap());
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.singletonList(createShardingRuleConfiguration()));
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap("sharding_db", database));
        when(contextManager.getMetaDataContexts().getDatabase("sharding_db")).thenReturn(database);
        ProxyContext.init(contextManager);
    }
    
    @Test
    public void assertExportDatabaseExecutor() throws SQLException {
        ExportDatabaseConfigurationHandler handler = new ExportDatabaseConfigurationHandler().init(createParameter(createSQLStatement(), mock(ConnectionSession.class)));
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
        result.getShardingAlgorithms().put("ds_inline", new ShardingSphereAlgorithmConfiguration("INLINE", createProperties()));
        String scalingName = "default_scaling";
        result.setScalingName(scalingName);
        result.getScaling().put(scalingName, null);
        return result;
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("algorithm-expression", "ds_${order_id % 2}");
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
        List<ColumnMetaData> columns = Collections.singletonList(new ColumnMetaData("order_id", 0, false, false, false));
        List<IndexMetaData> indexes = Collections.singletonList(new IndexMetaData("primary"));
        return Collections.singletonMap("t_order", new TableMetaData("t_order", columns, indexes, Collections.emptyList()));
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        return result;
    }
    
    private ExportDatabaseConfigurationStatement createSQLStatement() {
        return new ExportDatabaseConfigurationStatement(new DatabaseSegment(0, 0, new IdentifierValue("sharding_db")), null);
    }
    
    private HandlerParameter<ExportDatabaseConfigurationStatement> createParameter(final ExportDatabaseConfigurationStatement statement, final ConnectionSession connectionSession) {
        return new HandlerParameter<>(statement, new MySQLDatabaseType(), connectionSession);
    }
}
