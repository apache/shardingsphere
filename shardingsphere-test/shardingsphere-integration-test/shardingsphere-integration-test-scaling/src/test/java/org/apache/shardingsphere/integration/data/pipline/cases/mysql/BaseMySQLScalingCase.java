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

import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipline.cases.BaseScalingITCase;
import org.apache.shardingsphere.integration.data.pipline.cases.command.mysql.MySQLCommand;
import org.apache.shardingsphere.integration.data.pipline.util.TableCrudUtil;

import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BaseMySQLScalingCase extends BaseScalingITCase {
    
    @Getter
    private MySQLCommand mySQLCommand;
    
    public BaseMySQLScalingCase() {
        super(new MySQLDatabaseType());
    }
    
    protected void initTableAndData() throws SQLException {
        mySQLCommand = JAXB.unmarshal(BaseMySQLScalingCase.class.getClassLoader().getResource("env/mysql/sql.xml"), MySQLCommand.class);
        try (Connection connection = getProxyConnection("sharding_db")) {
            connection.createStatement().execute(mySQLCommand.getCreateTableOrder());
            connection.createStatement().execute(mySQLCommand.getCreateTableOrderItem());
            // init date, need more than 3000 rows, in order to test certain conditions
            PreparedStatement orderStatement = connection.prepareStatement(getCommonSQLCommand().getInsertOrder());
            PreparedStatement itemStatement = connection.prepareStatement(getCommonSQLCommand().getInsertOrderItem());
            TableCrudUtil.batchInsertOrderAndOrderItem(orderStatement, itemStatement, 3000);
            orderStatement.executeBatch();
            itemStatement.executeBatch();
        }
    }
}
