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

package com.dangdang.ddframe.rdb.sharding.example.jdbc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dangdang.ddframe.rdb.sharding.example.jdbc.entity.Order;
import com.dangdang.ddframe.rdb.sharding.example.jdbc.repository.OrderRepository;
// CHECKSTYLE:OFF
public final class Main {
    
    public static void main(final String[] args) {
        // CHECKSTYLE:ON
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/jpaContext.xml");
        OrderRepository orderRepository = applicationContext.getBean(OrderRepository.class);
        System.out.println(orderRepository.selectById(1000));
        System.out.println("--------------");
    
        System.out.println(orderRepository.selectAll());
        System.out.println("--------------");
    
        System.out.println(orderRepository.selectOrderBy());
        System.out.println("--------------");
    
        for (int i = 10000; i < 10010; i++) {
            Order order = new Order();
            order.setOrderId(i);
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            orderRepository.create(order);
            System.out.println(orderRepository.selectById(i));
            System.out.println("--------------");
            order.setStatus("UPDATE_TEST");
            orderRepository.update(order);
            System.out.println(orderRepository.selectById(i));
            System.out.println("--------------");
        }
    
        System.out.println(orderRepository.selectAll());
        System.out.println("--------------");
    
        System.out.println(orderRepository.selectOrderBy());
        System.out.println("--------------");
    
        for (int i = 10000; i < 10010; i++) {
            orderRepository.delete(i);
        }
        applicationContext.close();
    }
}
