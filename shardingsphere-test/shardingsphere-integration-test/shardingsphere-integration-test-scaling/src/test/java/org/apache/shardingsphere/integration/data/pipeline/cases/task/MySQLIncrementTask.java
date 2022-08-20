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

package org.apache.shardingsphere.integration.data.pipeline.cases.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.data.pipeline.cases.base.BaseIncrementTask;
import org.apache.shardingsphere.integration.data.pipeline.framework.helper.ScalingCaseHelper;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
public final class MySQLIncrementTask extends BaseIncrementTask {
    
    private final JdbcTemplate jdbcTemplate;
    
    private final KeyGenerateAlgorithm primaryKeyGenerateAlgorithm;
    
    private final Boolean incrementOrderItemTogether;
    
    private final int executeCountLimit;
    
    @Override
    public void run() {
        int executeCount = 0;
        while (executeCount < executeCountLimit && !Thread.currentThread().isInterrupted()) {
            Object orderPrimaryKey = insertOrder();
            if (executeCount % 2 == 0) {
                jdbcTemplate.update("DELETE FROM t_order WHERE id = ?", orderPrimaryKey);
            } else {
                setNullToOrderFields(orderPrimaryKey);
                updateOrderByPrimaryKey(orderPrimaryKey);
            }
            if (incrementOrderItemTogether) {
                Object orderItemPrimaryKey = insertOrderItem();
                jdbcTemplate.update("UPDATE t_order_item SET status = ? WHERE item_id = ?", "updated" + Instant.now().getEpochSecond(), orderItemPrimaryKey);
            }
            executeCount++;
        }
        log.info("MySQL increment task runnable execute successfully.");
    }
    
    private Object insertOrder() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String status = random.nextInt() % 2 == 0 ? null : "NOT-NULL";
        Object[] orderInsertDate = new Object[]{primaryKeyGenerateAlgorithm.generateKey(), ScalingCaseHelper.generateSnowflakeKey(), random.nextInt(0, 6),
                random.nextInt(1, 99), status};
        jdbcTemplate.update("INSERT INTO t_order (id,order_id,user_id,t_unsigned_int,status) VALUES (?, ?, ?, ?, ?)", orderInsertDate);
        return orderInsertDate[0];
    }
    
    private Object insertOrderItem() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String status = random.nextInt() % 2 == 0 ? null : "NOT-NULL";
        Object[] orderInsertItemDate = new Object[]{primaryKeyGenerateAlgorithm.generateKey(), ScalingCaseHelper.generateSnowflakeKey(), random.nextInt(0, 6), status};
        jdbcTemplate.update("INSERT INTO t_order_item(item_id,order_id,user_id,status) VALUES(?,?,?,?)", orderInsertItemDate);
        return orderInsertItemDate[0];
    }
    
    private void updateOrderByPrimaryKey(final Object primaryKey) {
        Object[] updateData = {"updated" + Instant.now().getEpochSecond(), ThreadLocalRandom.current().nextInt(0, 100), primaryKey};
        jdbcTemplate.update("UPDATE t_order SET status = ?,t_unsigned_int = ? WHERE id = ?", updateData);
        // TODO may cause some ci error occasional
//        jdbcTemplate.update("UPDATE t_order SET status = null,t_unsigned_int = 299,t_datetime='0000-00-00 00:00:00' WHERE id = ?", primaryKey);
    }
    
    private void setNullToOrderFields(final Object primaryKey) {
        jdbcTemplate.update("UPDATE t_order SET status = null, t_unsigned_int = null WHERE id = ?", primaryKey);
    }
}
