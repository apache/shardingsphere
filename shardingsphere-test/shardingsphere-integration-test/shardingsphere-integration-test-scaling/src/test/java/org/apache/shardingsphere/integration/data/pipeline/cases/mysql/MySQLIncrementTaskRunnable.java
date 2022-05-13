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

package org.apache.shardingsphere.integration.data.pipeline.cases.mysql;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.data.pipeline.cases.base.BaseTaskRunnable;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public final class MySQLIncrementTaskRunnable extends BaseTaskRunnable {
    
    public MySQLIncrementTaskRunnable(final JdbcTemplate jdbcTemplate, final ExtraSQLCommand extraSQLCommand, final KeyGenerateAlgorithm keyGenerateAlgorithm) {
        super(jdbcTemplate, extraSQLCommand, keyGenerateAlgorithm);
    }
    
    @Override
    public void run() {
        int executeCount = 0;
        List<Object> newOrderPrimaryKeys = new LinkedList<>();
        List<Object> newOrderItemPrimaryKeys = new LinkedList<>();
        while (executeCount < 20 && !Thread.currentThread().isInterrupted()) {
            newOrderPrimaryKeys.add(insertOrder());
            newOrderItemPrimaryKeys.add(insertOrderItem());
            if (newOrderPrimaryKeys.size() % 2 == 0) {
                deleteOrderByPrimaryKey(newOrderPrimaryKeys.get(newOrderPrimaryKeys.size() - 1));
                deleteOrderItemByPrimaryKey(newOrderItemPrimaryKeys.get(newOrderItemPrimaryKeys.size() - 1));
            } else {
                updateOrderByPrimaryKey(newOrderPrimaryKeys.get(newOrderPrimaryKeys.size() - 1));
                updateOrderItemByPrimaryKey(newOrderItemPrimaryKeys.get(newOrderItemPrimaryKeys.size() - 1));
            }
            executeCount++;
            log.info("MySQL increment task runnable execute successfully.");
        }
    }
    
    @Override
    protected Object[] getOrderInsertDate() {
        return new Object[]{getKeyGenerateAlgorithm().generateKey(), RANDOM.nextInt(0, 6), RANDOM.nextInt(0, 6)};
    }
    
    @Override
    protected Object[] getOrderInsertItemDate() {
        return new Object[]{getKeyGenerateAlgorithm().generateKey(), RANDOM.nextInt(0, 6), RANDOM.nextInt(0, 6), "OK"};
    }
}
