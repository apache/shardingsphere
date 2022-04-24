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

package org.apache.shardingsphere.integration.data.pipline.util;

import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.ThreadLocalRandom;

public final class TableCrudUtil {
    
    private static final SnowflakeKeyGenerateAlgorithm SNOWFLAKE_GENERATE = new SnowflakeKeyGenerateAlgorithm();
    
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    
    /**
     * insert order data.
     *
     * @param orderStatement prepared statement of order
     * @param itemStatement prepared statement of item
     * @param insertRows insert rows
     * @throws SQLException SQL exception
     */
    public static void batchInsertOrderAndOrderItem(final PreparedStatement orderStatement, final PreparedStatement itemStatement, final int insertRows) throws SQLException {
        if (insertRows < 0) {
            return;
        }
        for (int i = 1; i <= insertRows; i++) {
            orderStatement.setLong(1, (Long) SNOWFLAKE_GENERATE.generateKey());
            int orderId = RANDOM.nextInt(0, 5);
            orderStatement.setInt(2, orderId);
            int userId = RANDOM.nextInt(0, 5);
            orderStatement.setInt(3, userId);
            orderStatement.setString(4, "varchar" + i);
            orderStatement.setByte(5, (byte) 1);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            orderStatement.setTimestamp(6, timestamp);
            orderStatement.setTimestamp(7, timestamp);
            orderStatement.setBytes(8, "hello".getBytes(StandardCharsets.UTF_8));
            orderStatement.setBinaryStream(9, null);
            orderStatement.setBigDecimal(10, new BigDecimal("100.00"));
            orderStatement.setString(11, "test");
            orderStatement.setDouble(12, Math.random());
            orderStatement.setString(13, "{}");
            orderStatement.addBatch();
            itemStatement.setLong(1, (Long) SNOWFLAKE_GENERATE.generateKey());
            itemStatement.setInt(2, orderId);
            itemStatement.setInt(3, userId);
            itemStatement.setString(4, "SUCCESS");
            itemStatement.addBatch();
        }
    }
    
    /**
     * insert order data.
     *
     * @param connection connection
     * @param insertOrderSimpleSql insert order sql
     * @param insertOrderItemSimpleSql insert order item sql
     * @return primary key
     * @throws SQLException SQL exception
     */
    public static long insertOrderAndOrderItem(final Connection connection, final String insertOrderSimpleSql, final String insertOrderItemSimpleSql) throws SQLException {
        long primaryKey = (Long) SNOWFLAKE_GENERATE.generateKey();
        int orderId = RANDOM.nextInt(0, 5);
        int userId = RANDOM.nextInt(0, 5);
        try (PreparedStatement orderStatement = connection.prepareStatement(insertOrderSimpleSql); PreparedStatement itemStatement = connection.prepareStatement(insertOrderItemSimpleSql)) {
            orderStatement.setLong(1, primaryKey);
            orderStatement.setInt(2, orderId);
            orderStatement.setInt(3, userId);
            orderStatement.execute();
            itemStatement.setLong(1, primaryKey);
            itemStatement.setInt(2, orderId);
            itemStatement.setInt(3, userId);
            itemStatement.setString(4, "OK");
            itemStatement.execute();
        }
        return primaryKey;
    }
}
