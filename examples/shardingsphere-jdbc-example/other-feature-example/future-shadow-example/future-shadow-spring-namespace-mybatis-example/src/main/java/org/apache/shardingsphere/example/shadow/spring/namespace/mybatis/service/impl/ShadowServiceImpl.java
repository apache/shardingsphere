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

package org.apache.shardingsphere.example.shadow.spring.namespace.mybatis.service.impl;

import org.apache.shardingsphere.example.shadow.spring.namespace.mybatis.repository.ShadowMapper;
import org.apache.shardingsphere.example.shadow.spring.namespace.mybatis.service.ShadowService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.LinkedList;

@Service(value = "shadowService")
public final class ShadowServiceImpl implements ShadowService {
    
    @Resource
    private ShadowMapper shadowMapper;
    
    private void execute(String sql) {
        shadowMapper.execute(sql);
    }
    
    @Override
    public void executeInsertCase() {
        Collection<String> insertSQLs = initInsertCase();
        for (String each : insertSQLs) {
            execute(each);
        }
    }
    
    private Collection<String> initInsertCase() {
        Collection<String> result = new LinkedList<>();
        String insert_case_1 = "INSERT INTO t_order (user_id, content) VALUES (1, 'insert_case_1')";
        result.add(insert_case_1);
        String insert_case_2 = "INSERT INTO t_order (user_id, content) VALUES (1, 'insert_case_2'), (1, 'insert_case_2'), (1, 'insert_case_2'), (1, 'insert_case_2')";
        result.add(insert_case_2);
        String insert_case_3 = "INSERT INTO t_order (user_id, content) VALUES (1, 'insert_case_3'), (2, 'insert_case_3')";
        result.add(insert_case_3);
        String insert_case_4 = "INSERT INTO t_order (user_id, content) SELECT user_id, content from t_order_data where user_id = 1";
        result.add(insert_case_4);
        return result;
    }
    
    @Override
    public void executeUpdateCase() {
        Collection<String> updateSQLs = initUpdateCase();
        for (String each : updateSQLs) {
            execute(each);
        }
    }
    
    private Collection<String> initUpdateCase() {
        Collection<String> result = new LinkedList<>();
        String update_case_1 = "UPDATE t_order SET user_id = 2, content = 'update_case_1' WHERE user_id = 1 and content = 'update_case_1'";
        result.add(update_case_1);
        String update_case_2 = "UPDATE t_order SET user_id = 2, content = 'update_case_1' WHERE user_id = 2 and content = 'update_case_1'";
        result.add(update_case_2);
        String update_case_3 = "UPDATE t_order SET user_id = 2, content = 'update_case_1' WHERE user_id = 1 or content = 'aa'";
        result.add(update_case_3);
        String update_case_4 = "UPDATE t_order SET user_id = 2, content = 'update_case_1' WHERE user_id = 2 or content = 'aa'";
        result.add(update_case_4);
        String update_case_5 = "UPDATE t_order SET user_id = 2, content = 'update_case_1' WHERE user_id in (1, 2, 3) or content BETWEEN 'aaa' AND 'bbb'";
        result.add(update_case_5);
        String update_case_6 = "UPDATE t_order SET user_id = 2, content = 'update_case_1' WHERE user_id = 2 or content = 'aa'";
        result.add(update_case_6);
        String update_case_7 = "UPDATE t_order SET user_id = 2, content = 'update_case_1' WHERE user_id BETWEEN 0 AND 2 or content = 'aa'";
        result.add(update_case_7);
        return result;
    }
}
