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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Scaling case helper, some config is different between different database.
 */
public final class ScalingCaseHelper {
    
    private static final JdbcUrlAppender JDBC_URL_APPENDER = new JdbcUrlAppender();
    
    private static final String CUSTOM_SCHEMA = "test";
    
    /**
     * Get query properties by database type.
     *
     * @param databaseType database type
     * @return query properties
     */
    public static Properties getQueryPropertiesByDatabaseType(final DatabaseType databaseType) {
        Properties result = new Properties();
        if (databaseType instanceof MySQLDatabaseType) {
            result.put("useSSL", Boolean.FALSE.toString());
            result.put("rewriteBatchedStatements", Boolean.TRUE.toString());
            result.put("serverTimezone", "UTC");
            return result;
        }
        if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
            result.put("useSSL", Boolean.FALSE.toString());
            result.put("serverTimezone", "UTC");
            result.put("preferQueryMode", "extendedForPrepared");
            return result;
        }
        return result;
    }
    
    /**
     * Generate MySQL insert data, contains full fields.
     *
     * @param keyGenerateAlgorithm key generate algorithm
     * @param databaseType database type
     * @param insertRows insert rows
     * @return insert data list
     */
    public static Pair<List<Object[]>, List<Object[]>> generateFullInsertData(final KeyGenerateAlgorithm keyGenerateAlgorithm, final DatabaseType databaseType, final int insertRows) {
        if (insertRows < 0) {
            return Pair.of(null, null);
        }
        List<Object[]> orderData = new ArrayList<>(insertRows);
        List<Object[]> orderItemData = new ArrayList<>(insertRows);
        ThreadLocalRandom current = ThreadLocalRandom.current();
        for (int i = 0; i <= insertRows; i++) {
            int orderId = current.nextInt(0, 6);
            int userId = current.nextInt(0, 6);
            if (databaseType instanceof MySQLDatabaseType) {
                orderData.add(new Object[]{keyGenerateAlgorithm.generateKey(), orderId, userId, "varchar" + i, (byte) 1, new Timestamp(System.currentTimeMillis()),
                        new Timestamp(System.currentTimeMillis()), "hello".getBytes(StandardCharsets.UTF_8), null, new BigDecimal("100.00"), "test", Math.random(), "{}",
                        current.nextInt(0, 10000000)});
            } else {
                orderData.add(new Object[]{keyGenerateAlgorithm.generateKey(), ThreadLocalRandom.current().nextInt(0, 6), ThreadLocalRandom.current().nextInt(0, 6), "OK"});
            }
            orderItemData.add(new Object[]{keyGenerateAlgorithm.generateKey(), orderId, userId, "SUCCESS"});
        }
        return Pair.of(orderData, orderItemData);
    }
    
    /**
     * Get username by database type.
     *
     * @param databaseType database type
     * @return username
     */
    public static String getUsername(final DatabaseType databaseType) {
        if (databaseType instanceof OpenGaussDatabaseType) {
            return "gaussdb";
        }
        return "root";
    }
    
    /**
     * Get password by database type.
     *
     * @param databaseType database type
     * @return username
     */
    public static String getPassword(final DatabaseType databaseType) {
        if (databaseType instanceof OpenGaussDatabaseType) {
            return "Root@123";
        }
        return "root";
    }
}
