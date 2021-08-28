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

package org.apache.shardingsphere.driver.jdbc.core.datasource;

import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.driver.state.DriverStateContext;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.config.scope.SchemaRuleConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.required.RequiredSPIRegistry;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * ShardingSphere data source.
 */
@Getter
public final class ShardingSphereDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    static {
        ShardingSphereServiceLoader.register(ContextManagerBuilder.class);
    }
    
    private final String schemaName;
    
    private final ContextManager contextManager;
    
    public ShardingSphereDataSource(final String schemaName, final ModeConfiguration modeConfig) throws SQLException {
        this.schemaName = schemaName;
        contextManager = createContextManager(schemaName, modeConfig);
    }
    
    public ShardingSphereDataSource(final String schemaName, final ModeConfiguration modeConfig, final Map<String, DataSource> dataSourceMap,
                                    final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        this.schemaName = schemaName;
        contextManager = createContextManager(schemaName, modeConfig, dataSourceMap, ruleConfigs, props);
    }
    
    private ContextManager createContextManager(final String schemaName, final ModeConfiguration modeConfig) throws SQLException {
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(schemaName, new HashMap<>());
        Map<String, Collection<RuleConfiguration>> schemaRuleConfigs = Collections.singletonMap(schemaName, Collections.emptyList());
        Collection<RuleConfiguration> globalRuleConfigs = Collections.emptyList();
        ContextManagerBuilder builder = null == modeConfig
                ? RequiredSPIRegistry.getRegisteredService(ContextManagerBuilder.class) : TypedSPIRegistry.getRegisteredService(ContextManagerBuilder.class, modeConfig.getType(), new Properties());
        return builder.build(modeConfig, dataSourcesMap, schemaRuleConfigs, globalRuleConfigs, new Properties(), false);
    }
    
    private ContextManager createContextManager(final String schemaName, final ModeConfiguration modeConfig, final Map<String, DataSource> dataSourceMap,
                                                final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(schemaName, dataSourceMap);
        Map<String, Collection<RuleConfiguration>> schemaRuleConfigs = Collections.singletonMap(
                schemaName, ruleConfigs.stream().filter(each -> each instanceof SchemaRuleConfiguration).collect(Collectors.toList()));
        Collection<RuleConfiguration> globalRuleConfigs = ruleConfigs.stream().filter(each -> each instanceof GlobalRuleConfiguration).collect(Collectors.toList());
        ContextManagerBuilder builder = null == modeConfig
                ? RequiredSPIRegistry.getRegisteredService(ContextManagerBuilder.class) : TypedSPIRegistry.getRegisteredService(ContextManagerBuilder.class, modeConfig.getType(), new Properties());
        return builder.build(modeConfig, dataSourcesMap, schemaRuleConfigs, globalRuleConfigs, props, null == modeConfig || modeConfig.isOverwrite());
    }
    
    @Override
    public Connection getConnection() {
        return DriverStateContext.getConnection(schemaName, getDataSourceMap(), contextManager, TransactionTypeHolder.get());
    }
    
    @Override
    public Connection getConnection(final String username, final String password) {
        return getConnection();
    }
    
    private Map<String, DataSource> getDataSourceMap() {
        return contextManager.getMetaDataContexts().getMetaData(schemaName).getResource().getDataSources();
    }
    
    /**
     * Close data sources.
     *
     * @param dataSourceNames data source names to be closed
     * @throws Exception exception
     */
    public void close(final Collection<String> dataSourceNames) throws Exception {
        for (String each : dataSourceNames) {
            close(getDataSourceMap().get(each));
        }
        contextManager.close();
    }
    
    private void close(final DataSource dataSource) throws Exception {
        if (dataSource instanceof AutoCloseable) {
            ((AutoCloseable) dataSource).close();
        }
    }
    
    @Override
    public void close() throws Exception {
        close(getDataSourceMap().keySet());
    }
}
