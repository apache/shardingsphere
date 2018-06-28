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

import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.jdbc.orchestration.api.OrchestrationMasterSlaveDataSourceFactory;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationMasterSlaveDataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Orchestration master-slave data source factory bean.
 * 
 * @author zhangliang 
 */
public class OrchestrationMasterSlaveDataSourceFactoryBean implements FactoryBean<OrchestrationMasterSlaveDataSource>, InitializingBean, DisposableBean {
    
    private OrchestrationMasterSlaveDataSource orchestrationMasterSlaveDataSource;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final MasterSlaveRuleConfiguration masterSlaveRuleConfig;
    
    private final Map<String, Object> configMap;
    
    private final OrchestrationConfiguration orchestrationConfig;
    
    public OrchestrationMasterSlaveDataSourceFactoryBean(final OrchestrationConfiguration orchestrationConfig) {
        this(null, null, null, orchestrationConfig);
    }
    
    public OrchestrationMasterSlaveDataSourceFactoryBean(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig,
                                                         final Map<String, Object> configMap, final OrchestrationConfiguration orchestrationConfig) {
        this.orchestrationConfig = orchestrationConfig;
        this.dataSourceMap = dataSourceMap;
        this.masterSlaveRuleConfig = masterSlaveRuleConfig;
        this.configMap = configMap;
    }
    
    @Override
    public OrchestrationMasterSlaveDataSource getObject() {
        return orchestrationMasterSlaveDataSource;
    }
    
    @Override
    public Class<?> getObjectType() {
        return OrchestrationMasterSlaveDataSource.class;
    }
    
    @Override
    public boolean isSingleton() {
        return true;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        orchestrationMasterSlaveDataSource = 
                (OrchestrationMasterSlaveDataSource) OrchestrationMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig, configMap, orchestrationConfig);
    }
    
    @Override
    public void destroy() {
        orchestrationMasterSlaveDataSource.close();
    }
}
