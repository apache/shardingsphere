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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.CommonSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.util.TableCrudUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public final class MySQLIncrementTaskRunnable implements Runnable {
    
    private static final String INSERT_ORDER = "INSERT INTO t_order (id,order_id,user_id) VALUES (?, ?, ?)";
    
    private static final String INSERT_ORDER_ITEM = "INSERT INTO t_order_item(item_id,order_id,user_id,status) VALUES(?,?,?,?)";
    
    private static final String UPDATE_ORDER_BY_ID = "UPDATE t_order SET t_varchar = ?,t_unsigned_int = ? WHERE id = ?";
    
    private static final String UPDATE_ORDER_ITEM_BY_ID = "UPDATE t_order_item SET status = ? WHERE item_id = ?";
    
    private final JdbcTemplate jdbcTemplate;
    
    private final CommonSQLCommand commonSQLCommand;
    
    @Override
    public void run() {
        int executeCount = 0;
        List<Long> newPrimaryKeys = new LinkedList<>();
        try {
            while (executeCount < 20 && !Thread.currentThread().isInterrupted()) {
                newPrimaryKeys.add(insertOrderAndOrderItem());
                if (newPrimaryKeys.size() % 2 == 0) {
                    deleteOrderAndOrderItem(newPrimaryKeys.get(newPrimaryKeys.size() - 1));
                } else {
                    updateOrderAndOrderItem(newPrimaryKeys.get(newPrimaryKeys.size() - 1));
                }
                executeCount++;
                log.info("Increment task runnable execute successfully.");
            }
        } catch (final SQLException ex) {
            log.error("IncrementTaskThread error", ex);
            throw new RuntimeException(ex);
        }
    }
    
    private long insertOrderAndOrderItem() throws SQLException {
        Pair<Object[], Object[]> dataPair = TableCrudUtil.generateSimpleInsertData();
        jdbcTemplate.update(INSERT_ORDER, dataPair.getLeft());
        jdbcTemplate.update(INSERT_ORDER_ITEM, dataPair.getRight());
        return Long.parseLong(dataPair.getLeft()[0].toString());
    }
    
    private void updateOrderAndOrderItem(final long primaryKey) throws SQLException {
        jdbcTemplate.update(UPDATE_ORDER_BY_ID, null, null, primaryKey);
        long epochSecond = Instant.now().getEpochSecond();
        jdbcTemplate.update(UPDATE_ORDER_BY_ID, "update" + epochSecond, epochSecond, primaryKey);
        jdbcTemplate.update(UPDATE_ORDER_ITEM_BY_ID, "changed" + epochSecond, primaryKey);
    }
    
    private void deleteOrderAndOrderItem(final long primaryKey) throws SQLException {
        jdbcTemplate.execute(String.format(commonSQLCommand.getDeleteOrder(), primaryKey));
        jdbcTemplate.execute(String.format(commonSQLCommand.getDeleteOrderItem(), primaryKey));
    }
}
