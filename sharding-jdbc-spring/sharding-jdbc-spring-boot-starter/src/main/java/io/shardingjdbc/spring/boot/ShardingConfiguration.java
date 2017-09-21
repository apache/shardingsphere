package io.shardingjdbc.spring.boot;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sharding jdbc spring boot configuration.
 *
 * @author caohao
 */
@Configuration
@EnableConfigurationProperties(ShardingProperties.class)
public class ShardingConfiguration implements EnvironmentAware {
    
    @Autowired
    private ShardingProperties properties;
    
    private Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    @Bean
    public DataSource dataSource() throws SQLException {
        return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingConfig());
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ShardingRuleConfiguration shardingConfig() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.setMasterSlaveRuleConfigs(properties.getSharding().getMasterSlaveRuleConfigs());
        shardingRuleConfiguration.setBindingTableGroups(properties.getSharding().getBindingTableGroups());
        shardingRuleConfiguration.setDefaultDatabaseShardingStrategyConfig(properties.getSharding().getDefaultDatabaseShardingStrategyConfig());
        shardingRuleConfiguration.setDefaultTableShardingStrategyConfig(properties.getSharding().getDefaultTableShardingStrategyConfig());
        shardingRuleConfiguration.setDefaultDataSourceName(properties.getSharding().getDefaultDataSourceName());
        shardingRuleConfiguration.setDefaultKeyGeneratorClass(properties.getSharding().getDefaultKeyGeneratorClass());
        shardingRuleConfiguration.setTableRuleConfigs(properties.getSharding().getTableRuleConfigs());
        return shardingRuleConfiguration;
    }
    
    @Override
    public void setEnvironment(final Environment environment) {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "sharding.jdbc.datasource.");
        String dataSources = propertyResolver.getProperty("names");
        for (String each : dataSources.split(",")) {
            Map<String, Object> dataSourceProps = propertyResolver.getSubProperties(each + ".");
            try {
                DataSource dataSource = DataSourceBuilder.create().driverClassName(dataSourceProps.get("driver-class-name").toString())
                        .username(dataSourceProps.get("username").toString()).password(dataSourceProps.get("password").toString())
                        .url(dataSourceProps.get("url").toString()).type((Class<? extends DataSource>) Class.forName(dataSourceProps.get("type").toString())).build();
                dataSourceMap.put(each, dataSource);
            } catch (final ClassNotFoundException ex) {
                throw new RuntimeException("Can't find datasource type!", ex);
            }
        }
    }
}
