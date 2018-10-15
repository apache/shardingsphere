/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.repository.jpa.service;

import io.shardingsphere.example.repository.api.service.CommonService;
import io.shardingsphere.example.repository.jpa.entity.OrderEntity;
import io.shardingsphere.example.repository.jpa.entity.OrderItemEntity;
import io.shardingsphere.example.repository.api.repository.OrderItemRepository;
import io.shardingsphere.example.repository.api.repository.OrderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DemoService implements CommonService {
    
    @Resource
    private OrderRepository orderRepository;
    
    @Resource
    private OrderItemRepository orderItemRepository;
    
    public void demo() {
        List<Long> orderIds = insertData();
        printData();
        deleteData(orderIds);
        printData();
    }
    
    @Override
    public void initTables() {
    }
    
    @Override
    public List<Long> insertData() {
        System.out.println("1.Insert--------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            OrderEntity order = new OrderEntity();
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            OrderItemEntity item = new OrderItemEntity();
            item.setOrderId(order.getOrderId());
            item.setUserId(51);
            item.setStatus("INSERT_TEST");
            orderItemRepository.insert(item);
            result.add(order.getOrderId());
        }
        return result;
    }
    
    @Override
    public void deleteData(final List<Long> orderIds) {
        System.out.println("2.Delete--------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
        }
    }
    
    @Override
    public void printData() {
        System.out.println("Order Data--------------");
        System.out.println(orderRepository.selectAll());
        System.out.println("OrderItem Data--------------");
        System.out.println(orderItemRepository.selectAll());
    }
    
    @Override
    public void cleanTables() {
    }
}
