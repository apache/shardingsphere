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

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public final class SQLRouteTest extends AbstractSQLRouteTest {
    
    @Test
    public void assertNoTableUnicastRandomDataSource() {
        String sql = "SELECT id,name ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertWithBroadcastTable() {
        String sql = "SELECT id,name from t_order_item a join product b on a.product_id = b.product_id where user_id = ?";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertAllBindingWithBroadcastTable() {
        String sql = "SELECT id,name from t_order a join t_order_item b on a.order_id = b.order_id join t_product c on b.product_id = c.product_id where a.user_id = ?";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertComplexTableWithBroadcastTable() {
        String sql = "SELECT id,name from t_order a join t_user b on a.user_id = b.user_id join t_product c on a.product_id = c.product_id where a.user_id = ? and b.user_id =?";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        assertRoute(sql, parameters);
    }
    
    @Test
    public void assertInsertTable() {
        String sql = "INSERT INTO t_order (order_id, user_id) VALUES (?, ?)";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        assertRoute(sql, parameters);
    }
}
