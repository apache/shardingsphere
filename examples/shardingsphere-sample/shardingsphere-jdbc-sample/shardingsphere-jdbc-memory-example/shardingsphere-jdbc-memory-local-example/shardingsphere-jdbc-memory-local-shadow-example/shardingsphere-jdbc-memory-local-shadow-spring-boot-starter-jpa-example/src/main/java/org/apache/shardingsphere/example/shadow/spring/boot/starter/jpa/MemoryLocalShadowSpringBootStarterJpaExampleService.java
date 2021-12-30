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

package org.apache.shardingsphere.example.shadow.spring.boot.starter.jpa;

import org.apache.shardingsphere.example.shadow.spring.boot.starter.jpa.entity.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public final class MemoryLocalShadowSpringBootStarterJpaExampleService {

    @Resource
    private MemoryLocalShadowSpringBootStarterJpaRepository repository;

    /**
     * Execute test.
     */
    public void run() {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Integer> usersIds = insertData();
        printData(); 
        deleteData(usersIds);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }

    private List<Integer> insertData() {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Integer> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            User user = new User();
            user.setUserName("test_" + i);
            user.setPwd("pwd" + i);
            repository.insertUser(user);
            result.add(user.getUserId());
        }
        return result;
    }

    private void deleteData(final List<Integer> userIds) {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Integer each : userIds) {
            repository.deleteUser(each);
        }
    }
    
    private void printData() {
        System.out.println("---------------------------- Print User Data -----------------------");
        for (Object each : repository.selectAllUsers()) {
            System.out.println(each);
        }
    }
}
