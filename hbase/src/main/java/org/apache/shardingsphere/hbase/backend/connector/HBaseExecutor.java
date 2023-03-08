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

package org.apache.shardingsphere.hbase.backend.connector;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;
import org.apache.shardingsphere.hbase.backend.context.HBaseContext;
import org.apache.shardingsphere.hbase.backend.exception.HBaseOperationException;
import java.io.IOException;

/**
 * Execute HBase operation.
 */
@Slf4j
public final class HBaseExecutor {
    
    /**
     * Do operation in HBase, wrapper HBase Exception.
     * <p>If we need cache Table, do that in here.</p>
     *
     * @param tableName tableName
     * @param operation operation
     */
    public static void executeUpdate(final String tableName, final HBaseUpdateCallback operation) {
        TableName backendTableName = TableName.valueOf(tableName);
        try (Table table = HBaseContext.getInstance().getConnection(tableName).getTable(backendTableName)) {
            try {
                operation.executeInHBase(table);
            } catch (IOException e) {
                log.info(String.format("query hbase table: %s, execute hbase fail", tableName));
                log.error(e.toString());
                throw new HBaseOperationException(e.getMessage());
            }
        } catch (IOException e) {
            log.info(String.format("query hbase table: %s, execute hbase fail", tableName));
            log.error(e.toString());
            throw new HBaseOperationException(e.getMessage());
        }
    }
    
    /**
     * Do operation in HBase, wrapper HBase Exception.
     * <p>If we need cache Table, do that in here.</p>
     *
     * @param tableName tableName
     * @param operation operation
     * @param <R> Result Type
     * @return result
     */
    public static <R> R executeQuery(final String tableName, final HBaseQueryCallback<R> operation) {
        TableName backendTableName = TableName.valueOf(tableName);
        try (Table table = HBaseContext.getInstance().getConnection(tableName).getTable(backendTableName)) {
            R result;
            try {
                result = operation.executeInHBase(table);
            } catch (IOException e) {
                throw new HBaseOperationException(e.getMessage());
            }
            return result;
        } catch (IOException e) {
            throw new HBaseOperationException(e.getMessage());
        }
    }
    
    /**
     * Do operation in HBase, wrapper HBase Exception.
     * <p>If we need cache Table, do that in here.</p>
     *
     * @param connection HBase connection
     * @param operation operation
     * @param <R> Result Type
     * @return result.
     */
    public static <R> R executeAdmin(final Connection connection, final HBaseAdminCallback<R> operation) {
        try (Admin admin = connection.getAdmin()) {
            R result;
            try {
                result = operation.executeInHBase(admin);
            } catch (IOException e) {
                throw new HBaseOperationException(e.getMessage());
            }
            return result;
        } catch (IOException e) {
            throw new HBaseOperationException(e.getMessage());
        }
    }
}
