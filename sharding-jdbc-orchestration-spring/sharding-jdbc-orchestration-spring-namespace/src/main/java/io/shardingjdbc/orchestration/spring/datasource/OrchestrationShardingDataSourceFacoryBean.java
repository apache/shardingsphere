package io.shardingjdbc.orchestration.spring.datasource;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.orchestration.api.OrchestrationShardingDataSourceFactory;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.OrchestrationShardingDataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class OrchestrationShardingDataSourceFacoryBean implements FactoryBean<OrchestrationShardingDataSource>, InitializingBean, DisposableBean {
    
    private OrchestrationShardingDataSource orchestrationShardingDataSource;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRuleConfiguration shardingRuleConfig;
    
    private final Map<String, Object> configMap;
    
    private final Properties props;
    
    private final OrchestrationConfiguration orchestrationConfig;
    
    public OrchestrationShardingDataSourceFacoryBean(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        this(null, null, null, null, orchestrationConfig);
    }
    
    public OrchestrationShardingDataSourceFacoryBean(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig,
                                                     final Map<String, Object> configMap, final Properties props, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        this.orchestrationConfig = orchestrationConfig;
        this.dataSourceMap = dataSourceMap;
        this.shardingRuleConfig = shardingRuleConfig;
        this.configMap = configMap;
        this.props = props;
    }
    
    @Override
    public OrchestrationShardingDataSource getObject() throws Exception {
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
        orchestrationShardingDataSource = (OrchestrationShardingDataSource) OrchestrationShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, configMap, props, orchestrationConfig);
    }
    
    @Override
    public void destroy() throws Exception {
        orchestrationShardingDataSource.close();
    }
}
