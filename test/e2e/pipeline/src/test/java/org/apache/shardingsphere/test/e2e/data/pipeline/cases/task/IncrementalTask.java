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
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.BaseIncrementTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtil;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Slf4j
public final class IncrementalTask extends BaseIncrementTask {
    
    private static final String MYSQL_UPDATE_ORDER_BY_ID = "UPDATE `%s` SET status = ?, t_mediumint=?, t_smallint=?, t_tinyint=?, t_unsigned_int=?, t_unsigned_mediumint=?, t_unsigned_smallint=?, "
            + "t_unsigned_tinyint=?, t_float=?, t_double=?, t_decimal=?, t_timestamp=?, t_datetime=?, t_date=?, t_time=?, t_year=?, t_bit=?, t_binary=?, t_varbinary=?, t_blob=?, t_mediumblob=?, "
            + "t_char=?, t_text=?, t_mediumtext=?, t_enum=?, t_set=?, t_json=? WHERE order_id = ?";
    
    private static final String POSTGRESQL_UPDATE_ORDER_BY_ID = "UPDATE %S SET status=?, t_int2=?, t_numeric=?, t_bool=?, t_bytea=?, t_char=?, t_float=?, t_double=?, t_json=?, t_jsonb=?, t_text=?, "
            + "t_date=?, t_time=?, t_timestamp=?, t_timestamptz=? WHERE order_id = ?";
    
    private final DataSource dataSource;
    
    private final String orderTableName;
    
    private final String insertTableSql;
    
    private final KeyGenerateAlgorithm primaryKeyGenerateAlgorithm;
    
    private final DatabaseType databaseType;
    
    private final int loopCount;
    
    @Override
    public void run() {
        List<Object[]> orderInsertData = PipelineCaseHelper.generateOrderInsertData(databaseType, primaryKeyGenerateAlgorithm, loopCount);
        List<Object> primaryKeys = new LinkedList<>();
        for (Object[] each : orderInsertData) {
            primaryKeys.add(each[0]);
            DataSourceExecuteUtil.execute(dataSource, insertTableSql, each);
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < Math.max(1, loopCount / 4); i++) {
            // TODO 0000-00-00 00:00:00 now will cause consistency check failed of MySQL.
            // DataSourceUtil.execute(dataSource, String.format("UPDATE %s SET t_datetime='0000-00-00 00:00:00' WHERE order_id = ?", orderTableName)
            updateOrderById(primaryKeys.get(random.nextInt(0, primaryKeys.size())));
            if (databaseType instanceof MySQLDatabaseType) {
                setNullToUnsignedFields(primaryKeys.get(random.nextInt(0, primaryKeys.size())));
            }
        }
        for (int i = 0; i < Math.max(1, loopCount / 5); i++) {
            deleteOrderById(primaryKeys.get(random.nextInt(0, primaryKeys.size())));
        }
        log.info("increment task runnable execute successfully.");
    }
    
    private void updateOrderById(final Object orderId) {
        int randomInt = ThreadLocalRandom.current().nextInt(-100, 100);
        if (databaseType instanceof MySQLDatabaseType) {
            String updateSql = String.format(MYSQL_UPDATE_ORDER_BY_ID, orderTableName);
            int randomUnsignedInt = ThreadLocalRandom.current().nextInt(10, 100);
            LocalDateTime now = LocalDateTime.now();
            DataSourceExecuteUtil.execute(dataSource, updateSql, new Object[]{"中文测试", randomInt, randomInt, randomInt, randomUnsignedInt, randomUnsignedInt, randomUnsignedInt,
                    randomUnsignedInt, 1.0F, 1.0, new BigDecimal("999"), now, now, now.toLocalDate(), now.toLocalTime(), Year.now().getValue() + 1, "U".getBytes(), "P".getBytes(),
                    "D".getBytes(), "A".getBytes(), "T".getBytes(), "E", "text", "mediumText", "3", "3", PipelineCaseHelper.generateJsonString(32, true), orderId});
            return;
        }
        if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
            String updateSql = String.format(POSTGRESQL_UPDATE_ORDER_BY_ID, orderTableName);
            DataSourceExecuteUtil.execute(dataSource, updateSql, new Object[]{"中文测试", randomInt, BigDecimal.valueOf(10000), true, "bytea-update".getBytes(), "update", 1.0F, 2.0,
                    PipelineCaseHelper.generateJsonString(10, true), PipelineCaseHelper.generateJsonString(20, true), "text-update", LocalDate.now(),
                    LocalTime.now(), Timestamp.valueOf(LocalDateTime.now()), OffsetDateTime.now(), orderId});
        }
    }
    
    private void deleteOrderById(final Object orderId) {
        String sql = String.format("DELETE FROM %s WHERE order_id = ?", orderTableName);
        DataSourceExecuteUtil.execute(dataSource, sql, new Object[]{orderId});
    }
    
    private void setNullToUnsignedFields(final Object orderId) {
        DataSourceExecuteUtil.execute(dataSource, String.format("UPDATE %s SET t_unsigned_int = null, t_unsigned_tinyint = null WHERE order_id = ?", orderTableName),
                new Object[]{orderId});
    }
}
