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

package org.apache.shardingsphere.integration.scaling.test.mysql.engine;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.scaling.test.mysql.engine.base.BaseITCase;
import org.junit.Before;

import java.sql.Connection;
import java.util.Properties;

public class MySQLScalingIT extends BaseITCase {
    
    public MySQLScalingIT() {
        super(new MySQLDatabaseType());
    }
    
    @SneakyThrows
    @Before
    public void setUp() {
        super.setUp();
        String addResourceTemplate = "ADD RESOURCE ds_%s (URL=\"jdbc:mysql://%s:3306/%s?serverTimezone=UTC&useSSL=false\",USER=root,PASSWORD=123456)";
        try (Connection connection = getTargetDataSource().getConnection()) {
            connection.createStatement().execute("CREATE DATABASE sharding_db;");
            connection.createStatement().execute("USE sharding_db;");
            int dbIndex = 0;
            for (String value : getActualDataSourceMap().keySet()) {
                connection.createStatement().execute(String.format(addResourceTemplate, dbIndex, getDatabaseNetworkAlias(), value));
                dbIndex++;
            }
            for (String value : getExpectedDataSourceMap().keySet()) {
                connection.createStatement().execute(String.format(addResourceTemplate, dbIndex, getDatabaseNetworkAlias(), value));
                dbIndex++;
            }
            Properties initProps = new Properties();
            initProps.load(getClass().getClassLoader().getResourceAsStream("env/mysql/rule_inti.properties"));
            connection.createStatement().execute(initProps.getProperty("create.table.rule"));
            connection.createStatement().execute(initProps.getProperty("create.table.sql"));
            // connection.prepareStatement()
        }
    }
}
