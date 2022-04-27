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

package org.apache.shardingsphere.integration.data.pipline.cases.postgresql;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipline.cases.BaseScalingIT;
import org.apache.shardingsphere.integration.data.pipline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.integration.data.pipline.util.TableCrudUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BasePostgreSQLScalingIT extends BaseScalingIT {
    
    @Getter
    private final ExtraSQLCommand extraSQLCommand;
    
    @Getter(AccessLevel.PROTECTED)
    private JdbcTemplate jdbcTemplate;
    
    public BasePostgreSQLScalingIT() {
        super(new PostgreSQLDatabaseType());
        extraSQLCommand = JAXB.unmarshal(BasePostgreSQLScalingIT.class.getClassLoader().getResource("env/postgresql/sql.xml"), ExtraSQLCommand.class);
        initScalingEnvironment();
    }
    
    @SneakyThrows(SQLException.class)
    protected void initScalingEnvironment() {
        // TODO jdbc create database not take effect
        DataSource dataSource = getProxyDataSource("sharding_db");
        jdbcTemplate = new JdbcTemplate(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            for (String value : getTargetDataSourceNames()) {
                connection.createStatement().execute(String.format(extraSQLCommand.getAddResource(), value, getDatabaseUrl(), value));
            }
        }
    }
    
    protected void initTableAndData() {
        jdbcTemplate.execute(extraSQLCommand.getCreateTableOrder());
        jdbcTemplate.execute(extraSQLCommand.getCreateTableOrderItem());
        Pair<List<Object[]>, List<Object[]>> dataPair = TableCrudUtil.generatePostgresSQLInsertDataList(3000);
        getJdbcTemplate().batchUpdate(getExtraSQLCommand().getInsertOrder(), dataPair.getLeft());
        getJdbcTemplate().batchUpdate(getCommonSQLCommand().getInsertOrderItem(), dataPair.getRight());
    }
}
