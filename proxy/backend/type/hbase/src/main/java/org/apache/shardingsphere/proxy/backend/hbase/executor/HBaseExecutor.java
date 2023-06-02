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

package org.apache.shardingsphere.proxy.backend.hbase.executor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;
import org.apache.shardingsphere.proxy.backend.hbase.context.HBaseContext;
import org.apache.shardingsphere.proxy.backend.hbase.exception.HBaseOperationException;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseAdminCallback;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseQueryCallback;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseUpdateCallback;

import java.io.IOException;

/**
 * HBase executor.
 * 
 * <p>Do not cache table here.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HBaseExecutor {
    
    /**
     * Execute update.
     *
     * @param tableName table name
     * @param operation operation
     * @throws HBaseOperationException HBase operation exception
     */
    public static void executeUpdate(final String tableName, final HBaseUpdateCallback operation) {
        try (Table table = HBaseContext.getInstance().getConnection(tableName).getTable(TableName.valueOf(tableName))) {
            executeUpdate(table, operation);
        } catch (final IOException ex) {
            throw new HBaseOperationException(ex.getMessage());
        }
    }
    
    private static void executeUpdate(final Table table, final HBaseUpdateCallback operation) {
        try {
            operation.executeInHBase(table);
        } catch (final IOException ex) {
            throw new HBaseOperationException(ex.getMessage());
        }
    }
    
    /**
     * Execute query.
     * 
     * @param tableName table name
     * @param operation operation
     * @param <T> type of result
     * @return query result
     * @throws HBaseOperationException HBase operation exception
     */
    public static <T> T executeQuery(final String tableName, final HBaseQueryCallback<T> operation) {
        TableName backendTableName = TableName.valueOf(tableName);
        try (Table table = HBaseContext.getInstance().getConnection(tableName).getTable(backendTableName)) {
            return executeQuery(table, operation);
        } catch (final IOException ex) {
            throw new HBaseOperationException(ex.getMessage());
        }
    }
    
    private static <T> T executeQuery(final Table table, final HBaseQueryCallback<T> operation) {
        try {
            return operation.executeInHBase(table);
        } catch (final IOException ex) {
            throw new HBaseOperationException(ex.getMessage());
        }
    }
    
    /**
     * Execute admin.
     * 
     * @param connection HBase connection
     * @param operation operation
     * @param <T> type of result
     * @return admin result
     * @throws HBaseOperationException HBase operation exception
     */
    public static <T> T executeAdmin(final Connection connection, final HBaseAdminCallback<T> operation) {
        try (Admin admin = connection.getAdmin()) {
            return executeAdmin(admin, operation);
        } catch (final IOException ex) {
            throw new HBaseOperationException(ex.getMessage());
        }
    }
    
    private static <T> T executeAdmin(final Admin admin, final HBaseAdminCallback<T> operation) {
        try {
            return operation.executeInHBase(admin);
        } catch (final IOException ex) {
            throw new HBaseOperationException(ex.getMessage());
        }
    }
}
