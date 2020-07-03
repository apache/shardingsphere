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

package org.apache.shardingsphere.driver.jdbc.core.context;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.jdbc.core.datasource.metadata.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.SQLParserEngineFactory;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.infra.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Runtime context.
 */
@Getter
@Slf4j(topic = "ShardingSphere-metadata")
public final class RuntimeContext implements AutoCloseable {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final DatabaseType databaseType;
    
    private final Collection<RuleConfiguration> configurations;
    
    private final Collection<ShardingSphereRule> rules;
    
    private final ConfigurationProperties props;
    
    private final ExecutorKernel executorKernel;
    
    private final SQLParserEngine sqlParserEngine;
    
    private final CachedDatabaseMetaData cachedDatabaseMetaData;
    
    private final ShardingTransactionManagerEngine shardingTransactionManagerEngine;
    
    @Setter
    private ShardingSphereMetaData metaData;
    
    public RuntimeContext(final Map<String, DataSource> dataSourceMap, 
                          final DatabaseType databaseType, final Collection<RuleConfiguration> configurations, final Properties props) throws SQLException {
        this.dataSourceMap = dataSourceMap;
        this.databaseType = databaseType;
        this.configurations = configurations;
        rules = ShardingSphereRulesBuilder.build(configurations, dataSourceMap.keySet());
        this.props = new ConfigurationProperties(null == props ? new Properties() : props);
        executorKernel = new ExecutorKernel(this.props.<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE));
        sqlParserEngine = SQLParserEngineFactory.getSQLParserEngine(DatabaseTypes.getTrunkDatabaseTypeName(databaseType));
        cachedDatabaseMetaData = createCachedDatabaseMetaData(dataSourceMap);
        shardingTransactionManagerEngine = new ShardingTransactionManagerEngine();
        shardingTransactionManagerEngine.init(databaseType, dataSourceMap);
        metaData = createMetaData(dataSourceMap, databaseType);
        log(configurations, props);
    }
    
    public RuntimeContext(final DataSource dataSource, final DatabaseType databaseType, final Collection<RuleConfiguration> configurations, final Properties props) throws SQLException {
        this(ImmutableMap.of(DefaultSchema.LOGIC_NAME, dataSource), databaseType, configurations, props);
    }
    
    private CachedDatabaseMetaData createCachedDatabaseMetaData(final Map<String, DataSource> dataSourceMap) throws SQLException {
        try (Connection connection = dataSourceMap.values().iterator().next().getConnection()) {
            return new CachedDatabaseMetaData(connection.getMetaData());
        }
    }
    
    private ShardingSphereMetaData createMetaData(final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType) throws SQLException {
        long start = System.currentTimeMillis();
        DataSourceMetas dataSourceMetas = new DataSourceMetas(databaseType, getDatabaseAccessConfigurationMap(dataSourceMap));
        RuleSchemaMetaData ruleSchemaMetaData = new RuleSchemaMetaDataLoader(rules).load(getDatabaseType(), dataSourceMap, getProps(), executorKernel.getExecutorService().getExecutorService());
        ShardingSphereMetaData result = new ShardingSphereMetaData(dataSourceMetas, ruleSchemaMetaData);
        log.info("Meta data load finished, cost {} milliseconds.", System.currentTimeMillis() - start);
        return result;
    }
    
    private Map<String, DatabaseAccessConfiguration> getDatabaseAccessConfigurationMap(final Map<String, DataSource> dataSourceMap) throws SQLException {
        Map<String, DatabaseAccessConfiguration> result = new LinkedHashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            DataSource dataSource = entry.getValue();
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                result.put(entry.getKey(), new DatabaseAccessConfiguration(metaData.getURL(), metaData.getUserName(), null));
            }
        }
        return result;
    }
    
    private void log(final Collection<RuleConfiguration> configurations, final Properties props) {
        ConfigurationLogger.log(configurations);
        ConfigurationLogger.log(props);
    }
    
    @Override
    public void close() throws Exception {
        shardingTransactionManagerEngine.close();
        executorKernel.close();
    }
}
