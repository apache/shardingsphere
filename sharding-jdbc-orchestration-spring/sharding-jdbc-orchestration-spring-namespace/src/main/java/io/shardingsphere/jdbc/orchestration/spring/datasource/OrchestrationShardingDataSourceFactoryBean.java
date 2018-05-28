/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.spring.datasource;

import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.jdbc.orchestration.api.OrchestrationShardingDataSourceFactory;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationShardingDataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration sharding data source factory bean.
 *
 * @author zhangliang
 */
public class OrchestrationShardingDataSourceFactoryBean implements FactoryBean<OrchestrationShardingDataSource>, InitializingBean, DisposableBean {
    
    private OrchestrationShardingDataSource orchestrationShardingDataSource;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRuleConfiguration shardingRuleConfig;
    
    private final Map<String, Object> configMap;
    
    private final Properties props;
    
    private final OrchestrationConfiguration orchestrationConfig;
    
    public OrchestrationShardingDataSourceFactoryBean(final OrchestrationConfiguration orchestrationConfig) {
        this(null, null, null, null, orchestrationConfig);
    }
    
    public OrchestrationShardingDataSourceFactoryBean(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig,
                                                      final Map<String, Object> configMap, final Properties props, final OrchestrationConfiguration orchestrationConfig) {
        this.orchestrationConfig = orchestrationConfig;
        this.dataSourceMap = dataSourceMap;
        this.shardingRuleConfig = shardingRuleConfig;
        this.configMap = configMap;
        this.props = props;
    }
    
    @Override
    public OrchestrationShardingDataSource getObject() {
        return orchestrationShardingDataSource;
    }
    
    @Override
    public Class<?> getObjectType() {
        return OrchestrationShardingDataSource.class;
    }
    
    @Override
    public boolean isSingleton() {
        return true;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        orchestrationShardingDataSource =
                (OrchestrationShardingDataSource) OrchestrationShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, configMap, props, orchestrationConfig);
    }
    
    @Override
    public void destroy() {
        orchestrationShardingDataSource.close();
    }
}
