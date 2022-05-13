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

package org.apache.shardingsphere.integration.data.pipeline.cases.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@AllArgsConstructor
public abstract class BaseTaskRunnable implements Runnable {
    
    private final JdbcTemplate jdbcTemplate;
    
    private final ExtraSQLCommand extraSQLCommand;
    
    private final KeyGenerateAlgorithm keyGenerateAlgorithm;
    
    protected abstract Object[] getOrderInsertDate();
    
    protected abstract Object[] getOrderInsertItemDate();
    
    /**
     * Insert order.
     *
     * @return primary key of insert data
     */
    public Object insertOrder() {
        Object[] orderInsertDate = getOrderInsertDate();
        jdbcTemplate.update(extraSQLCommand.getInsertOrder(), orderInsertDate);
        return orderInsertDate[0];
    }
    
    /**
     * Insert order item.
     *
     * @return primary key of insert data
     */
    public Object insertOrderItem() {
        Object[] orderInsertItemDate = getOrderInsertItemDate();
        jdbcTemplate.update(extraSQLCommand.getInsertOrderItem(), orderInsertItemDate);
        return orderInsertItemDate[0];
    }
    
    /**
     * Update order by primary key.
     *
     * @param primaryKey primary key
     */
    public void updateOrderByPrimaryKey(final Object primaryKey) {
        jdbcTemplate.update(extraSQLCommand.getUpdateOrderById(), "updated" + Instant.now().getEpochSecond(), null, primaryKey);
        jdbcTemplate.update(extraSQLCommand.getUpdateOrderById(), "updated" + Instant.now().getEpochSecond(), ThreadLocalRandom.current().nextInt(0, 100), primaryKey);
    }
    
    /**
     * Update order item by primary key.
     *
     * @param primaryKey primary key
     */
    public void updateOrderItemByPrimaryKey(final Object primaryKey) {
        jdbcTemplate.update(extraSQLCommand.getUpdateOrderItemById(), "updated" + Instant.now().getEpochSecond(), primaryKey);
    }
    
    /**
     * Delete order by primary key.
     *
     * @param primaryKey primary key
     */
    public void deleteOrderByPrimaryKey(final Object primaryKey) {
        jdbcTemplate.update(extraSQLCommand.getDeleteOrderById(), primaryKey);
    }
    
    /**
     * Delete order by primary key.
     *
     * @param primaryKey primary key
     */
    public void deleteOrderItemByPrimaryKey(final Object primaryKey) {
        jdbcTemplate.update(extraSQLCommand.getDeleteOrderItemById(), primaryKey);
    }
}
