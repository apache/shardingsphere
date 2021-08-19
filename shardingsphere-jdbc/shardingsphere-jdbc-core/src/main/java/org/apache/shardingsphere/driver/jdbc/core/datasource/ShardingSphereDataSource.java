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
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.config.scope.SchemaRuleConfiguration;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.manager.ContextManagerBuilder;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.mode.builder.ModeBuilderEngine;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
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
    
    private final ShardingSphereMode mode;
    
    private final ContextManager contextManager;
    
    public ShardingSphereDataSource(final String schemaName, final ModeConfiguration modeConfig, final Map<String, DataSource> dataSourceMap,
                                    final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        this.schemaName = schemaName;
        mode = ModeBuilderEngine.build(modeConfig);
        Collection<RuleConfiguration> schemaRuleConfigs = ruleConfigs.stream().filter(each -> each instanceof SchemaRuleConfiguration).collect(Collectors.toList());
        Collection<RuleConfiguration> globalRuleConfigs = ruleConfigs.stream().filter(each -> each instanceof GlobalRuleConfiguration).collect(Collectors.toList());
        contextManager = TypedSPIRegistry.getRegisteredService(ContextManagerBuilder.class, modeConfig.getType(), new Properties()).build(
                mode, Collections.singletonMap(schemaName, dataSourceMap), Collections.singletonMap(schemaName, schemaRuleConfigs), globalRuleConfigs, props, modeConfig.isOverwrite());
        
    }
    
    @Override
    public ShardingSphereConnection getConnection() {
        return new ShardingSphereConnection(schemaName, getDataSourceMap(), contextManager, TransactionTypeHolder.get());
    }
    
    @Override
    public ShardingSphereConnection getConnection(final String username, final String password) {
        return getConnection();
    }
    
    /**
     * Get data sources.
     * 
     * @return data sources
     */
    public Map<String, DataSource> getDataSourceMap() {
        return contextManager.getMetaDataContexts().getMetaData(schemaName).getResource().getDataSources();
    }
    
    @Override
    public void close() throws Exception {
        close(getDataSourceMap().keySet());
    }
    
    /**
     * Close dataSources.
     * 
     * @param dataSourceNames data source names
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
}
