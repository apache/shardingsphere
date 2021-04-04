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

package org.apache.shardingsphere.infra.optimizer.sql;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ExtractTableNameSqlShuttleTest {
    
    @Test
    public void testVisitFromWithIdentifier() throws SqlParseException {
        String sql = "select 10 + 30 as prefix, t_order.order_id + 10, t_order.order_id, t_order.user_id from t_order where t_order.status='FINISHED' "
                + "and 1=1 order by t_order.order_id desc";
        
        SqlParser parser = SqlParser.create(sql);
        SqlNode calciteSqlNode = parser.parseQuery();
        Assert.assertNotNull(calciteSqlNode);
    
        ExtractTableNameSqlShuttle extractTableNameSqlShuttle = new ExtractTableNameSqlShuttle();
        SqlNode newNode = calciteSqlNode.accept(extractTableNameSqlShuttle);
        Assert.assertEquals(Arrays.asList("T_ORDER"), extractTableNameSqlShuttle.getTableNames().stream()
                .map(SqlDynamicValueParam::getOriginal).collect(Collectors.toList()));
        // TODO check result SqlNode
    }
    
    @Test
    public void testVisitFromWithAsBasicCall() throws SqlParseException {
        String sql = "select 10 + 30 as prefix, o1.order_id + 10, o1.order_id, o1.user_id from t_order as o1 where o1.status='FINISHED' " 
                + "and 1=1 order by o1.order_id desc";
        
        SqlParser parser = SqlParser.create(sql);
        SqlNode calciteSqlNode = parser.parseQuery();
        Assert.assertNotNull(calciteSqlNode);
        
        ExtractTableNameSqlShuttle extractTableNameSqlShuttle = new ExtractTableNameSqlShuttle();
        SqlNode newNode = calciteSqlNode.accept(extractTableNameSqlShuttle);
        Assert.assertEquals(Arrays.asList("T_ORDER"), extractTableNameSqlShuttle.getTableNames().stream()
                .map(SqlDynamicValueParam::getOriginal).collect(Collectors.toList()));
        // TODO check result SqlNode
    }
    
    @Test
    public void testVisitFromWithJoinAsBasicCall() throws SqlParseException {
        String sql = "select 10 + 30, o1.order_id + 10, o1.order_id, o1.user_id, o2.status from t_order o1 join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 order by "
                + "o1.order_id desc";
        
        SqlParser parser = SqlParser.create(sql);
        SqlNode calciteSqlNode = parser.parseQuery();
        Assert.assertNotNull(calciteSqlNode);
        
        ExtractTableNameSqlShuttle extractTableNameSqlShuttle = new ExtractTableNameSqlShuttle();
        SqlNode newNode = calciteSqlNode.accept(extractTableNameSqlShuttle);
        Assert.assertEquals(Arrays.asList("T_ORDER", "T_ORDER_ITEM"), extractTableNameSqlShuttle.getTableNames().stream()
                .map(SqlDynamicValueParam::getOriginal).collect(Collectors.toList()));
        // TODO check result SqlNode
    }
}
