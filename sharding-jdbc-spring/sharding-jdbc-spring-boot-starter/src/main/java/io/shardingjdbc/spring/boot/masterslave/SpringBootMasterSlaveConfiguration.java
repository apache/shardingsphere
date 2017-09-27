package io.shardingjdbc.spring.boot.masterslave;

import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
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
 * Spring boot master slave rule configuration.
 *
 * @author caohao
 */
@Configuration
@EnableConfigurationProperties(SpringBootMasterSlaveRuleConfigurationProperties.class)
public class SpringBootMasterSlaveConfiguration implements EnvironmentAware {
    
    @Autowired
    private SpringBootMasterSlaveRuleConfigurationProperties properties;
    
    private Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    @Bean
    public DataSource dataSource() throws SQLException {
        return MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, properties.getMasterSlaveRuleConfiguration());
    }
    
    @Override
    public void setEnvironment(final Environment environment) {
        EnvironmentAwareUtil.setDataSourceMap(dataSourceMap, environment);
    }
}
