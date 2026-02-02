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

package org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.util.RandomStrings;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.mock;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PipelineCaseHelper {
    
    /**
     * Generate a pseudorandom integer in the specified range.
     *
     * @param min lower bound (inclusive)
     * @param max upper bound (exclusive)
     * @return a pseudorandom int between {@code min} (inclusive) and {@code max} (exclusive)
     */
    public static int generateInt(final int min, final int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
    
    /**
     * Generate a random alphanumeric string of the given length.
     *
     * @param strLength desired string length
     * @return random alphanumeric string
     */
    public static String generateString(final int strLength) {
        return RandomStrings.randomAlphanumeric(strLength);
    }
    
    /**
     * Generate json string.
     *
     * @param useUnicodeCharacter use unicode character
     * @param length length
     * @return json string
     */
    public static String generateJsonString(final int length, final boolean useUnicodeCharacter) {
        String value;
        if (useUnicodeCharacter) {
            value = Strings.repeat("{''中 } A'", Math.max(1, length / 10));
        } else {
            value = generateString(length);
        }
        return String.format("{\"test\":\"%s\"}", value);
    }
    
    /**
     * Generate float value.
     *
     * @return float.
     */
    public static float generateFloat() {
        return ThreadLocalRandom.current().nextInt(-1000, 1000) / 100.0F;
    }
    
    /**
     * Generate double value.
     *
     * @return double
     */
    public static double generateDouble() {
        return ThreadLocalRandom.current().nextInt(-1000000000, 1000000000) / 1000000.0D;
    }
    
    /**
     * Batch insert order records with general columns.
     *
     * @param tableName table name
     * @param connection connection
     * @param keyGenerateAlgorithm key generate algorithm
     * @param recordCount record count
     * @throws SQLException sql exception
     */
    public static void batchInsertOrderRecordsWithGeneralColumns(final Connection connection, final KeyGenerateAlgorithm keyGenerateAlgorithm, final String tableName,
                                                                 final int recordCount) throws SQLException {
        log.info("init data begin: {}", LocalDateTime.now());
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("INSERT INTO %s (order_id,user_id,status) VALUES (?,?,?)", tableName))) {
            for (int i = 0; i < recordCount; i++) {
                preparedStatement.setObject(1, keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next());
                preparedStatement.setObject(2, ThreadLocalRandom.current().nextInt(0, 6));
                preparedStatement.setObject(3, "OK");
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        log.info("init data end: {}", LocalDateTime.now());
    }
}
