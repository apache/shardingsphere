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
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.BaseIncrementTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtils;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Slf4j
// TODO remove later
public final class MySQLIncrementTask extends BaseIncrementTask {
    
    private final DataSource dataSource;
    
    private final String orderTableName;
    
    private final KeyGenerateAlgorithm primaryKeyGenerateAlgorithm;
    
    private final int executeCountLimit;
    
    @Override
    public void run() {
        int executeCount = 0;
        while (executeCount < executeCountLimit && !Thread.currentThread().isInterrupted()) {
            Object orderPrimaryKey = insertOrder();
            if (0 == executeCount % 2) {
                String sql = String.format("DELETE FROM %s WHERE order_id = ?", orderTableName);
                DataSourceExecuteUtils.execute(dataSource, sql, new Object[]{orderPrimaryKey});
            } else {
                setNullToOrderFields(orderPrimaryKey);
                updateOrderByPrimaryKey(orderPrimaryKey);
            }
            executeCount++;
        }
        log.info("MySQL increment task runnable execute successfully.");
    }
    
    private Object insertOrder() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Object[] orderInsertDate = new Object[]{primaryKeyGenerateAlgorithm.generateKey(), random.nextInt(0, 6),
                random.nextInt(1, 99), "中文测试"};
        DataSourceExecuteUtils.execute(dataSource, String.format("INSERT INTO %s (order_id,user_id,t_unsigned_int,status) VALUES (?, ?, ?, ?)", orderTableName), orderInsertDate);
        return orderInsertDate[0];
    }
    
    private void updateOrderByPrimaryKey(final Object primaryKey) {
        Object[] updateData = {"updated" + Instant.now().getEpochSecond(), ThreadLocalRandom.current().nextInt(0, 100), primaryKey};
        DataSourceExecuteUtils.execute(dataSource, String.format("UPDATE %s SET t_char = ?,t_unsigned_int = ? WHERE order_id = ?", orderTableName), updateData);
        // TODO 0000-00-00 00:00:00 now will cause consistency check failed.
        // jdbcTemplate.update(String.format("UPDATE %s SET t_char = null,t_unsigned_int = 299,t_datetime='0000-00-00 00:00:00' WHERE order_id = ?", orderTableName), primaryKey);
        /*
         * DataSourceUtil.execute(dataSource, String.format("UPDATE %s SET t_char = null,t_unsigned_int = 299,t_datetime='0000-00-00 00:00:00' WHERE order_id = ?", orderTableName), new
         * Object[]{primaryKey});
         */
    }
    
    private void setNullToOrderFields(final Object primaryKey) {
        DataSourceExecuteUtils.execute(dataSource, String.format("UPDATE %s SET t_char = null, t_unsigned_int = null WHERE order_id = ?", orderTableName), new Object[]{primaryKey});
    }
}
