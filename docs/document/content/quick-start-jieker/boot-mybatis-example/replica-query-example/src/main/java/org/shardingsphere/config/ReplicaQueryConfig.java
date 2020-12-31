package org.shardingsphere.config;


import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

@Configuration
@ComponentScan(basePackageClasses = ReplicaQueryConfig.class)
@MapperScan(basePackages = "org.shardingsphere.mapper")
public class ReplicaQueryConfig {

    /**
     * description : Get objects of the SqlSessionFactory
     *
     * @return : org.apache.ibatis.session.SqlSessionFactory
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(getDataSource());
        sessionFactory.setFailFast(true);
        sessionFactory.setMapperLocations(resolver.getResources("classpath:/mapper/*Mapper.xml"));
        return sessionFactory.getObject();
    }
    /**
     * description : get datasource and config the rules
     *
     * @return : javax.sql.DataSource
     */
    public DataSource getDataSource() throws SQLException {
        // Primary datasource name and Replica data source names
        ReplicaQueryDataSourceRuleConfiguration dataSourceConfig = new ReplicaQueryDataSourceRuleConfiguration(
                "masterandslaves", "master", Arrays.asList("slave0", "slave1"), "round-robin");
        // Load balance algorithm configuration
        Properties algorithmProperties = new Properties();
        algorithmProperties.setProperty("work-id", "123");
        ShardingSphereAlgorithmConfiguration algorithmConfiguration = new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", algorithmProperties);
        HashMap<String, ShardingSphereAlgorithmConfiguration> algorithm = new HashMap<>();
        algorithm.put("round-robin", algorithmConfiguration);
        ReplicaQueryRuleConfiguration ruleConfig = new ReplicaQueryRuleConfiguration(Collections.singleton(dataSourceConfig), algorithm);
        // Whether to print the sql statement
        Properties properties = new Properties();
        properties.setProperty("sql-show", "true");
        return ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Collections.singleton(ruleConfig), properties);
    }

    /**
     * description : config the datasource
     *
     * @return : java.util.Map<java.lang.String,javax.sql.DataSource>
     */
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(3, 1);
        result.put("master", DataSourceUtil.createDataSource("master"));
        result.put("slave0", DataSourceUtil.createDataSource("slave0"));
        result.put("slave1", DataSourceUtil.createDataSource("slave1"));
        return result;
    }

}
