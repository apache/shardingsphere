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

package org.apache.shardingsphere.integration.data.pipline.cases;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.data.pipline.cases.command.CommonSQLCommand;
import org.apache.shardingsphere.integration.data.pipline.util.TableCrudUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public final class IncrementTaskRunnable implements Runnable {
    
    private final Connection connection;
    
    private final CommonSQLCommand commonSQLCommand;
    
    @Override
    public void run() {
        int executeCount = 0;
        List<Long> newPrimaryKeys = new LinkedList<>();
        try {
            while (!Thread.currentThread().isInterrupted() && executeCount < 20) {
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
        } finally {
            try {
                connection.close();
            } catch (final SQLException ex) {
                log.error("IncrementTaskThread error", ex);
            }
        }
    }
    
    private long insertOrderAndOrderItem() throws SQLException {
        return TableCrudUtil.insertOrderAndOrderItem(connection, commonSQLCommand.getSimpleInsertOrder(), commonSQLCommand.getInsertOrderItem());
    }
    
    private void updateOrderAndOrderItem(final long primaryKey) throws SQLException {
        connection.createStatement().execute(String.format(commonSQLCommand.getUpdateOrder(), primaryKey));
        connection.createStatement().execute(String.format(commonSQLCommand.getUpdateOrderItem(), primaryKey));
    }
    
    private void deleteOrderAndOrderItem(final long primaryKey) throws SQLException {
        connection.createStatement().execute(String.format(commonSQLCommand.getDeleteOrder(), primaryKey));
        connection.createStatement().execute(String.format(commonSQLCommand.getDeleteOrderItem(), primaryKey));
    }
}
