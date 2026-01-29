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

package org.apache.shardingsphere.test.natived.jdbc.features;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.apache.shardingsphere.test.natived.commons.util.ResourceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings("SqlNoDataSourceInspection")
class ShardingTest {
    
    private DataSource logicDataSource;
    
    private TestShardingService testShardingService;
    
    @AfterEach
    void afterEach() throws SQLException {
        ResourceUtils.closeJdbcDataSource(logicDataSource);
    }
    
    /**
     * `groovy.lang.Closure` related classes are not available on GraalVM Native Image.
     * This CLASS_BASE algorithm class is designed to emulate INLINE's `ds_${user_id % 2}`.
     * See <a href="https://github.com/oracle/graal/issues/5522">oracle/graal#5522</a> .
     *
     * @throws SQLException SQL exception
     */
    @Test
    void assertShardingInLocalTransactions() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/features/sharding.yaml");
        logicDataSource = new HikariDataSource(config);
        try (
                Connection connection = logicDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("register storage unit if not exists ds_0 (url='jdbc:h2:mem:local_sharding_ds_0;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE',user='sa',password=''), "
                    + "ds_1 (url='jdbc:h2:mem:local_sharding_ds_1;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE',user='sa',password=''), "
                    + "ds_2 (url='jdbc:h2:mem:local_sharding_ds_2;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE',user='sa',password='')");
            statement.execute(
                    "create default sharding database strategy if not exists (type='standard', sharding_column=user_id,sharding_algorithm(type(name=class_based, properties('strategy'='STANDARD',"
                            + "'algorithmClassName'='org.apache.shardingsphere.test.natived.commons.algorithm.ClassBasedInlineShardingAlgorithmFixture'))))");
            statement.execute("create sharding table rule if not exists "
                    + "t_order (datanodes('<LITERAL>ds_0.t_order, ds_1.t_order, ds_2.t_order'), key_generate_strategy(column=order_id,type(name='SNOWFLAKE'))), "
                    + "t_order_item (datanodes('<LITERAL>ds_0.t_order_item, ds_1.t_order_item, ds_2.t_order_item'), key_generate_strategy(column=order_item_id,type(name='SNOWFLAKE')))");
            statement.execute("create broadcast table rule if not exists t_address");
        }
        testShardingService = new TestShardingService(logicDataSource);
        initEnvironment();
        testShardingService.processSuccess();
        testShardingService.cleanEnvironment();
        try (
                Connection connection = logicDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("drop broadcast table rule if exists t_address");
            statement.execute("drop sharding table rule if exists t_order, t_order_item");
            statement.execute("drop default sharding database strategy if exists");
            statement.execute("unregister storage unit if exists ds_0, ds_1, ds_2");
        }
    }
    
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInMySQL();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInMySQL();
        testShardingService.getAddressRepository().createTableIfNotExistsInMySQL();
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
}
