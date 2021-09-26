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

package org.apache.shardingsphere.example.shadow.spring.namespace.mybatis.service.impl;

import org.apache.shardingsphere.example.shadow.spring.namespace.mybatis.domain.OrderInfo;
import org.apache.shardingsphere.example.shadow.spring.namespace.mybatis.repository.OrderMapper;
import org.apache.shardingsphere.example.shadow.spring.namespace.mybatis.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service(value = "orderService")
public final class OrderServiceImpl implements OrderService {
    
    @Resource
    private OrderMapper orderMapper;
    
    @Override
    public void executeInsertCase() {
        executeOneInsertCase();
        executeBatchInsertCase();
    }
    
    private void executeBatchInsertCase() {
        List<OrderInfo> orders = new ArrayList<>();
        OrderInfo orderInfo1 = new OrderInfo();
        orderInfo1.setUserId(1);
        orderInfo1.setContent("insert_case_2");
        orders.add(orderInfo1);
        OrderInfo orderInfo2 = new OrderInfo();
        orderInfo2.setUserId(2);
        orderInfo2.setContent("insert_case_2");
        orders.add(orderInfo2);
        OrderInfo orderInfo3 = new OrderInfo();
        orderInfo3.setUserId(1);
        orderInfo3.setContent("insert_case_2");
        orders.add(orderInfo3);
        orderMapper.saveBatch(orders);
    }
    
    private void executeOneInsertCase() {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(1);
        orderInfo.setContent("insert_case_1");
        orderMapper.saveOne(orderInfo);
    }
    
    @Override
    public void executeUpdateCase() {
        executeOneUpdateCase();
        executeInUpdateCase();
    }
    
    private void executeInUpdateCase() {
        Map<String, Object> updateMap = new LinkedHashMap<>();
        List<Integer> userIds = new LinkedList<>();
        userIds.add(1);
        userIds.add(2);
        updateMap.put("userIds",userIds);
        updateMap.put("content","update_case_2");
        orderMapper.updateByUserIds(updateMap);
        
    }
    
    private void executeOneUpdateCase() {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(1);
        orderInfo.setContent("update_case_1");
        orderMapper.updateOne(orderInfo);
    }
    
    @Override
    public void executeDeleteCase() {
        executeRemoveOneCase();
        executeRemoveInCase();
    }
    
    private void executeRemoveInCase() {
        List<Integer> userIds = new LinkedList<>();
        userIds.add(1);
        userIds.add(1);
        userIds.add(1);
        orderMapper.removeInUserIds(userIds);
    }
    
    private void executeRemoveOneCase() {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(1);
        orderMapper.remove(orderInfo);
    }
}
