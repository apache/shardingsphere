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

import io.shardingsphere.example.repository.jpa.entity.JPAOrder;
import io.shardingsphere.example.repository.jpa.entity.JPAOrderItem;
import io.shardingsphere.example.repository.jpa.repository.OrderItemRepository;
import io.shardingsphere.example.repository.jpa.repository.OrderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DemoService {
    
    @Resource
    private OrderRepository orderRepository;
    
    @Resource
    private OrderItemRepository orderItemRepository;
    
    public void demo() {
        List<Long> orderIds = new ArrayList<>(10);
        System.out.println("1.Insert--------------");
        for (int i = 0; i < 10; i++) {
            JPAOrder JPAOrder = new JPAOrder();
            JPAOrder.setUserId(51);
            JPAOrder.setStatus("INSERT_TEST");
            orderRepository.insert(JPAOrder);
            long orderId = JPAOrder.getOrderId();
            orderIds.add(orderId);
            
            JPAOrderItem item = new JPAOrderItem();
            item.setOrderId(orderId);
            item.setUserId(51);
            item.setStatus("INSERT_TEST");
            orderItemRepository.insert(item);
        }
        System.out.println("JPAOrder Data--------------");
        System.out.println(orderRepository.selectAll());
        System.out.println("JPAOrderItem Data--------------");
        System.out.println(orderItemRepository.selectAll());
        System.out.println("2.Delete--------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
        }
        System.out.println("JPAOrder Data--------------");
        System.out.println(orderRepository.selectAll());
        System.out.println("JPAOrderItem Data--------------");
        System.out.println(orderItemRepository.selectAll());
        orderItemRepository.dropTable();
        orderRepository.dropTable();
    }
}
