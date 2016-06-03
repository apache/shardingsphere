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

package com.dangdang.ddframe.rdb.sharding.example.config.spring.masterslave;

import com.dangdang.ddframe.rdb.sharding.example.config.spring.masterslave.service.OrderService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.SQLException;

public final class SpringNamespaceWithMasterSlaveMain {
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws SQLException {
    // CHECKSTYLE:ON
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/applicationContextWithMasterSlave.xml");
        OrderService orderService =  applicationContext.getBean(OrderService.class);
        orderService.insert();
        orderService.select();
        orderService.delete();
        orderService.select();
    }
}
