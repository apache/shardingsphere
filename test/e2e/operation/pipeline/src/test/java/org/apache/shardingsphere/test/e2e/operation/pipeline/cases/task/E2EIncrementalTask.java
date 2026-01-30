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

package org.apache.shardingsphere.test.e2e.operation.pipeline.cases.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.database.connector.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.database.connector.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.test.e2e.operation.pipeline.dao.order.large.IntPkLargeOrderDAO;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.DataSourceExecuteUtils;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.SQLBuilderUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Slf4j
public final class E2EIncrementalTask implements Runnable {
    
    private static final List<String> MYSQL_COLUMN_NAMES = Arrays.asList("order_id", "user_id", "status", "t_mediumint", "t_smallint", "t_tinyint", "t_unsigned_int", "t_unsigned_mediumint",
            "t_unsigned_smallint", "t_unsigned_tinyint", "t_float", "t_double", "t_decimal", "t_timestamp", "t_datetime", "t_date", "t_time", "t_year", "t_bit", "t_binary", "t_varbinary", "t_blob",
            "t_mediumblob", "t_char", "t_text", "t_mediumtext", "t_enum", "t_set", "t_json");
    
    private static final List<String> POSTGRESQL_COLUMN_NAMES = Arrays.asList("order_id", "user_id", "status", "t_int2", "t_numeric", "t_bool", "t_bytea", "t_char", "t_varchar", "t_float",
            "t_double", "t_json", "t_jsonb", "t_text", "t_date", "t_time", "t_timestamp", "t_timestamptz");
    
    private static final List<String> OPENGAUSS_COLUMN_NAMES = Arrays.asList("order_id", "user_id", "status", "c_int", "c_smallint", "c_float", "c_double", "c_numeric", "c_boolean",
            "c_char", "c_text", "c_bytea", "c_raw", "c_date", "c_time", "c_smalldatetime", "c_timestamp", "c_timestamptz", "c_interval", "c_array", "c_json", "c_jsonb", "c_uuid", "c_hash32",
            "c_tsvector", "c_tsquery", "c_bit", "c_int4range", "c_daterange", "c_tsrange", "c_reltime", "c_abstime", "c_point", "c_lseg", "c_box", "c_circle", "c_bitvarying", "c_cidr", "c_inet",
            "c_macaddr", "c_hll", "c_money");
    
    private final DataSource dataSource;
    
    private final String orderQualifiedTableName;
    
    private final KeyGenerateAlgorithm primaryKeyGenerateAlgorithm;
    
    private final DatabaseType databaseType;
    
    private final int loopCount;
    
    @Override
    public void run() {
        try {
            run0();
        } catch (final SQLException ex) {
            log.error("Increment task runnable execute failed.", ex);
            throw new SQLWrapperException(ex);
        }
    }
    
    private void run0() throws SQLException {
        List<Object[]> orderInsertData = new IntPkLargeOrderDAO(dataSource, databaseType, orderQualifiedTableName).generateInsertData(primaryKeyGenerateAlgorithm, loopCount);
        List<Object> primaryKeys = new LinkedList<>();
        for (Object[] each : orderInsertData) {
            primaryKeys.add(each[0]);
            insertOrder(each);
        }
        Iterator<Object> primaryKeysIterator = primaryKeys.iterator();
        Iterator<List<IncrementalAction>> incrementalActionsIterator = Arrays.asList(
                Arrays.asList(IncrementalAction.PLAIN_UPDATE, IncrementalAction.UPDATE_NULL), Arrays.asList(IncrementalAction.UPDATE_NULL, IncrementalAction.PLAIN_UPDATE),
                Arrays.asList(IncrementalAction.PLAIN_UPDATE, IncrementalAction.DELETE), Arrays.asList(IncrementalAction.UPDATE_NULL, IncrementalAction.DELETE),
                Collections.singletonList(IncrementalAction.PLAIN_UPDATE), Collections.singletonList(IncrementalAction.UPDATE_NULL), Collections.singletonList(IncrementalAction.DELETE)).iterator();
        while (primaryKeysIterator.hasNext() && incrementalActionsIterator.hasNext()) {
            doIncrementalChanges(primaryKeysIterator.next(), incrementalActionsIterator.next());
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (primaryKeysIterator.hasNext()) {
            doIncrementalChanges(primaryKeysIterator.next(), Collections.singletonList(IncrementalAction.values()[random.nextInt(0, IncrementalAction.values().length)]));
        }
        // TODO 0000-00-00 00:00:00 now will cause consistency check failed of MySQL.
        // DataSourceUtil.execute(dataSource, String.format("UPDATE %s SET t_datetime='0000-00-00 00:00:00' WHERE order_id = ?", orderTableName)
        log.info("increment task runnable execute successfully.");
    }
    
    // TODO Refactor into SPI
    private void insertOrder(final Object[] orderInsertData) throws SQLException {
        String sql;
        if (databaseType instanceof MySQLDatabaseType) {
            sql = SQLBuilderUtils.buildInsertSQL(MYSQL_COLUMN_NAMES, orderQualifiedTableName);
        } else if (databaseType instanceof PostgreSQLDatabaseType) {
            sql = SQLBuilderUtils.buildInsertSQL(POSTGRESQL_COLUMN_NAMES, orderQualifiedTableName);
        } else if (databaseType instanceof OpenGaussDatabaseType) {
            sql = SQLBuilderUtils.buildInsertSQL(OPENGAUSS_COLUMN_NAMES, orderQualifiedTableName);
        } else {
            throw new UnsupportedOperationException("");
        }
        DataSourceExecuteUtils.execute(dataSource, sql, orderInsertData);
    }
    
    private void doIncrementalChanges(final Object orderId, final List<IncrementalAction> actions) throws SQLException {
        for (IncrementalAction each : actions) {
            switch (each) {
                case PLAIN_UPDATE:
                    updateOrderById(orderId);
                    break;
                case UPDATE_NULL:
                    setNullToAllFields(orderId);
                    break;
                case DELETE:
                    deleteOrderById(orderId);
                    break;
                default:
                    throw new UnsupportedOperationException("");
            }
        }
    }
    
    private void updateOrderById(final Object orderId) throws SQLException {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randomInt = random.nextInt(-100, 100);
        if (databaseType instanceof MySQLDatabaseType) {
            String sql = SQLBuilderUtils.buildUpdateSQL(ignoreShardingColumns(MYSQL_COLUMN_NAMES), orderQualifiedTableName, "?");
            int randomUnsignedInt = random.nextInt(10, 100);
            LocalDateTime now = LocalDateTime.now();
            Object[] parameters = {"中文测试", randomInt, randomInt, randomInt, randomUnsignedInt, randomUnsignedInt, randomUnsignedInt,
                    randomUnsignedInt, 1.0F, 1.0, new BigDecimal("999"), now, now, now.toLocalDate(), now.toLocalTime(), Year.now().getValue() + 1,
                    new byte[]{-1, 0, 1}, new byte[]{1, 2, -1, -3},
                    "D".getBytes(), "A".getBytes(), "T".getBytes(), "E", "text", "mediumText", "3", "3", PipelineCaseHelper.generateJsonString(32, true), orderId};
            log.info("update sql: {}, params: {}", sql, parameters);
            DataSourceExecuteUtils.execute(dataSource, sql, parameters);
            return;
        }
        if (databaseType instanceof PostgreSQLDatabaseType) {
            String sql = SQLBuilderUtils.buildUpdateSQL(ignoreShardingColumns(POSTGRESQL_COLUMN_NAMES), orderQualifiedTableName, "?");
            Object[] parameters = {"中文测试", randomInt, BigDecimal.valueOf(10000), random.nextBoolean(), new byte[]{-1, 0, 1}, "char", "varchar", PipelineCaseHelper.generateFloat(),
                    PipelineCaseHelper.generateDouble(), PipelineCaseHelper.generateJsonString(10, true), PipelineCaseHelper.generateJsonString(20, true), "text-update", LocalDate.now(),
                    LocalTime.now(), Timestamp.valueOf(LocalDateTime.now()), OffsetDateTime.now(), orderId};
            log.info("update sql: {}, params: {}", sql, parameters);
            DataSourceExecuteUtils.execute(dataSource, sql, parameters);
            return;
        }
        if (databaseType instanceof OpenGaussDatabaseType) {
            LocalDateTime now = LocalDateTime.now();
            String sql = SQLBuilderUtils.buildUpdateSQL(ignoreShardingColumns(OPENGAUSS_COLUMN_NAMES), orderQualifiedTableName, "?");
            Object[] parameters = {"中文测试", randomInt, random.nextInt(-999, 999), PipelineCaseHelper.generateFloat(), PipelineCaseHelper.generateDouble(), BigDecimal.valueOf(10000),
                    random.nextBoolean(), "update-char", "update-text", new byte[]{-1, 0, 1}, new byte[]{1, 0}, now.toLocalDate().plusDays(1), now.toLocalTime().plusHours(6), "2023-03-01", now,
                    OffsetDateTime.now(), "1 years 1 mons 1 days 1 hours 1 mins 1 secs", "{4, 5, 6}", PipelineCaseHelper.generateJsonString(1, true), PipelineCaseHelper.generateJsonString(1, false),
                    UUID.fromString("00000000-000-0000-0000-000000000001").toString(), DigestUtils.md5Hex(now.toString()), "aaa", "bbb", "1111", "[1,10000)", "[2020-01-02,2021-01-01)",
                    "[2020-01-01 00:00:00,2021-01-01 00:00:00)", "2 years 2 mons 2 days 06:00:00", "2023-01-01 00:00:00+00", "(2.0,2.0)", "[(0.0,0.0),(3.0,3.0)]", "(1.0,1.0),(3.0,3.0)",
                    "<(5.0,5.0),1.0>", "1010", "192.168.0.0/24", "192.168.1.1", "08:00:3b:01:02:03", null, 1000, orderId};
            log.info("update sql: {}, params: {}", sql, parameters);
            DataSourceExecuteUtils.execute(dataSource, sql, parameters);
        }
    }
    
    private List<String> ignoreShardingColumns(final List<String> columnNames) {
        return new ArrayList<>(columnNames.subList(2, columnNames.size()));
    }
    
    private void deleteOrderById(final Object orderId) throws SQLException {
        String sql = SQLBuilderUtils.buildDeleteSQL(orderQualifiedTableName, "order_id");
        log.info("delete sql: {}, params: {}", sql, orderId);
        DataSourceExecuteUtils.execute(dataSource, sql, new Object[]{orderId});
    }
    
    private void setNullToAllFields(final Object orderId) throws SQLException {
        if (databaseType instanceof MySQLDatabaseType) {
            String sql = SQLBuilderUtils.buildUpdateSQL(ignoreShardingColumns(MYSQL_COLUMN_NAMES), orderQualifiedTableName, "null");
            log.info("update sql: {}", sql);
            DataSourceExecuteUtils.execute(dataSource, sql, new Object[]{orderId});
        }
    }
    
    private enum IncrementalAction {
        PLAIN_UPDATE, UPDATE_NULL, DELETE
    }
}
