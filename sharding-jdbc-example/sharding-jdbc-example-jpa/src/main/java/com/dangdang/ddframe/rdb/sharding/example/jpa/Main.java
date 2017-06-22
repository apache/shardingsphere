/*
 * Copyright 1999-2015 dangdang.com.
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

package com.dangdang.ddframe.rdb.sharding.example.jpa;

import com.dangdang.ddframe.rdb.sharding.example.jpa.entity.Order;
import com.dangdang.ddframe.rdb.sharding.example.jpa.repository.OrderRepository;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

// CHECKSTYLE:OFF
public final class Main {
    
    public static void main(final String[] args) {
        // CHECKSTYLE:ON
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/jpa/mysql/jpaContext.xml");
        OrderRepository orderRepository = applicationContext.getBean(OrderRepository.class);
        List<Long> orderIds = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            orderRepository.create(order);
            orderIds.add(order.getOrderId());
            System.out.println(orderRepository.selectById(order.getOrderId()));
            System.out.println("--------------");
            order.setStatus("UPDATE_TEST");
            orderRepository.update(order);
            System.out.println(orderRepository.selectById(order.getOrderId()));
            System.out.println("--------------");
        }

        System.out.println(orderRepository.selectAll());
        System.out.println("--------------");

        System.out.println(orderRepository.selectOrderBy());
        System.out.println("--------------");

        for (Long each : orderIds) {
            orderRepository.delete(each);
        }
        applicationContext.close();
    }
}
