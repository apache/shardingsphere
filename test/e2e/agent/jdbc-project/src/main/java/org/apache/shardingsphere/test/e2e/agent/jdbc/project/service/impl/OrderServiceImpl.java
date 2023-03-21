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

package org.apache.shardingsphere.test.e2e.agent.jdbc.project.service.impl;

import org.apache.shardingsphere.test.e2e.agent.jdbc.project.entity.OrderEntity;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.enums.StatementType;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.mapper.OrderMapper;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * Order service impl.
 */
@Service
public class OrderServiceImpl implements OrderService {
    
    @Resource
    private OrderMapper orderMapper;
    
    @Override
    public void createTable() {
        orderMapper.createTable();
    }
    
    @Override
    public void dropTable() {
        orderMapper.dropTable();
    }
    
    @Override
    public void insert(final OrderEntity order, final StatementType statementType) {
        switch (statementType) {
            case STATEMENT:
                orderMapper.insertWithStatement(order);
                break;
            case PREPARED:
                orderMapper.insertWithPreparedStatement(order);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation");
        }
    }
    
    @Override
    public void delete(final Long id, final StatementType statementType) {
        switch (statementType) {
            case STATEMENT:
                orderMapper.deleteWithStatement(id);
                break;
            case PREPARED:
                orderMapper.deleteWithPreparedStatement(id);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation");
        }
    }
    
    @Override
    public void update(final OrderEntity order, final StatementType statementType) {
        switch (statementType) {
            case STATEMENT:
                orderMapper.updateWithStatement(order);
                break;
            case PREPARED:
                orderMapper.updateWithPreparedStatement(order);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation");
        }
    }
    
    @Override
    public Collection<OrderEntity> selectAll(final StatementType statementType) {
        switch (statementType) {
            case STATEMENT:
                return orderMapper.selectAllWithStatement();
            case PREPARED:
                return orderMapper.selectAllWithPreparedStatement();
            default:
                throw new UnsupportedOperationException("Unsupported operation");
        }
    }
}
