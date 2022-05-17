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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
@Slf4j
public abstract class BaseTaskRunnable implements Runnable {
    
    private final JdbcTemplate jdbcTemplate;
    
    private final ExtraSQLCommand extraSQLCommand;
    
    private final KeyGenerateAlgorithm keyGenerateAlgorithm;
    
    protected abstract Object[] getOrderInsertData();
    
    protected abstract Object[] getOrderInsertItemData();
    
    protected abstract Object[] getOrderUpdateData(Object primaryKey);
    
    /**
     * Insert order.
     *
     * @return primary key of insert data
     */
    public Object insertOrder() {
        Object[] orderInsertDate = getOrderInsertData();
        jdbcTemplate.update(extraSQLCommand.getInsertOrder(), orderInsertDate);
        return orderInsertDate[0];
    }
    
    /**
     * Insert order item.
     *
     * @return primary key of insert data
     */
    public Object insertOrderItem() {
        Object[] orderInsertItemDate = getOrderInsertItemData();
        jdbcTemplate.update(extraSQLCommand.getInsertOrderItem(), orderInsertItemDate);
        return orderInsertItemDate[0];
    }
    
    /**
     * Update order by primary key.
     *
     * @param primaryKey primary key
     */
    public void updateOrderByPrimaryKey(final Object primaryKey) {
        jdbcTemplate.update(extraSQLCommand.getUpdateOrderById(), getOrderUpdateData(primaryKey));
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
