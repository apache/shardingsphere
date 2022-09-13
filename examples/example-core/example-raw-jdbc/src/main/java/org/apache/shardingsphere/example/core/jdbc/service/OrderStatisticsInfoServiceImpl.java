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

package org.apache.shardingsphere.example.core.jdbc.service;

import org.apache.shardingsphere.example.core.api.entity.OrderStatisticsInfo;
import org.apache.shardingsphere.example.core.api.repository.OrderStatisticsInfoRepository;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.example.core.jdbc.repository.OrderStatisticsInfoRepositoryImpl;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

public final class OrderStatisticsInfoServiceImpl implements ExampleService {
    
    private final OrderStatisticsInfoRepository orderStatisticsInfoRepository;
    
    public OrderStatisticsInfoServiceImpl(final DataSource dataSource) {
        orderStatisticsInfoRepository = new OrderStatisticsInfoRepositoryImpl(dataSource);
    }
    
    @Override
    public void initEnvironment() throws SQLException {
        orderStatisticsInfoRepository.createTableIfNotExists();
        orderStatisticsInfoRepository.truncateTable();
    }
    
    @Override
    public void cleanEnvironment() throws SQLException {
        orderStatisticsInfoRepository.dropTable();
    }
    
    @Override
    public void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        Collection<Long> ids = insertData();
        printData();
        deleteData(ids);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }
    
    @Override
    public void processFailure() throws SQLException {
        System.out.println("-------------- Process Failure Begin ---------------");
        insertData();
        System.out.println("-------------- Process Failure Finish --------------");
        throw new RuntimeException("Exception occur for transaction test.");
    }
    
    private Collection<Long> insertData() throws SQLException {
        System.out.println("------------------- Insert Data --------------------");
        Collection<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            OrderStatisticsInfo orderStatisticsInfo = insertOrderStatisticsInfo(i);
            result.add(orderStatisticsInfo.getId());
        }
        return result;
    }
    
    private OrderStatisticsInfo insertOrderStatisticsInfo(final int i) throws SQLException {
        OrderStatisticsInfo result = new OrderStatisticsInfo();
        result.setUserId(new Long(i));
        if (0 == i % 2) {
            result.setOrderDate(LocalDate.now().plusYears(-1));
        } else {
            result.setOrderDate(LocalDate.now());
        }
        result.setOrderNum(i * 10);
        orderStatisticsInfoRepository.insert(result);
        return result;
    }
    
    private void deleteData(final Collection<Long> ids) throws SQLException {
        System.out.println("-------------------- Delete Data -------------------");
        for (Long each : ids) {
            orderStatisticsInfoRepository.delete(each);
        }
    }
    
    @Override
    public void printData() throws SQLException {
        System.out.println("---------------- Print Order Data ------------------");
        for (Object each : orderStatisticsInfoRepository.selectAll()) {
            System.out.println(each);
        }
    }
}
