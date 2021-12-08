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

package org.apache.shardingsphere.example.shadow.spring.namespace.mybatis;

import org.apache.shardingsphere.example.shadow.spring.namespace.mybatis.entity.User;
import org.apache.shardingsphere.example.shadow.spring.namespace.mybatis.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public final class MemoryLocalShadowSpringNamespaceMybatisExampleService {

    @Resource
    private UserRepository userRepository;

    /**
     * Execute test.
     *
     * @throws SQLException
     */
    public void run() {
        try {
            this.initEnvironment();
            this.processSuccess();
        } finally {
            this.cleanEnvironment();
        }
    }
    
    /**
     * Initialize the database test environment.
     * @throws SQLException
     */
    private void initEnvironment() {
        userRepository.createTableIfNotExists();
        userRepository.truncateTable();
    }
    
    private void processSuccess() {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> orderIds = insertData();
        printData(); 
        deleteData(orderIds);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }

    private List<Long> insertData() {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            User user = new User();
            user.setUserId(i);
            user.setUserName("test_mybatis_" + i);
            user.setPwd("pwd_mybatis_" + i);
            userRepository.insert(user);
            result.add((long) user.getUserId());
        }
        return result;
    }

    private void deleteData(final List<Long> orderIds) {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : orderIds) {
            userRepository.delete(each);
        }
    }
    
    private void printData() {
        System.out.println("---------------------------- Print Order Data -----------------------");
        for (Object each : userRepository.selectAll()) {
            System.out.println(each);
        }
    }
    
    /**
     * Restore the environment.
     * @throws SQLException
     */
    private void cleanEnvironment() {
        userRepository.dropTable();
    }

}
