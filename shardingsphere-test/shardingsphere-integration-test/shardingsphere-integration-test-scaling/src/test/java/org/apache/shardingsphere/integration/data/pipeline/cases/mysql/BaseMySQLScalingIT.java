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

package org.apache.shardingsphere.integration.data.pipeline.cases.mysql;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.BaseScalingIT;
import org.apache.shardingsphere.integration.data.pipeline.util.TableCrudUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class BaseMySQLScalingIT extends BaseScalingIT {
    
    private static final DatabaseType MYSQL_DATABASE = new MySQLDatabaseType();
    
    private static final String ADD_RESOURCE_SQL = "ADD RESOURCE %s (URL='jdbc:mysql://%s/%s?serverTimezone=UTC&useSSL=false&rewriteBatchedStatements=true',USER=root,PASSWORD=root)";
    
    @Getter(AccessLevel.PROTECTED)
    private JdbcTemplate jdbcTemplate;
    
    public BaseMySQLScalingIT() {
        super(MYSQL_DATABASE);
        initScalingEnvironment();
    }
    
    @SneakyThrows
    protected void initScalingEnvironment() {
        try (Connection connection = getProxyDataSource("").getConnection()) {
            connection.createStatement().execute("CREATE DATABASE sharding_db");
            connection.createStatement().execute("USE sharding_db");
            connection.createStatement().execute(String.format(ADD_RESOURCE_SQL, "ds_0", getDatabaseIpAndPort(), "ds_0"));
            connection.createStatement().execute(String.format(ADD_RESOURCE_SQL, "ds_1", getDatabaseIpAndPort(), "ds_1"));
            connection.createStatement().execute(String.format(ADD_RESOURCE_SQL, "ds_2", getDatabaseIpAndPort(), "ds_2"));
            connection.createStatement().execute(String.format(ADD_RESOURCE_SQL, "ds_3", getDatabaseIpAndPort(), "ds_3"));
            connection.createStatement().execute(String.format(ADD_RESOURCE_SQL, "ds_4", getDatabaseIpAndPort(), "ds_4"));
            for (String sql : getCommonSQLCommand().getCreateShardingAlgorithm()) {
                connection.createStatement().execute(sql);
                // TODO sleep to wait for sharding algorithm table created，otherwise, the next sql will fail.
                TimeUnit.SECONDS.sleep(1);
            }
            connection.createStatement().execute(getCommonSQLCommand().getCreateShardingTable());
            connection.createStatement().execute(getCommonSQLCommand().getCreateShardingBinding());
            connection.createStatement().execute(getCommonSQLCommand().getCreateShardingScalingRule());
        }
        jdbcTemplate = new JdbcTemplate(getProxyDataSource("sharding_db"));
    }
    
    protected void initTableAndData() {
        jdbcTemplate.execute(getExtraSQLCommand().getCreateTableOrder());
        jdbcTemplate.execute(getExtraSQLCommand().getCreateTableOrderItem());
        Pair<List<Object[]>, List<Object[]>> dataPair = TableCrudUtil.generateMySQLInsertDataList(3000);
        jdbcTemplate.batchUpdate(getExtraSQLCommand().getInsertOrder(), dataPair.getLeft());
        jdbcTemplate.batchUpdate(getCommonSQLCommand().getInsertOrderItem(), dataPair.getRight());
    }
}
