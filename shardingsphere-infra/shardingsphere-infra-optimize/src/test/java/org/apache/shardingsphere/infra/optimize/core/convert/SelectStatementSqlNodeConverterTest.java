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

package org.apache.shardingsphere.infra.optimize.core.convert;

import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * testcase of converting shardingshphere ast to calcite ast.
 *
 * <p>after converting phrase finished, the next phrase is comparing  the converted result with the
 * result of calcite parser.
 * </p>
 */
public final class SelectStatementSqlNodeConverterTest extends BaseSqlNodeConverterTest {

    private ShardingSphereSQLParserEngine sqlStatementParserEngine;

    @Before
    public void init() {
        sqlStatementParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(
                new MySQLDatabaseType()));
    }

    @Test
    public void testConvertSimpleSelect() {
        String sql = "select order_id, user_id from t_order";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql.toUpperCase(), false);
        SqlNode sqlNode = SqlNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertEquals(2, sqlSelect.getSelectList().size());
        assertNull(sqlSelect.getWhere());
        assertNull(sqlSelect.getOffset());
        assertNull(sqlSelect.getFetch());
        // compare ast from calcite parser
        SqlNode calciteSqlNode = parseByCalciteParser(sql);
        assertNotNull(calciteSqlNode);
        assertEquals(sqlNode.toString(), calciteSqlNode.toString());
    }

    @Test
    public void testConvertSimpleSelectLimit() {
        String sql = "select order_id, user_id from t_order limit 1, 2";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql.toUpperCase(), false);
        SqlNode sqlNode = SqlNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertEquals(2, sqlSelect.getSelectList().size());
        assertNull(sqlSelect.getWhere());
        assertNotNull(sqlSelect.getOffset());
        assertNotNull(sqlSelect.getFetch());
        SqlNode calciteSqlNode = parseByCalciteParser(sql, new MySQLDatabaseType());
        assertNotNull(calciteSqlNode);
        assertEquals(sqlNode.toString(), calciteSqlNode.toString());
    }

    @Test
    public void testConvertSimpleSelectRowCount() {
        String sql = "select order_id, user_id from t_order limit 2";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql.toUpperCase(), false);
        SqlNode sqlNode = SqlNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertEquals(2, sqlSelect.getSelectList().size());
        assertNull(sqlSelect.getWhere());
        assertNull(sqlSelect.getOffset());
        assertNotNull(sqlSelect.getFetch());
        SqlNode calciteSqlNode = parseByCalciteParser(sql, new MySQLDatabaseType());
        assertNotNull(calciteSqlNode);
        assertEquals(sqlNode.toString(), calciteSqlNode.toString());
    }

    @Test
    public void testConvertSimpleSelectFilter() {
        String sql = "select order_id, user_id from t_order where order_id = 10";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql.toUpperCase(), false);
        SqlNode sqlNode = SqlNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertEquals(2, sqlSelect.getSelectList().size());
        assertNotNull(sqlSelect.getWhere());
        SqlNode calciteSqlNode = parseByCalciteParser(sql, new MySQLDatabaseType());
        assertNotNull(calciteSqlNode);
        assertEquals(sqlNode.toString(), calciteSqlNode.toString());
    }

    @Test
    public void testConvertSimpleSelectFilterGroupBy() {
        String sql = "select order_id, user_id from t_order where order_id = 10 group by order_id";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql.toUpperCase(), false);
        SqlNode sqlNode = SqlNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertEquals(2, sqlSelect.getSelectList().size());
        assertNotNull(sqlSelect.getWhere());
        assertEquals(1, sqlSelect.getGroup().size());
        SqlNode calciteSqlNode = parseByCalciteParser(sql, new MySQLDatabaseType());
        assertNotNull(calciteSqlNode);
        assertEquals(sqlNode.toString(), calciteSqlNode.toString());
    }

    @Test
    public void testConvertSimpleSelectFilterOrderBy() {
        String sql = "select order_id, user_id from t_order where user_id = 10 order by order_id desc";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql.toUpperCase(), false);
        SqlNode sqlNode = SqlNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertEquals(2, sqlSelect.getSelectList().size());
        assertNotNull(sqlSelect.getWhere());
        assertEquals(1, sqlSelect.getOrderList().size());
        SqlNode calciteSqlNode = parseByCalciteParser(sql, new MySQLDatabaseType());
        assertNotNull(calciteSqlNode);
        assertEquals(sqlNode.toString(), calciteSqlNode.toString());
    }

    @Test
    public void testConvertInnerJoin() {
        String sql = "select 10 + 30, o1.order_id + 10, o1.order_id, o1.user_id, o2.status from t_order o1 join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 order by "
                + "o1.order_id desc";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        assertEquals("INNER", ((JoinTableSegment) ((MySQLSelectStatement) sqlStatement).getFrom()).getJoinType());
        SqlNode sqlNode = SqlNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertThat(sqlSelect.getFrom(), instanceOf(SqlJoin.class));
        assertEquals(1, sqlSelect.getOrderList().size());
    }

    @Test
    public void testConvertLeftOuterJoin() {
        String sql = "select 10 + 30, o1.order_id + 10, o1.order_id, o1.user_id, o2.status from t_order o1 left outer join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 order by "
                + "o1.order_id desc";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        SqlNode sqlNode = SqlNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        // TODO outer join is not supported by parser of ShardingSphere 
    }
}
