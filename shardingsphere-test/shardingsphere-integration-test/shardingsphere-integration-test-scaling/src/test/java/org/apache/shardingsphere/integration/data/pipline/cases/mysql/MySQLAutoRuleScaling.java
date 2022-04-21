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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipline.cases.BaseITCase;
import org.apache.shardingsphere.integration.data.pipline.cases.dataset.CommonSQLCommand;
import org.apache.shardingsphere.integration.data.pipline.cases.dataset.mysql.MySQLCommand;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.util.Objects;

/**
 * MySQL auto rule scaling test case.
 */
public final class MySQLAutoRuleScaling extends BaseITCase {
    
    public MySQLAutoRuleScaling() {
        super(new MySQLDatabaseType());
    }
    
    @SneakyThrows
    @Before
    public void setUp() {
        super.setUp();
        CommonSQLCommand commonSQLCommand = JAXB.unmarshal(Objects.requireNonNull(getClass().getClassLoader().getResource("env/common/command.xml")), CommonSQLCommand.class);
        MySQLCommand mysqlCommand = JAXB.unmarshal(Objects.requireNonNull(getClass().getClassLoader().getResource("env/mysql/sql.xml")), MySQLCommand.class);
        try (Connection connection = getProxyConnection()) {
            connection.createStatement().execute(commonSQLCommand.getCreateDatabase());
            connection.createStatement().execute(commonSQLCommand.getUseDatabase());
            int dbIndex = 0;
            for (String dbName : listSourceDatabaseName()) {
                connection.createStatement().execute(String.format(commonSQLCommand.getAddResource(), dbIndex, getDatabaseUrl(), dbName));
                dbIndex++;
            }
            for (String value : listTargetDatabaseName()) {
                connection.createStatement().execute(String.format(commonSQLCommand.getAddResource(), dbIndex, getDatabaseUrl(), value));
                dbIndex++;
            }
            connection.createStatement().execute(commonSQLCommand.getCreateShardingTableRule());
            connection.createStatement().execute(mysqlCommand.getCreateTableOrder());
        }
    }
    
    @Test
    public void test() {
        
    }
}
