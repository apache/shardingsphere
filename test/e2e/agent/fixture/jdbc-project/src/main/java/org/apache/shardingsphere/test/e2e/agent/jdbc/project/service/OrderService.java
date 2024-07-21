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

package org.apache.shardingsphere.test.e2e.agent.jdbc.project.service;

import org.apache.shardingsphere.test.e2e.agent.jdbc.project.entity.OrderEntity;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.enums.StatementType;

import java.util.Collection;

/**
 * Order service.
 */
public interface OrderService {
    
    /**
     * Create table.
     */
    void createTable();
    
    /**
     * Drop table.
     */
    void dropTable();
    
    /**
     * Insert order.
     *
     * @param order order
     * @param statementType statement type
     * @param isRollback is rollback
     */
    void insert(OrderEntity order, StatementType statementType, boolean isRollback);
    
    /**
     * Delete.
     *
     * @param orderId order id
     * @param statementType statement type
     */
    void delete(Long orderId, StatementType statementType);
    
    /**
     * Update.
     *
     * @param order order
     * @param statementType statement type
     */
    void update(OrderEntity order, StatementType statementType);
    
    /**
     * Select all.
     *
     * @param statementType statement type
     * @return orders
     */
    Collection<OrderEntity> selectAll(StatementType statementType);
}
