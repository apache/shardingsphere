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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipline.cases.BaseScalingITCase;
import org.apache.shardingsphere.integration.data.pipline.cases.command.CreateTableSQLCommand;
import org.apache.shardingsphere.integration.data.pipline.util.TableCrudUtil;

import javax.xml.bind.JAXB;
import java.util.List;

public abstract class BaseMySQLScalingCase extends BaseScalingITCase {
    
    @Getter
    private final CreateTableSQLCommand createTableSQLCommand;
    
    public BaseMySQLScalingCase() {
        super(new MySQLDatabaseType());
        createTableSQLCommand = JAXB.unmarshal(BaseMySQLScalingCase.class.getClassLoader().getResource("env/mysql/sql.xml"), CreateTableSQLCommand.class);
    }
    
    protected void initTableAndData() {
        getJdbcTemplate().execute(createTableSQLCommand.getCreateTableOrder());
        getJdbcTemplate().execute(createTableSQLCommand.getCreateTableOrderItem());
        Pair<List<Object[]>, List<Object[]>> dataPair = TableCrudUtil.generateInsertDataList(3000);
        getJdbcTemplate().batchUpdate(getCommonSQLCommand().getInsertOrder(), dataPair.getLeft());
        getJdbcTemplate().batchUpdate(getCommonSQLCommand().getInsertOrderItem(), dataPair.getRight());
    }
}
