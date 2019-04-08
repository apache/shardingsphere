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

package org.apache.shardingsphere.example.common.mybatis.service;

import org.apache.shardingsphere.example.common.entity.Country;
import org.apache.shardingsphere.example.common.entity.Order;
import org.apache.shardingsphere.example.common.entity.OrderItem;
import org.apache.shardingsphere.example.common.repository.CountryRepository;
import org.apache.shardingsphere.example.common.repository.OrderItemRepository;
import org.apache.shardingsphere.example.common.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SpringPojoServiceImpl implements SpringPojoService {
    
    @Resource
    private OrderRepository orderRepository;
    
    @Resource
    private OrderItemRepository orderItemRepository;

    @Resource
    private CountryRepository countryRepository;
    
    @Override
    public void initEnvironment() {
        orderRepository.createTableIfNotExists();
        orderItemRepository.createTableIfNotExists();
        countryRepository.createTableIfNotExists();
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
        countryRepository.truncateTable();
    }

    @Override
    public void cleanEnvironment() {
        orderRepository.dropTable();
        orderItemRepository.dropTable();
        countryRepository.dropTable();
    }
    
    @Override
    @Transactional
    public void processSuccess() {
        System.out.println("-------------- Process Success Begin ---------------");
        InsertResult insertResult = insertData();
        printData();
        deleteData(insertResult.getOrderIds(), insertResult.getCountryIds());
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }
    
    @Override
    @Transactional
    public void processFailure() {
        System.out.println("-------------- Process Failure Begin ---------------");
        insertData();
        System.out.println("-------------- Process Failure Finish --------------");
        throw new RuntimeException("Exception occur for transaction test.");
    }

    private InsertResult insertData() {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> orderIds = insertOrderData();
        List<Long> countryIds = insertCountryData();
        return new InsertResult(orderIds, countryIds);
    }
    
    private List<Long> insertOrderData() {
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            OrderItem item = new OrderItem();
            item.setOrderId(order.getOrderId());
            item.setUserId(i);
            item.setStatus("INSERT_TEST");
            orderItemRepository.insert(item);
            result.add(order.getOrderId());
        }
        return result;
    }

    private List<Long> insertCountryData() {
        List<Long> result = new ArrayList<>();
        Locale[] locales = Locale.getAvailableLocales();
        int i = 0;
        for (Locale l:locales) {
            final String country = l.getCountry();
            if (country == null || "".equals(country)) {
                continue;
            }
            Country currCountry = new Country();
            currCountry.setName(l.getDisplayCountry(l));
            currCountry.setLanguage(l.getLanguage());
            currCountry.setCode(l.getCountry());
            countryRepository.insert(currCountry);
            result.add(currCountry.getId());
            if (++i == 10) {
                break;
            }
        }
        return result;
    }
    
    private void deleteData(final List<Long> orderIds, final List<Long> countryIds) {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
        }
        for (Long each: countryIds) {
            countryRepository.delete(each);
        }
    }
    
    @Override
    public void printData() {
        System.out.println("---------------------------- Print Order Data -----------------------");
        for (Object each : orderRepository.selectAll()) {
            System.out.println(each);
        }
        System.out.println("---------------------------- Print OrderItem Data -------------------");
        for (Object each : orderItemRepository.selectAll()) {
            System.out.println(each);
        }
        System.out.println("---------------------------- Print Country Data -------------------");
        for (Object each : countryRepository.selectAll()) {
            System.out.println(each);
        }
    }

    private static class InsertResult {

        private List<Long> orderIds;

        private List<Long> countryIds;

        InsertResult(final List<Long> orderIds, final List<Long> countryIds) {
            this.orderIds = orderIds;
            this.countryIds = countryIds;
        }

        public List<Long> getOrderIds() {
            return orderIds;
        }

        public List<Long> getCountryIds() {
            return countryIds;
        }
    }
}
