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

package org.apache.shardingsphere.example.shadow.spring.namespace.mybatis.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Sql generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLGenerator {
    
    /**
     * Init insert case.
     *
     * @return insert case sql
     */
    public static Collection<String> initInsertCase() {
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
    
    /**
     * Init update case.
     *
     * @return update case sql
     */
    public static Collection<String> initUpdateCase() {
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
    
    /**
     * Init delete case.
     *
     * @return delete case sql
     */
    public static Collection<String> initDeleteCase() {
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
    
    /**
     * Init select case.
     *
     * @return select case sql
     */
    public static Collection<String> initSelectCase() {
        Collection<String> result = new LinkedList<>();
        String select_case_1 = "SELECT user_id, content FROM t_order WHERE user_id = 1";
        result.add(select_case_1);
        String select_case_2 = "SELECT user_id, content FROM t_order WHERE user_id = 2";
        result.add(select_case_2);
        String select_case_3 = "SELECT user_id, content FROM t_order WHERE user_id in (1, 1)";
        result.add(select_case_3);
        String select_case_4 = "SELECT user_id, content FROM t_order WHERE user_id in (1, 2)";
        result.add(select_case_4);
        String select_case_5 = "SELECT user_id, content FROM t_order WHERE user_id BETWEEN 0 AND 2 and user_id = 1";
        result.add(select_case_5);
        String select_case_6 = "SELECT user_id, content FROM t_order WHERE user_id BETWEEN 0 AND 2";
        result.add(select_case_6);
        String select_case_7 = "SELECT tor1.user_id as user_id, tor2.user_id as new_user_id, tor1.content as content FROM t_order as tor1 left join t_order as tor2 on tor1.id = tor2.id " +
                "left join t_order as tor3 on tor1.id = tor3.id where tor1.user_id = 1 and tor2.content = 'insert_case_2'";
        result.add(select_case_7);
        String select_case_8 = "SELECT t_order.user_id as user_id, tor1.user_id as new_user_id, t_order.content as content FROM t_order left join t_order as tor1 on t_order.id = tor1.id " +
                "left join t_order as tor2 on t_order.id = tor2.id where t_order.user_id = 1 and tor1.content = 'insert_case_2'";
        result.add(select_case_8);
        String select_case_9 = "SELECT t_order.user_id as user_id, tor1.user_id as new_user_id, tor2.content as content FROM t_order left join t_order as tor1 on t_order.id = tor1.id " +
                "left join (select * from t_order_data) as tor2 on t_order.id = tor2.id where t_order.user_id = 1 and tor1.content = 'insert_case_2'";
        result.add(select_case_9);
        String select_case_10 = "SELECT tor1.user_id as user_id, tor2.user_id as new_user_id, tor1.content as content FROM t_order as tor1 left join t_order as tor2 on tor1.id = tor2.id " +
                "left join t_order_data as tor3 on tor1.id = tor3.id where tor3.user_id = 2 and tor2.content = 'insert_case_2'";
        result.add(select_case_10);
        String select_case_11 = "SELECT user_id, content FROM t_order WHERE user_id like '1'";
        result.add(select_case_11);
        String select_case_12 = "SELECT user_id, content FROM t_order WHERE user_id not like '1'";
        result.add(select_case_12);
        String select_case_13 = "SELECT max(id) as max_id, user_id FROM t_order WHERE user_id = 1 group by user_id having max_id = 2";
        result.add(select_case_13);
        String select_case_14 = "SELECT max(id) as max_id, user_id FROM t_order WHERE user_id = 2 group by user_id having max_id = 1";
        result.add(select_case_14);
        String select_case_15 = "SELECT tor0.user_id as user_id_0, tor1.user_id as user_id_1, tor2.user_id as user_id_2 FROM t_order as tor0 left join t_order_data as tor1 on tor0.id = tor1.id " +
                "left join (select * from t_order_data) as tor2 on tor0.id = tor2.id where tor0.user_id = 1 and tor1.user_id in (1,1) and tor1.content = 'insert_case_2'";
        result.add(select_case_15);
        return result;
    }
    
    public static Collection<String> initNoteCase() {
        Collection<String> result = new LinkedList<>();
        String note_case_1 = "create table t_user(id int(11) not null , user_name varchar(32) not null ) /*shadow:true,foo:bar,aaa:ddd*/";
        result.add(note_case_1);
        String note_case_2 = "INSERT INTO t_order (user_id, content) VALUES (1, 'note_case_1') /*shadow:true,foo:bar,aaa:ddd*/";
        result.add(note_case_2);
        return result;
    }
}
