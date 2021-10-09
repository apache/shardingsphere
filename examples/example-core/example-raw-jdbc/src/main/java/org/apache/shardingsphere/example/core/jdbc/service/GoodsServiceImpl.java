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

import org.apache.shardingsphere.example.core.api.entity.Goods;
import org.apache.shardingsphere.example.core.api.repository.GoodsRepository;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.example.core.jdbc.repository.GoodsRepositoryImpl;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class GoodsServiceImpl implements ExampleService {
    
    private final GoodsRepository goodsRepository;
    
    public GoodsServiceImpl(final DataSource dataSource) {
        goodsRepository = new GoodsRepositoryImpl(dataSource);
    }
    
    public GoodsServiceImpl(final GoodsRepository goodsRepository) {
        this.goodsRepository = goodsRepository;
    }
    
    @Override
    public void initEnvironment() throws SQLException {
        goodsRepository.createTableIfNotExists();
        goodsRepository.truncateTable();
    }
    
    @Override
    public void cleanEnvironment() throws SQLException {
        goodsRepository.dropTable();
    }
    
    @Override
    public void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> goodsIds = insertData();
        printData();
        deleteData(goodsIds);
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
    
    private List<Long> insertData() throws SQLException {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Goods goods = insertGoods(i);
            result.add(goods.getGoodsId());
        }
        return result;
    }
    
    private Goods insertGoods(final int i) throws SQLException {
        Goods goods = new Goods();
        goods.setUserId(i);
        goods.setStatus("INSERT_TEST");
        goodsRepository.insert(goods);
        return goods;
    }
    
    private void deleteData(final List<Long> goodsIds) throws SQLException {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : goodsIds) {
            goodsRepository.delete(each);
        }
    }
    
    @Override
    public void printData() throws SQLException {
        System.out.println("---------------------------- Print Goods Data -----------------------");
        for (Object each : goodsRepository.selectAll()) {
            System.out.println(each);
        }
    }
}
