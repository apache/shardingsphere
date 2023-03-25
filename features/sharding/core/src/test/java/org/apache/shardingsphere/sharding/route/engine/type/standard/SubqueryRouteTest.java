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

package org.apache.shardingsphere.sharding.route.engine.type.standard;

import org.apache.shardingsphere.infra.hint.HintManager;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

class SubqueryRouteTest extends AbstractSQLRouteTest {
    
    @Test
    void assertOneTableDifferentConditionWithFederation() {
        String sql = "select (select max(id) from t_order b where b.user_id =? ) from t_order a where user_id = ? ";
        assertRoute(sql, Arrays.asList(3, 2));
    }
    
    @Test
    void assertOneTableSameConditionWithFederation() {
        String sql = "select (select max(id) from t_order b where b.user_id = ? and b.user_id = a.user_id) from t_order a where user_id = ? ";
        assertRoute(sql, Arrays.asList(1, 1));
    }
    
    @Test
    void assertBindingTableWithFederation() {
        String sql = "select (select max(id) from t_order_item b where b.user_id = ?) from t_order a where user_id = ? ";
        assertRoute(sql, Arrays.asList(1, 1));
    }
    
    @Test
    void assertNotShardingTable() {
        String sql = "select (select max(id) from t_category b where b.id = ?) from t_category a where id = ? ";
        assertRoute(sql, Arrays.asList(1, 1));
    }
    
    @Test
    void assertBindingTableWithDifferentValueWithFederation() {
        String sql = "select (select max(id) from t_order_item b where b.user_id = ? ) from t_order a where user_id = ? ";
        assertRoute(sql, Arrays.asList(2, 3));
    }
    
    @Test
    void assertTwoTableWithDifferentOperatorWithFederation() {
        String sql = "select (select max(id) from t_order_item b where b.user_id in(?,?)) from t_order a where user_id = ? ";
        assertRoute(sql, Arrays.asList(1, 2, 1));
    }
    
    @Test
    void assertTwoTableWithInWithFederation() {
        String sql = "select (select max(id) from t_order_item b where b.user_id in(?,?)) from t_order a where user_id in(?,?) ";
        assertRoute(sql, Arrays.asList(1, 2, 1, 3));
    }
    
    @Test
    void assertSubqueryInSubqueryError() {
        String sql = "select (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?)) as c from t_order a "
                + "where status = (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?))";
        assertRoute(sql, Arrays.asList(11, 2, 1, 1));
    }
    
    @Test
    void assertSubqueryInSubquery() {
        String sql = "select (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?)) as c from t_order a "
                + "where status = (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?))";
        assertRoute(sql, Arrays.asList(1, 1, 1, 1));
    }
    
    @Test
    void assertSubqueryInFromError() {
        String sql = "select status from t_order b join (select user_id,status from t_order b where b.user_id =?) c on b.user_id = c.user_id where b.user_id =? ";
        assertRoute(sql, Arrays.asList(11, 1));
    }
    
    @Test
    void assertSubqueryInFrom() {
        String sql = "select status from t_order b join (select user_id,status from t_order b where b.user_id =?) c on b.user_id = c.user_id where b.user_id =? ";
        assertRoute(sql, Arrays.asList(1, 1));
    }
    
    @Test
    void assertSubqueryForAggregation() {
        String sql = "select count(*) from t_order where user_id = (select user_id from t_order where user_id =?) ";
        assertRoute(sql, Collections.singletonList(1));
    }
    
    @Test
    void assertSubqueryForBinding() {
        String sql = "select count(*) from t_order where user_id = (select user_id from t_order_item where user_id =?) ";
        assertRoute(sql, Collections.singletonList(1));
    }
    
    @Test
    void assertSubqueryWithHint() {
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_test", 1);
        hintManager.addTableShardingValue("t_hint_test", 1);
        String sql = "select count(*) from t_hint_test where user_id = (select t_hint_test from t_hint_test where user_id in (?,?,?)) ";
        assertRoute(sql, Arrays.asList(1, 3, 5));
        hintManager.close();
    }
    
    @Test
    void assertSubqueryWithOneInstance() {
        String sql = "select count(*) from t_order where user_id =?";
        assertRoute(sql, Collections.singletonList(1));
    }
}
