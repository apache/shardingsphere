/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.transaction.soft.base;

import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.contstant.SQLType;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractSoftTransactionIntegrationTest {
    
    private ShardingDataSource shardingDataSource;
    
    private DataSource transactionDataSource;
    
    @Before
    public void setup() throws SQLException {
        prepareEnv();
    }
    
    private void prepareEnv() throws SQLException {
        DataSourceRule dataSourceRule = new DataSourceRule(createDataSourceMap());
        TableRule tableRule = TableRule.builder("transaction_test").dataSourceRule(dataSourceRule).build();
        ShardingRule shardingRule = ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Lists.newArrayList(tableRule)).build();
        shardingDataSource = new ShardingDataSource(shardingRule);
        createTable(shardingDataSource);
        transactionDataSource = createTransactionLogDataSource();
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(1);
        result.put("db_trans", createBizDataSource());
        return result;
    }
    
    private DataSource createBizDataSource() {
        return createDataSource("db_trans");
    }
    
    private DataSource createTransactionLogDataSource() {
        return createDataSource("trans_log");
    }
    
    private DataSource createDataSource(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(org.h2.Driver.class.getName());
        result.setUrl("jdbc:h2:mem:" + dataSourceName);
        result.setUsername("sa");
        result.setPassword("");
        return result;
    }
    
    private void createTable(final ShardingDataSource shardingDataSource) {
        String dbSchema = "CREATE TABLE IF NOT EXISTS `transaction_test` ("
            + "`id` int NOT NULL, "
            + "PRIMARY KEY (`id`));";
        try (
                Connection conn = shardingDataSource.getConnection().getConnection("db_trans", SQLType.SELECT);
                PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}
