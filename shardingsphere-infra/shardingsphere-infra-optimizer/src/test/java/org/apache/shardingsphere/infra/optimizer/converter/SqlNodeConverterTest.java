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

package org.apache.shardingsphere.infra.optimizer.converter;

import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.optimizer.schema.AbstractSchemaTest;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertTrue;

/**
 * testcase of converting shardingshphere ast to calcite ast.
 * 
 * <p>after converting phrase finished, the next phrase is comparing  the converted result with the
 * result of calcite parser.
 * </p>
 */
public final class SqlNodeConverterTest extends AbstractSchemaTest {

    private ShardingSphereSQLParserEngine sqlStatementParserEngine;

    @Before
    public void init() {
        sqlStatementParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(
                new MySQLDatabaseType()));
    }

    @Test
    public void testConvertSimpleSelect() {
        String sql = "select order_id, user_id from t_order";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        Optional<SqlNode> optional = SqlNodeConverter.convertSqlStatement(sqlStatement);
        assertTrue(optional.isPresent());
        Assert.assertThat(optional.get(), instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) optional.get();
        Assert.assertEquals(2, sqlSelect.getSelectList().size());
        Assert.assertNull(sqlSelect.getWhere());
        /* 
         TODO compare ast from calcite parser and ast converted from ss ast if possible
        SqlParser parser = SqlParser.create(sql);
        SqlNode calciteSqlNode = parser.parseQuery();
        Assert.assertNotNull(calciteSqlNode);
        */
    }
    
    @Test
    public void testConvertSimpleSelectFilter() {
        String sql = "select order_id, user_id from t_order where order_id = 10";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        
        Optional<SqlNode> optional = SqlNodeConverter.convertSqlStatement(sqlStatement);
        assertTrue(optional.isPresent());
        Assert.assertThat(optional.get(), instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) optional.get();
        Assert.assertEquals(2, sqlSelect.getSelectList().size());
        Assert.assertNotNull(sqlSelect.getWhere());
    }
    
    @Test
    public void testConvertSimpleSelectFilterGroupBy() {
        String sql = "select order_id, user_id from t_order where order_id = 10 group by order_id";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        
        Optional<SqlNode> optional = SqlNodeConverter.convertSqlStatement(sqlStatement);
        assertTrue(optional.isPresent());
        Assert.assertThat(optional.get(), instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) optional.get();
        Assert.assertEquals(2, sqlSelect.getSelectList().size());
        Assert.assertNotNull(sqlSelect.getWhere());
        Assert.assertEquals(1, sqlSelect.getGroup().size());
    }
    
    @Test
    public void testConvertSimpleSelectFilterOrderBy() {
        String sql = "select order_id, user_id from t_order where user_id = 10 order by order_id desc";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        
        Optional<SqlNode> optional = SqlNodeConverter.convertSqlStatement(sqlStatement);
        assertTrue(optional.isPresent());
        Assert.assertThat(optional.get(), instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) optional.get();
        Assert.assertEquals(2, sqlSelect.getSelectList().size());
        Assert.assertNotNull(sqlSelect.getWhere());
        Assert.assertEquals(1, sqlSelect.getOrderList().size());
    }
    
    @Test
    public void testConvertInnerJoin() {
        String sql = "select 10 + 30, o1.order_id + 10, o1.order_id, o1.user_id, o2.status from t_order o1 join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 order by "
                + "o1.order_id desc";
    
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        Optional<SqlNode> optional = SqlNodeConverter.convertSqlStatement(sqlStatement);
        Assert.assertTrue(optional.isPresent());
        Assert.assertThat(optional.get(), instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) optional.get();
        Assert.assertThat(sqlSelect.getFrom(), instanceOf(SqlJoin.class));
        Assert.assertEquals(1, sqlSelect.getOrderList().size());
    }
    
    @Test
    public void testConvertLeftOuterJoin() {
        String sql = "select 10 + 30, o1.order_id + 10, o1.order_id, o1.user_id, o2.status from t_order o1 left outer join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 order by "
                + "o1.order_id desc";
        
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        Optional<SqlNode> optional = SqlNodeConverter.convertSqlStatement(sqlStatement);
        Assert.assertTrue(optional.isPresent());
        // TODO outer join is not supported by parser of ShardingSphere 
    }

}
