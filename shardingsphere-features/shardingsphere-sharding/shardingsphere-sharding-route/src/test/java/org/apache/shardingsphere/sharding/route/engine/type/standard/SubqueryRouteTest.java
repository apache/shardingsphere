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
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public final class SubqueryRouteTest extends AbstractSQLRouteTest {
    
    @Test(expected = IllegalStateException.class)
    public void assertOneTableError() {
        String sql = "select (select max(id) from t_order b where b.user_id =? ) from t_order a where user_id = ? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(3);
        parameters.add(2);
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertOneTable() {
        String sql = "select (select max(id) from t_order b where b.user_id = ? and b.user_id = a.user_id) from t_order a where user_id = ? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertBindingTable() {
        String sql = "select (select max(id) from t_order_item b where b.user_id = ?) from t_order a where user_id = ? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertNotShardingTable() {
        String sql = "select (select max(id) from t_category b where b.id = ?) from t_category a where id = ? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        assertRoute(sql, parameters);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertBindingTableWithDifferentValue() {
        String sql = "select (select max(id) from t_order_item b where b.user_id = ? ) from t_order a where user_id = ? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(2);
        parameters.add(3);
        assertRoute(sql, parameters);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertTwoTableWithDifferentOperator() {
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(2);
        parameters.add(1);
        String sql = "select (select max(id) from t_order_item b where b.user_id in(?,?)) from t_order a where user_id = ? ";
        assertRoute(sql, parameters);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertTwoTableWithIn() {
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(2);
        parameters.add(1);
        parameters.add(3);
        String sql = "select (select max(id) from t_order_item b where b.user_id in(?,?)) from t_order a where user_id in(?,?) ";
        assertRoute(sql, parameters);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertSubqueryInSubqueryError() {
        List<Object> parameters = new LinkedList<>();
        parameters.add(11);
        parameters.add(2);
        parameters.add(1);
        parameters.add(1);
        String sql = "select (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?)) as c from t_order a "
                + "where status = (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?))";
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertSubqueryInSubquery() {
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        parameters.add(1);
        parameters.add(1);
        String sql = "select (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?)) as c from t_order a "
                + "where status = (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?))";
        assertRoute(sql, parameters);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertSubqueryInFromError() {
        String sql = "select status from t_order b join (select user_id,status from t_order b where b.user_id =?) c on b.user_id = c.user_id where b.user_id =? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(11);
        parameters.add(1);
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertSubqueryInFrom() {
        String sql = "select status from t_order b join (select user_id,status from t_order b where b.user_id =?) c on b.user_id = c.user_id where b.user_id =? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertSubqueryForAggregation() {
        String sql = "select count(*) from t_order where c.user_id = (select user_id from t_order where user_id =?) ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertSubqueryForBinding() {
        String sql = "select count(*) from t_order where user_id = (select user_id from t_order_item where user_id =?) ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        assertRoute(sql, parameters);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertSubqueryWithoutHint() {
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(2);
        parameters.add(5);
        String sql = "select count(*) from t_hint_test where user_id = (select t_hint_test from t_hint_test where user_id in (?,?,?)) ";
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertSubqueryWithHint() {
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_test", 1);
        hintManager.addTableShardingValue("t_hint_test", 1);
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(3);
        parameters.add(5);
        String sql = "select count(*) from t_hint_test where user_id = (select t_hint_test from t_hint_test where user_id in (?,?,?)) ";
        assertRoute(sql, parameters);
        hintManager.close();
    }
}
