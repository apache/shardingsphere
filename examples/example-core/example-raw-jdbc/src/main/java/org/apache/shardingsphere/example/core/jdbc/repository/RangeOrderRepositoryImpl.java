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

package org.apache.shardingsphere.example.core.jdbc.repository;

import org.apache.shardingsphere.example.core.api.entity.Order;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public final class RangeOrderRepositoryImpl extends OrderRepositoryImpl {
    
    public RangeOrderRepositoryImpl(final DataSource dataSource) {
        super(dataSource);
    }
    
    @Override
    public List<Order> selectAll() throws SQLException {
        String sql = "SELECT * FROM t_order WHERE order_id BETWEEN 200000000000000000 AND 400000000000000000";
        return getOrders(sql);
    }
}
