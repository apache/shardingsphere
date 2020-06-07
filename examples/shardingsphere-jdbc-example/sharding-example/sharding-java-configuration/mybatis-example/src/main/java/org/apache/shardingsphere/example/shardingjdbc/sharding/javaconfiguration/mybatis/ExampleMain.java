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

package org.apache.shardingsphere.example.shardingjdbc.sharding.javaconfiguration.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.shardingsphere.example.shardingjdbc.sharding.javaconfiguration.mybatis.entity.Order;
import org.apache.shardingsphere.example.shardingjdbc.sharding.javaconfiguration.mybatis.repository.OrderRepository;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class ExampleMain {

    private static final int DATA_COUNT = 10;

    private static OrderRepository orderRepository;

    public static void main(String... args) throws IOException, SQLException {
        Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        orderRepository = sqlSession.getMapper(OrderRepository.class);

        // init the environment
        initEnvironment();

        // insert demo data
        insertData();

        // select all data and then print out
        selectData();

        // delete the data created
        deleteData();
    }

    public static void initEnvironment() throws SQLException {
        System.out.println("---------------- Environment Initialization Start ----------------");
        orderRepository.createTableIfNotExists();
        orderRepository.truncateTable();
        System.out.println("---------------- Environment Initialization Stop ----------------");
        System.out.println("\n");
    }

    public static void insertData() throws SQLException {
        System.out.println("---------------- Data Insertion Start ----------------");
        for (int i = 0; i < DATA_COUNT; i++) {
            Order order = new Order();
            order.setAddressId(new Random().nextInt(200));
            order.setUserId(i);
            order.setStatus("INIT STATUS");
            orderRepository.insert(order);
        }
        System.out.println("---------------- Data Insertion Stop ----------------");
        System.out.println("\n");
    }

    public static void selectData() throws SQLException {
        System.out.println("---------------- Data Selection Start ----------------");
        System.out.println("Followings are the Data Created Just Now : ");
        List<Order> orderList = orderRepository.selectAll();
        for (Order each : orderList) {
            System.out.println(each);
        }
        System.out.println("---------------- Data Selection Stop ----------------");
        System.out.println("\n");
    }

    public static void deleteData() throws SQLException {
        System.out.println("---------------- Data Deletion Start ----------------");
        List<Order> orderList = orderRepository.selectAll();
        for (Order each : orderList) {
            orderRepository.delete(each.getOrderId());
        }
        System.out.println("---------------- Data Deletion Stop ----------------");
        System.out.println("\n");
        System.out.println("The Order Data Remains : " + orderRepository.selectAll().size());
    }
}
