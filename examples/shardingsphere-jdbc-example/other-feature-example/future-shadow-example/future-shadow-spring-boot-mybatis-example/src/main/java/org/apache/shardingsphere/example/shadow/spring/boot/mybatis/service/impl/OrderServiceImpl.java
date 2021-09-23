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

package org.apache.shardingsphere.example.shadow.spring.boot.mybatis.service.impl;

import org.apache.shardingsphere.example.shadow.spring.boot.mybatis.domain.OrderInfo;
import org.apache.shardingsphere.example.shadow.spring.boot.mybatis.repository.OrderMapper;
import org.apache.shardingsphere.example.shadow.spring.boot.mybatis.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.LinkedList;

@Service(value = "orderService")
public final class OrderServiceImpl implements OrderService {
    
    @Resource
    private OrderMapper orderMapper;
    
    @Override
    public void executeInsertCase() {
        Collection<OrderInfo> orderInfos = initShadowOrderInfos();
        orderInfos.forEach(each -> orderMapper.saveOne(each));
    }
    
    private Collection<OrderInfo> initShadowOrderInfos() {
        Collection<OrderInfo> result = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            result.add(new OrderInfo((i % 2), 1, "shadow_case"));
        }
        return result;
    }
    
    private Collection<OrderInfo> initNativeOrderInfos() {
        Collection<OrderInfo> result = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            result.add(new OrderInfo((i % 2), 2, "native_case"));
        }
        return result;
    }
}
