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
        initInsertCase().forEach(this::execute);
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
        initUpdateCase().forEach(this::execute);
    }
    
    private Collection<String> initUpdateCase() {
        Collection<String> result = new LinkedList<>();
        String update_case_1 = "UPDATE t_order SET user_id = 2, content = 'update_case_1' WHERE user_id = 1 and content = 'update_case_1'";
        result.add(update_case_1);
        String update_case_2 = "UPDATE t_order SET user_id = 2, content = 'update_case_2' WHERE user_id = 2 and content = 'update_case_1'";
        result.add(update_case_2);
        String update_case_3 = "UPDATE t_order SET user_id = 2, content = 'update_case_3' WHERE user_id in (1, 1) or content = 'aa'";
        result.add(update_case_3);
        String update_case_4 = "UPDATE t_order SET user_id = 2, content = 'update_case_4' WHERE user_id in (1, 2, 3) or content BETWEEN 'aaa' AND 'bbb'";
        result.add(update_case_4);
        String update_case_5 = "UPDATE t_order SET user_id = 2, content = 'update_case_5' WHERE user_id like '1'";
        result.add(update_case_5);
        String update_case_6 = "UPDATE t_order SET user_id = 2, content = 'update_case_6' WHERE user_id not like '1'";
        result.add(update_case_6);
        String update_case_7 = "UPDATE t_order SET user_id = 2, content = 'update_case_7' WHERE user_id = 1 or user_id in (2, 3) ";
        result.add(update_case_7);
        String update_case_8 = "UPDATE t_order SET user_id = 2, content = 'update_case_8' WHERE user_id = 2 or user_id in (1, 1) ";
        result.add(update_case_8);
        String update_case_9 = "UPDATE t_order SET user_id = 2, content = 'update_case_9' WHERE user_id in (select user_id from t_order_data where user_id = 1)";
        result.add(update_case_9);
        String update_case_10 = "UPDATE t_order SET user_id = 2, content = 'update_case_10' WHERE user_id BETWEEN 0 AND 2 or content = 'aa'";
        result.add(update_case_10);
        return result;
    }
    
    @Override
    public void executeDeleteCase() {
        initDeleteCase().forEach(this::execute);
    }
    
    private Collection<String> initDeleteCase() {
        Collection<String> result = new LinkedList<>();
        String delete_case_1 = "DELETE FROM t_order WHERE user_id = 1 and content = 'delete_case_1'";
        result.add(delete_case_1);
        String delete_case_2 = "DELETE FROM t_order WHERE user_id = 2 and content = 'delete_case_2'";
        result.add(delete_case_2);
        String delete_case_3 = "DELETE FROM t_order WHERE user_id in (1, 1) or content = 'aa'";
        result.add(delete_case_3);
        String delete_case_4 = "DELETE FROM t_order WHERE user_id in (1, 2, 3) or content BETWEEN 'aaa' AND 'bbb'";
        result.add(delete_case_4);
        String delete_case_5 = "DELETE FROM t_order WHERE user_id like '1'";
        result.add(delete_case_5);
        String delete_case_6 = "DELETE FROM t_order WHERE user_id not like '1'";
        result.add(delete_case_6);
        String delete_case_7 = "DELETE FROM t_order WHERE user_id = 1 or user_id in (2, 3) ";
        result.add(delete_case_7);
        String delete_case_8 = "DELETE FROM t_order WHERE user_id = 2 or user_id in (1, 1) ";
        result.add(delete_case_8);
        String delete_case_9 = "DELETE FROM t_order WHERE user_id in (select user_id from t_order_data where user_id = 1)";
        result.add(delete_case_9);
        String delete_case_10 = "DELETE FROM t_order WHERE user_id BETWEEN 0 AND 2 or content = 'aa'";
        result.add(delete_case_10);
        return result;
    }
}
