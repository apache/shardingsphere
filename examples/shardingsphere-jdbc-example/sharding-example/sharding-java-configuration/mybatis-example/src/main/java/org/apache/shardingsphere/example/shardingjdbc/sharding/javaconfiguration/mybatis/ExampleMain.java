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
        orderRepository.createTableIfNotExists();
        orderRepository.truncateTable();
    }

    public static void insertData() throws SQLException {
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setAddressId(new Random().nextInt(200));
            order.setUserId(i);
            order.setStatus("INIT STATUS");
            orderRepository.insert(order);
        }
    }

    public static void selectData() throws SQLException {
        List<Order> orderList = orderRepository.selectAll();
        for (Order order : orderList) {
            System.out.println(order);
        }
    }

    public static void deleteData() throws SQLException {
        List<Order> orderList = orderRepository.selectAll();
        for (Order order : orderList) {
            orderRepository.delete(order.getOrderId());
        }
    }
}
