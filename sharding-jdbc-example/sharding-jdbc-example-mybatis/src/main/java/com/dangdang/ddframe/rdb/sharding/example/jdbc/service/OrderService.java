/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.example.jdbc.service;

import javax.annotation.Resource;

import com.dangdang.ddframe.rdb.sharding.example.jdbc.entity.Order;
import com.dangdang.ddframe.rdb.sharding.example.jdbc.repository.OrderRepository;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order 服务对象.
 * 
 * @author gaohongtao
 */
@Service
@Transactional
public class OrderService {
    
    @Resource
    private OrderRepository orderRepository;
    
    @Transactional(readOnly = true)
    public void select() {
        System.out.println(orderRepository.selectAll());
    }
    
    public void clear() {
        orderRepository.deleteAll();
    }
    
    public void fooService() {
        Order criteria = new Order();
        criteria.setUserId(10);
        criteria.setOrderId(1);
        criteria.setStatus("INSERT");
        orderRepository.insert(criteria);
        criteria.setUserId(11);
        criteria.setOrderId(1);
        criteria.setStatus("INSERT2");
        orderRepository.insert(criteria);
        orderRepository.update(Lists.newArrayList(10, 11));
    }
    
    public void fooServiceWithFailure() {
        fooService();
        throw new IllegalArgumentException("failed");
    }
}
