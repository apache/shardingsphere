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

package org.apache.shardingsphere.integration.data.pipline.cases.mysql;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipline.cases.BaseScalingIT;
import org.apache.shardingsphere.integration.data.pipline.util.TableCrudUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class BaseMySQLScalingIT extends BaseScalingIT {
    
    private static final DatabaseType MYSQL_DATABASE = new MySQLDatabaseType();
    
    @Getter(AccessLevel.PROTECTED)
    private JdbcTemplate jdbcTemplate;
    
    public BaseMySQLScalingIT() {
        super(MYSQL_DATABASE);
        initScalingEnvironment();
    }
    
    @SneakyThrows
    protected void initScalingEnvironment() {
        try (Connection connection = getProxyDataSource("").getConnection()) {
            connection.createStatement().execute(getCommonSQLCommand().getCreateDatabase());
            connection.createStatement().execute(getExtraSQLCommand().getUseDatabase());
            String addResource = getExtraSQLCommand().getAddResource();
            for (String source : getSourceDataSourceNames()) {
                connection.createStatement().execute(String.format(addResource, source, getDatabaseUrl(), source));
            }
            for (String target : getTargetDataSourceNames()) {
                connection.createStatement().execute(String.format(addResource, target, getDatabaseUrl(), target));
            }
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
