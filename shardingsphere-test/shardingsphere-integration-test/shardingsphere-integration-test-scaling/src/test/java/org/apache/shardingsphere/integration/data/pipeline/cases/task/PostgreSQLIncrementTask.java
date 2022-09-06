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
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.integration.data.pipeline.cases.base.BaseIncrementTask;
import org.apache.shardingsphere.integration.data.pipeline.framework.helper.ScalingCaseHelper;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
public final class PostgreSQLIncrementTask extends BaseIncrementTask {
    
    private static final KeyGenerateAlgorithm KEY_GENERATE_ALGORITHM = new SnowflakeKeyGenerateAlgorithm();
    
    private final JdbcTemplate jdbcTemplate;
    
    private final String schema;
    
    private final boolean incrementOrderItemTogether;
    
    private final int executeCountLimit;
    
    static {
        Properties props = new Properties();
        props.setProperty("max-vibration-offset", "2");
        KEY_GENERATE_ALGORITHM.init(props);
    }
    
    @Override
    public void run() {
        int executeCount = 0;
        while (executeCount < executeCountLimit && !Thread.currentThread().isInterrupted()) {
            Object orderId = insertOrder();
            if (executeCount % 2 == 0) {
                jdbcTemplate.update(prefixSchema("DELETE FROM ${schema}t_order_copy WHERE order_id = ?", schema), orderId);
            } else {
                updateOrderByPrimaryKey(orderId);
            }
            if (incrementOrderItemTogether) {
                Object orderItemPrimaryKey = insertOrderItem();
                jdbcTemplate.update(prefixSchema("UPDATE ${schema}t_order_item SET status = ? WHERE item_id = ?", schema), "updated" + Instant.now().getEpochSecond(), orderItemPrimaryKey);
                jdbcTemplate.update(prefixSchema("DELETE FROM ${schema}t_order_item WHERE item_id = ?", schema), orderItemPrimaryKey);
            }
            executeCount++;
        }
        log.info("PostgreSQL increment task runnable execute successfully.");
    }
    
    private Object insertOrder() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String status = random.nextInt() % 2 == 0 ? null : "NOT-NULL";
        Object[] orderInsertDate = new Object[]{KEY_GENERATE_ALGORITHM.generateKey(), random.nextInt(0, 6), status};
        jdbcTemplate.update(prefixSchema("INSERT INTO ${schema}t_order_copy (order_id,user_id,status) VALUES (?, ?, ?)", schema), orderInsertDate);
        return orderInsertDate[0];
    }
    
    private Object insertOrderItem() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String status = random.nextInt() % 2 == 0 ? null : "NOT-NULL";
        Object[] orderInsertItemDate = new Object[]{KEY_GENERATE_ALGORITHM.generateKey(), ScalingCaseHelper.generateSnowflakeKey(), random.nextInt(0, 6), status};
        jdbcTemplate.update(prefixSchema("INSERT INTO ${schema}t_order_item(item_id,order_id,user_id,status) VALUES(?,?,?,?)", schema), orderInsertItemDate);
        return orderInsertItemDate[0];
    }
    
    private void updateOrderByPrimaryKey(final Object primaryKey) {
        Object[] updateData = {"updated" + Instant.now().getEpochSecond(), primaryKey};
        jdbcTemplate.update(prefixSchema("UPDATE ${schema}t_order_copy SET status = ? WHERE order_id = ?", schema), updateData);
    }
    
    private String prefixSchema(final String sql, final String schema) {
        if (StringUtils.isNotBlank(schema)) {
            return sql.replace("${schema}", schema + ".");
        } else {
            return sql.replace("${schema}", "");
        }
    }
}
