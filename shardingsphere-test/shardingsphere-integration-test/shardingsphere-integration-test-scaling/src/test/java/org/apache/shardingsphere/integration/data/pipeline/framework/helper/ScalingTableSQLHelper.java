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

package org.apache.shardingsphere.integration.data.pipeline.framework.helper;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.util.TableCrudUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * SQL helper.
 */
@AllArgsConstructor
public final class ScalingTableSQLHelper {
    
    private final DatabaseType databaseType;
    
    private final ExtraSQLCommand extraSQLCommand;
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Create order table.
     */
    public void createOrderTable() {
        jdbcTemplate.execute(extraSQLCommand.getCreateTableOrder());
    }
    
    /**
     * Create order item table.
     */
    public void createOrderItemTable() {
        jdbcTemplate.execute(extraSQLCommand.getCreateTableOrderItem());
    }
    
    /**
     * Init table data.
     *
     * @param initOrderItemTogether whether init order item table together
     */
    public void initTableData(final boolean initOrderItemTogether) {
        Pair<List<Object[]>, List<Object[]>> dataPair = Pair.of(null, null);
        if (databaseType instanceof MySQLDatabaseType) {
            dataPair = TableCrudUtil.generateMySQLInsertDataList(3000);
        } else if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
            dataPair = TableCrudUtil.generatePostgresSQLInsertDataList(3000);
        }
        jdbcTemplate.batchUpdate(extraSQLCommand.getFullInsertOrder(), dataPair.getLeft());
        if (initOrderItemTogether) {
            jdbcTemplate.batchUpdate(extraSQLCommand.getInsertOrderItem(), dataPair.getRight());
        }
    }
}
