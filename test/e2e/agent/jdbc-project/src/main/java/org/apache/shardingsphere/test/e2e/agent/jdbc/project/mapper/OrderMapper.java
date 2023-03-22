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

package org.apache.shardingsphere.test.e2e.agent.jdbc.project.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.entity.OrderEntity;

import java.util.Collection;

/**
 * Order mapper.
 */
@Mapper
public interface OrderMapper {
    
    /**
     * Create table.
     */
    void createTable();
    
    /**
     * Drop table.
     */
    void dropTable();
    
    /**
     * Insert with statement.
     *
     * @param order order
     */
    void insertWithStatement(OrderEntity order);
    
    /**
     * Insert with prepared statement.
     *
     * @param order order
     */
    void insertWithPreparedStatement(OrderEntity order);
    
    /**
     * Delete with statement.
     *
     * @param orderId order id
     */
    void deleteWithStatement(@Param("orderId") Long orderId);
    
    /**
     * Delete with prepared statement.
     *
     * @param orderId order id
     */
    void deleteWithPreparedStatement(@Param("orderId") Long orderId);
    
    /**
     * Select all with statement.
     *
     * @return orders
     */
    Collection<OrderEntity> selectAllWithStatement();
    
    /**
     * Select all with prepared statement.
     *
     * @return orders
     */
    Collection<OrderEntity> selectAllWithPreparedStatement();
    
    /**
     * Update with statement.
     *
     * @param order order
     */
    void updateWithStatement(OrderEntity order);
    
    /**
     * Update with prepared statement.
     *
     * @param order order
     */
    void updateWithPreparedStatement(OrderEntity order);
}
