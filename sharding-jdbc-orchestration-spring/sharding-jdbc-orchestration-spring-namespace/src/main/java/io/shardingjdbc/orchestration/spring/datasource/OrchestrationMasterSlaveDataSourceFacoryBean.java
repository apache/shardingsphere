package io.shardingjdbc.orchestration.spring.datasource;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.orchestration.api.OrchestrationMasterSlaveDataSourceFactory;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.OrchestrationMasterSlaveDataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

public class OrchestrationMasterSlaveDataSourceFacoryBean implements FactoryBean<OrchestrationMasterSlaveDataSource>, InitializingBean, DisposableBean {
    
    private OrchestrationMasterSlaveDataSource orchestrationMasterSlaveDataSource;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final MasterSlaveRuleConfiguration masterSlaveRuleConfig;
    
    private final Map<String, Object> configMap;
    
    private final OrchestrationConfiguration orchestrationConfig;
    
    public OrchestrationMasterSlaveDataSourceFacoryBean(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        this(null, null, null, orchestrationConfig);
    }
    
    public OrchestrationMasterSlaveDataSourceFacoryBean(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig,
                                              final Map<String, Object> configMap, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        this.orchestrationConfig = orchestrationConfig;
        this.dataSourceMap = dataSourceMap;
        this.masterSlaveRuleConfig = masterSlaveRuleConfig;
        this.configMap = configMap;
    }
    
    @Override
    public OrchestrationMasterSlaveDataSource getObject() throws Exception {
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
        orchestrationMasterSlaveDataSource = (OrchestrationMasterSlaveDataSource) OrchestrationMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig, configMap, orchestrationConfig);
    }
    
    @Override
    public void destroy() throws Exception {
        orchestrationMasterSlaveDataSource.close();
    }
}
