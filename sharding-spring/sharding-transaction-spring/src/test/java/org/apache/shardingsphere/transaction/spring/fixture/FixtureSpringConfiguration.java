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

package org.apache.shardingsphere.transaction.spring.fixture;

import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlShardingDataSourceFactory;
import org.apache.shardingsphere.transaction.spring.ShardingTransactionTypeScanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {"org.apache.shardingsphere.transaction.spring"})
public class FixtureSpringConfiguration {
    
    /**
     * Data source configuration.
     *
     * @return data source
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    @Bean
    public DataSource dataSource() throws IOException, SQLException {
        return YamlShardingDataSourceFactory.createDataSource(new File(FixtureSpringConfiguration.class.getResource("/META-INF/sharding-databases-tables.yaml").getFile()));
    }
    
    /**
     * Create platform transaction manager bean.
     *
     * @return platform transaction manager
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    @Bean
    public PlatformTransactionManager txManager() throws IOException, SQLException {
        return new DataSourceTransactionManager(dataSource());
    }
    
    /**
     * Create JDBC template bean.
     *
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     * @return JDBC template bean
     */
    @Bean
    public JdbcTemplate jdbcTemplate() throws IOException, SQLException {
        return new JdbcTemplate(dataSource());
    }
    
    /**
     * Sharding transaction type scanner configuration.
     *
     * @return sharding transaction type scanner
     */
    @Bean
    public ShardingTransactionTypeScanner shardingTransactionTypeScanner() {
        return new ShardingTransactionTypeScanner();
    }
}
