package io.shardingjdbc.spring.boot.sharding;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.spring.boot.util.EnvironmentAwareUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
@EnableConfigurationProperties(SpringBootShardingRuleConfigurationProperties.class)
public class SpringBootShardingConfiguration implements EnvironmentAware {
    
    @Autowired
    private SpringBootShardingRuleConfigurationProperties properties;
    
    private Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    @Bean
    public DataSource dataSource() throws SQLException {
        return ShardingDataSourceFactory.createDataSource(dataSourceMap, properties.getShardingRuleConfiguration());
    }
    
    @Override
    public void setEnvironment(final Environment environment) {
        EnvironmentAwareUtil.setDataSourceMap(dataSourceMap, environment);
    }
}
