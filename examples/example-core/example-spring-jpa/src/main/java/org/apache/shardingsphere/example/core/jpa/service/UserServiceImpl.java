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

package org.apache.shardingsphere.example.core.jpa.service;

import org.apache.shardingsphere.example.core.api.repository.UserRepository;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.example.core.jpa.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service("encryptExample")
public class UserServiceImpl implements ExampleService {
    
    @Resource
    private UserRepository userRepository;
    
    @Override
    public void initEnvironment() {
    }
    
    @Override
    public void cleanEnvironment() {
    }
    
    @Override
    @Transactional
    public void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> userIds = insertData();
        printData();
        deleteData(userIds);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }
    
    @Override
    @Transactional
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
            UserEntity user = new UserEntity();
            user.setUserId(i);
            user.setUserName("test_jpa_" + i);
            user.setPwd("pwd_jpa_" + i);
            userRepository.insert(user);
            result.add((long) user.getUserId());
        }
        return result;
    }
    
    private void deleteData(final List<Long> userIds) throws SQLException {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : userIds) {
            userRepository.delete(each);
        }
    }
    
    @Override
    public void printData() throws SQLException {
        System.out.println("---------------------------- Print User Data -----------------------");
        for (Object each : userRepository.selectAll()) {
            System.out.println(each);
        }
    }
}
