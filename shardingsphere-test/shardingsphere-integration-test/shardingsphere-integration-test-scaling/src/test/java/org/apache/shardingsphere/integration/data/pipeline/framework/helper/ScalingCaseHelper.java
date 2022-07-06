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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Scaling case helper, some config is different between different database.
 */
public final class ScalingCaseHelper {
    
    /**
     * Get query properties by database type.
     *
     * @return query properties
     */
    public static Properties getPostgreSQLQueryProperties() {
        Properties result = new Properties();
        result.put("preferQueryMode", "extendedForPrepared");
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
        for (int i = 0; i < insertRows; i++) {
            int orderId = generateInt(0, 6);
            int userId = generateInt(0, 6);
            LocalDateTime now = LocalDateTime.now();
            int randomInt = generateInt(-100, 100);
            int randomUnsignedInt = generateInt(0, 100);
            if (databaseType instanceof MySQLDatabaseType) {
                Object[] addObjs = {keyGenerateAlgorithm.generateKey(), orderId, userId, generateString(6), randomInt, randomInt, randomInt,
                        randomUnsignedInt, randomUnsignedInt, randomUnsignedInt, randomUnsignedInt, generateFloat(), generateDouble(-1000, 100000),
                        BigDecimal.valueOf(generateDouble(1, 100)), now, now, now.toLocalDate(), now.toLocalTime(), null, "1", "t", "e", "s", "t", generateString(2), generateString(1),
                        generateString(1), "1", "2", "{}"};
                orderData.add(addObjs);
            } else {
                orderData.add(new Object[]{keyGenerateAlgorithm.generateKey(), orderId, userId, generateString(6), randomInt,
                        BigDecimal.valueOf(generateDouble(1, 100)), true, generateString(2), generateString(2), generateFloat(),
                        generateDouble(0, 1000), LocalDateTime.now(), OffsetDateTime.now()});
            }
            orderItemData.add(new Object[]{keyGenerateAlgorithm.generateKey(), orderId, userId, "SUCCESS"});
        }
        return Pair.of(orderData, orderItemData);
    }
    
    private static int generateInt(final int min, final int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
    
    private static String generateString(final int strLength) {
        return RandomStringUtils.randomAlphabetic(strLength);
    }
    
    private static float generateFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }
    
    private static double generateDouble(final double min, final double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
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
