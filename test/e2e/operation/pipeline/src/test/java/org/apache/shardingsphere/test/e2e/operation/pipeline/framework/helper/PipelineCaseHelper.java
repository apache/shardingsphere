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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.mariadb.type.MariaDBDatabaseType;
import org.apache.shardingsphere.database.connector.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.database.connector.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.database.connector.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.AutoIncrementKeyGenerateAlgorithm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.mock;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PipelineCaseHelper {
    
    /**
     * Generate insert data, contains full fields.
     *
     * @param databaseType database type
     * @param insertRows insert rows
     * @return insert data list
     */
    public static Pair<List<Object[]>, List<Object[]>> generateFullInsertData(final DatabaseType databaseType, final int insertRows) {
        if (insertRows < 0) {
            return Pair.of(null, null);
        }
        AutoIncrementKeyGenerateAlgorithm orderKeyGenerate = new AutoIncrementKeyGenerateAlgorithm();
        AutoIncrementKeyGenerateAlgorithm orderItemKeyGenerate = new AutoIncrementKeyGenerateAlgorithm();
        List<Object[]> orderData = generateOrderInsertData(databaseType, orderKeyGenerate, insertRows);
        List<Object[]> orderItemData = generateOrderItemInsertData(orderItemKeyGenerate, insertRows);
        return Pair.of(orderData, orderItemData);
    }
    
    /**
     * Generate order insert data.
     *
     * @param databaseType database type
     * @param keyGenerateAlgorithm key generate algorithm
     * @param insertRows insert rows
     * @return order insert data
     * @throws UnsupportedOperationException Unsupported operation exception
     */
    public static List<Object[]> generateOrderInsertData(final DatabaseType databaseType, final KeyGenerateAlgorithm keyGenerateAlgorithm, final int insertRows) {
        List<Object[]> result = new ArrayList<>(insertRows);
        String emojiText = "☠️x☺️x✋x☹️";
        if (databaseType instanceof MySQLDatabaseType || databaseType instanceof MariaDBDatabaseType) {
            for (int i = 0; i < insertRows; i++) {
                int randomInt = generateInt(-100, 100);
                Object orderId = keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
                int randomUnsignedInt = generateInt(0, 100);
                LocalDateTime now = LocalDateTime.now();
                Object[] addObjs = {orderId, generateInt(0, 100), generateString(6), randomInt, randomInt, randomInt,
                        randomUnsignedInt, randomUnsignedInt, randomUnsignedInt, randomUnsignedInt, generateFloat(), generateDouble(),
                        BigDecimal.valueOf(generateDouble()), now, now, now.toLocalDate(), now.toLocalTime(), Year.now().getValue(), "1", "t", "e", "s", "t", generateString(2),
                        emojiText, generateString(1), "1", "2", generateJsonString(32, false)};
                result.add(addObjs);
            }
            return result;
        }
        if (databaseType instanceof PostgreSQLDatabaseType) {
            for (int i = 0; i < insertRows; i++) {
                Object orderId = keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
                result.add(new Object[]{orderId, generateInt(0, 100), generateString(6), generateInt(-128, 127),
                        BigDecimal.valueOf(generateDouble()), true, "bytea".getBytes(), generateString(2), generateString(2), generateFloat(), generateDouble(),
                        generateJsonString(8, false), generateJsonString(12, true), emojiText, LocalDate.now(),
                        LocalTime.now(), Timestamp.valueOf(LocalDateTime.now()), OffsetDateTime.now()});
            }
            return result;
        }
        if (databaseType instanceof OpenGaussDatabaseType) {
            for (int i = 0; i < insertRows; i++) {
                Object orderId = keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
                byte[] bytesValue = {Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE};
                result.add(new Object[]{orderId, generateInt(0, 1000), "'status'" + i, generateInt(-1000, 9999), generateInt(0, 100), generateFloat(), generateDouble(),
                        BigDecimal.valueOf(generateDouble()), false, generateString(6), "texts", bytesValue, bytesValue, LocalDate.now(), LocalTime.now(), "2001-10-01",
                        Timestamp.valueOf(LocalDateTime.now()), OffsetDateTime.now(), "0 years 0 mons 1 days 2 hours 3 mins 4 secs", "{1, 2, 3}", generateJsonString(8, false),
                        generateJsonString(8, true), UUID.randomUUID().toString(), DigestUtils.md5Hex(orderId.toString()), "'rat' 'sat'", "tsquery", "0000", "[1,1000)", "[2020-01-02,2021-01-01)",
                        "[2020-01-01 00:00:00,2021-01-01 00:00:00)", "1 years 1 mons 10 days -06:00:00", "2000-01-02 00:00:00+00", "(1.0,1.0)", "[(0.0,0.0),(2.0,2.0)]", "(3.0,3.0),(1.0,1.0)",
                        "<(5.0,5.0),5.0>", "1111", "192.168.0.0/16", "192.168.1.1", "08:00:2b:01:02:03", "\\x484c4c00000000002b05000000000000000000000000000000000000", 999});
            }
            return result;
        }
        throw new UnsupportedOperationException(String.format("Not support generate %s insert data", databaseType.getType()));
    }
    
    private static int generateInt(final int min, final int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
    
    private static String generateString(final int strLength) {
        return RandomStringUtils.randomAlphanumeric(strLength);
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
     * Generate order item insert data.
     *
     * @param keyGenerateAlgorithm key generate algorithm
     * @param insertRows insert rows
     * @return order item insert data
     */
    public static List<Object[]> generateOrderItemInsertData(final KeyGenerateAlgorithm keyGenerateAlgorithm, final int insertRows) {
        List<Object[]> result = new ArrayList<>(insertRows);
        for (int i = 0; i < insertRows; i++) {
            Object orderId = keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
            int userId = generateInt(0, 100);
            result.add(new Object[]{keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next(), orderId, userId, "SUCCESS"});
        }
        return result;
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
