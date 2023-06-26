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

package org.apache.shardingsphere.test.e2e.agent.jdbc.project;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.controller.OrderController;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.service.OrderService;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.service.impl.OrderServiceImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.TimeUnit;

/**
 * Jdbc project application.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcProjectApplication {
    
    /**
     * Main.
     *
     * @param args args
     * @throws Exception Exception
     */
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws Exception {
        run();
    }
    // CHECKSTYLE:ON
    
    private static void run() throws Exception {
        Connection connection = getConnection();
        OrderService orderService = new OrderServiceImpl(connection);
        OrderController orderController = new OrderController(orderService);
        long endTime = System.currentTimeMillis() + (5 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            orderController.dropTable();
            orderController.createTable();
            orderController.insert();
            orderController.selectAll();
            orderController.update();
            orderController.createErrorRequest();
            orderController.delete();
            orderController.dropTable();
            TimeUnit.MILLISECONDS.sleep(1000L);
        }
    }
    
    private static Connection getConnection() throws Exception {
        Class.forName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        String url = "jdbc:shardingsphere:classpath:config.yaml";
        return DriverManager.getConnection(url, "root", "root");
    }
}
