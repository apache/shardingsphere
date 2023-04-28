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

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.SchemaSupportedDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.BaseIncrementTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtils;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.SQLBuilderUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Slf4j
public final class E2EIncrementalTask extends BaseIncrementTask {
    
    private static final List<String> MYSQL_COLUMN_NAMES = Arrays.asList("order_id", "user_id", "status", "t_mediumint", "t_smallint", "t_tinyint", "t_unsigned_int", "t_unsigned_mediumint",
            "t_unsigned_smallint", "t_unsigned_tinyint", "t_float", "t_double", "t_decimal", "t_timestamp", "t_datetime", "t_date", "t_time", "t_year", "t_bit", "t_binary", "t_varbinary", "t_blob",
            "t_mediumblob", "t_char", "t_text", "t_mediumtext", "t_enum", "t_set", "t_json");
    
    private static final List<String> POSTGRESQL_COLUMN_NAMES = Arrays.asList("order_id", "user_id", "status", "t_int2", "t_numeric", "t_bool", "t_bytea", "t_char", "t_varchar", "t_float",
            "t_double", "t_json", "t_jsonb", "t_text", "t_date", "t_time", "t_timestamp", "t_timestamptz");
    
    private final DataSource dataSource;
    
    private final String orderTableName;
    
    private final KeyGenerateAlgorithm primaryKeyGenerateAlgorithm;
    
    private final DatabaseType databaseType;
    
    private final int loopCount;
    
    @Override
    public void run() {
        List<Object[]> orderInsertData = PipelineCaseHelper.generateOrderInsertData(databaseType, primaryKeyGenerateAlgorithm, loopCount);
        List<Object> primaryKeys = new LinkedList<>();
        for (Object[] each : orderInsertData) {
            primaryKeys.add(each[0]);
            insertOrder(each);
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < Math.max(1, loopCount / 3); i++) {
            // TODO 0000-00-00 00:00:00 now will cause consistency check failed of MySQL.
            // DataSourceUtil.execute(dataSource, String.format("UPDATE %s SET t_datetime='0000-00-00 00:00:00' WHERE order_id = ?", orderTableName)
            updateOrderById(primaryKeys.get(random.nextInt(0, primaryKeys.size())));
        }
        for (int i = 0; i < Math.max(1, loopCount / 3); i++) {
            setNullToAllFields(primaryKeys.get(random.nextInt(0, primaryKeys.size())));
            deleteOrderById(primaryKeys.get(random.nextInt(0, primaryKeys.size())));
        }
        log.info("increment task runnable execute successfully.");
    }
    
    private void insertOrder(final Object[] orderInsertData) {
        String sql;
        if (databaseType instanceof MySQLDatabaseType) {
            sql = SQLBuilderUtils.buildInsertSQL(MYSQL_COLUMN_NAMES, orderTableName);
        } else if (databaseType instanceof SchemaSupportedDatabaseType) {
            sql = SQLBuilderUtils.buildInsertSQL(POSTGRESQL_COLUMN_NAMES, orderTableName);
        } else {
            throw new UnsupportedOperationException();
        }
        DataSourceExecuteUtils.execute(dataSource, sql, orderInsertData);
    }
    
    private void updateOrderById(final Object orderId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randomInt = random.nextInt(-100, 100);
        if (databaseType instanceof MySQLDatabaseType) {
            String sql = SQLBuilderUtils.buildUpdateSQL(ignoreShardingColumns(MYSQL_COLUMN_NAMES), orderTableName, "?");
            int randomUnsignedInt = random.nextInt(10, 100);
            LocalDateTime now = LocalDateTime.now();
            Object[] parameters = {"中文测试", randomInt, randomInt, randomInt, randomUnsignedInt, randomUnsignedInt, randomUnsignedInt,
                    randomUnsignedInt, 1.0F, 1.0, new BigDecimal("999"), now, now, now.toLocalDate(), now.toLocalTime(), Year.now().getValue() + 1, new byte[]{}, new byte[]{1, 2, -1, -3},
                    "D".getBytes(), "A".getBytes(), "T".getBytes(), "E", "text", "mediumText", "3", "3", PipelineCaseHelper.generateJsonString(32, true), orderId};
            log.info("update sql: {}, params: {}", sql, parameters);
            DataSourceExecuteUtils.execute(dataSource, sql, parameters);
            return;
        }
        if (databaseType instanceof SchemaSupportedDatabaseType) {
            String sql = SQLBuilderUtils.buildUpdateSQL(ignoreShardingColumns(POSTGRESQL_COLUMN_NAMES), orderTableName, "?");
            Object[] parameters = {"中文测试", randomInt, BigDecimal.valueOf(10000), true, new byte[]{}, "char", "varchar", PipelineCaseHelper.generateFloat(),
                    PipelineCaseHelper.generateDouble(), PipelineCaseHelper.generateJsonString(10, true), PipelineCaseHelper.generateJsonString(20, true), "text-update", LocalDate.now(),
                    LocalTime.now(), Timestamp.valueOf(LocalDateTime.now()), OffsetDateTime.now(), orderId};
            log.info("update sql: {}, params: {}", sql, parameters);
            DataSourceExecuteUtils.execute(dataSource, sql, parameters);
        }
    }
    
    private List<String> ignoreShardingColumns(final List<String> columnNames) {
        return new ArrayList<>(columnNames.subList(2, columnNames.size()));
    }
    
    private void deleteOrderById(final Object orderId) {
        String sql = SQLBuilderUtils.buildDeleteSQL(orderTableName, "order_id");
        log.info("delete sql: {}, params: {}", sql, orderId);
        DataSourceExecuteUtils.execute(dataSource, sql, new Object[]{orderId});
    }
    
    private void setNullToAllFields(final Object orderId) {
        if (databaseType instanceof MySQLDatabaseType) {
            String sql = SQLBuilderUtils.buildUpdateSQL(ignoreShardingColumns(MYSQL_COLUMN_NAMES), orderTableName, "null");
            log.info("update sql: {}", sql);
            DataSourceExecuteUtils.execute(dataSource, sql, new Object[]{orderId});
        }
    }
}
