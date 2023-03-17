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
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.entity.Order;

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
    void insertWithStatement(Order order);
    
    /**
     * Insert with prepared statement.
     *
     * @param order order
     */
    void insertWithPreparedStatement(Order order);
    
    /**
     * Delete with statement.
     *
     * @param id id
     */
    void deleteWithStatement(@Param("id") Long id);
    
    /**
     * Delete with prepared statement.
     *
     * @param id id
     */
    void deleteWithPreparedStatement(@Param("id") Long id);
    
    /**
     * Select all with statement.
     *
     * @return orders
     */
    Collection<Order> selectAllWithStatement();
    
    /**
     * Select all with prepared statement.
     *
     * @return orders
     */
    Collection<Order> selectAllWithPreparedStatement();
    
    /**
     * Update with statement.
     *
     * @param order order
     */
    void updateWithStatement(Order order);
    
    /**
     * Update with prepared statement.
     *
     * @param order order
     */
    void updateWithPreparedStatement(Order order);
}
