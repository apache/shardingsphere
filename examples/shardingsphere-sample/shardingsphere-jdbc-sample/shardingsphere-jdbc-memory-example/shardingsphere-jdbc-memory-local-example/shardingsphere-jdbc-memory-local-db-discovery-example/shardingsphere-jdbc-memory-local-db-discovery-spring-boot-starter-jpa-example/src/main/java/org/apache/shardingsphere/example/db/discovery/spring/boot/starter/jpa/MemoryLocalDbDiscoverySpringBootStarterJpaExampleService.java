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

package org.apache.shardingsphere.example.db.discovery.spring.boot.starter.jpa;

import org.apache.shardingsphere.example.db.discovery.spring.boot.starter.jpa.entity.Order;
import org.apache.shardingsphere.example.db.discovery.spring.boot.starter.jpa.entity.OrderItem;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public final class MemoryLocalDbDiscoverySpringBootStarterJpaExampleService {
    
    @Resource
    private MemoryLocalDbDiscoverySpringBootStarterJpaRepository repository;

    /**
     * Execute test.
     */
    public void run() {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> orderIds = insertData();
        printData(); 
        deleteData(orderIds);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }

    private List<Long> insertData() {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            repository.insertOrder(order);
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setUserId(i);
            orderItem.setStatus("INSERT_TEST");
            repository.insertOrderItem(orderItem);
            result.add(order.getOrderId());
        }
        return result;
    }

    private void deleteData(final List<Long> orderIds) {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : orderIds) {
            repository.deleteOrder(each);
            repository.deleteOrderItem(each);
        }
    }
    
    private void printData() {
        System.out.println("---------------------------- Print Order Data -----------------------");
        for (Object each : repository.selectAllOrder()) {
            System.out.println(each);
        }
        System.out.println("---------------------------- Print OrderItem Data -------------------");
        for (Object each : repository.selectAllOrderItem()) {
            System.out.println(each);
        }
    }
}
