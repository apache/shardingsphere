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

package org.apache.shardingsphere.integration.data.pipeline.util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class TableCrudUtil {
    
    private static final SnowflakeKeyGenerateAlgorithm SNOWFLAKE_GENERATE = new SnowflakeKeyGenerateAlgorithm();
    
    /**
     * Generate MySQL insert data, contains full fields.
     *
     * @param insertRows insert rows
     * @return insert data list
     */
    public static Pair<List<Object[]>, List<Object[]>> generateMySQLInsertDataList(final int insertRows) {
        if (insertRows < 0) {
            return Pair.of(null, null);
        }
        List<Object[]> orderData = new ArrayList<>(insertRows);
        List<Object[]> orderItemData = new ArrayList<>(insertRows);
        ThreadLocalRandom current = ThreadLocalRandom.current();
        for (int i = 0; i <= insertRows; i++) {
            int orderId = current.nextInt(0, 6);
            int userId = current.nextInt(0, 6);
            orderData.add(new Object[]{SNOWFLAKE_GENERATE.generateKey(), orderId, userId, "varchar" + i, (byte) 1, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()),
                    "hello".getBytes(StandardCharsets.UTF_8), null, new BigDecimal("100.00"), "test", Math.random(), "{}", current.nextInt(0, 10000000)});
            orderItemData.add(new Object[]{SNOWFLAKE_GENERATE.generateKey(), orderId, userId, "SUCCESS"});
        }
        return Pair.of(orderData, orderItemData);
    }
    
    /**
     * Generate PostgreSQL insert data, contains full fields.
     *
     * @param insertRows insert rows
     * @return insert data
     */
    public static Pair<List<Object[]>, List<Object[]>> generatePostgresSQLInsertDataList(final int insertRows) {
        List<Object[]> orderData = new ArrayList<>(insertRows);
        List<Object[]> orderItemData = new ArrayList<>(insertRows);
        for (int i = 1; i <= insertRows; i++) {
            int orderId = ThreadLocalRandom.current().nextInt(0, 6);
            int userId = ThreadLocalRandom.current().nextInt(0, 6);
            orderData.add(new Object[]{SNOWFLAKE_GENERATE.generateKey(), orderId, userId, "OK"});
            orderItemData.add(new Object[]{SNOWFLAKE_GENERATE.generateKey(), orderId, userId, "SUCCESS"});
        }
        return Pair.of(orderData, orderItemData);
    }
}
