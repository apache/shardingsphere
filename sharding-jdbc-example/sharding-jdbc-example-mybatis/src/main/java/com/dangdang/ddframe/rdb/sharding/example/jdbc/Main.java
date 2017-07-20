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

import com.dangdang.ddframe.rdb.sharding.example.jdbc.service.OrderService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// CHECKSTYLE:OFF
@Service
@Transactional
public class Main {
    public static void main(final String[] args) {
        // CHECKSTYLE:ON
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/mybatis/mysql/mybatisContext.xml");
        OrderService orderService = applicationContext.getBean(OrderService.class);
        orderService.clear();
        orderService.fooService();
        orderService.select();
        //[order_id: , user_id: 10, status: UPDATED, order_id: , user_id: 11, status: UPDATED]
        orderService.clear();
        try {
            orderService.fooServiceWithFailure();
        } catch (final IllegalArgumentException e) {
            System.out.println("roll back");
        }
        //[]
        orderService.select();
    }
}
