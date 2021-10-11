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

package org.apache.shardingsphere.infra.optimize.converter.statement;

import lombok.SneakyThrows;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.optimize.converter.SQLNodeConvertEngine;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class SelectStatementConverterTest {
    
    private ShardingSphereSQLParserEngine sqlStatementParserEngine;
    
    @Before
    public void init() {
        sqlStatementParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(new MySQLDatabaseType()), new ConfigurationProperties(new Properties()));
    }
    
    @Test
    public void assertConvertSimpleSelect() {
        String sql = "select order_id, user_id from t_order";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        SqlNode sqlNode = SQLNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertThat(sqlSelect.getSelectList().size(), is(2));
        assertNull(sqlSelect.getWhere());
        assertNull(sqlSelect.getOffset());
        assertNull(sqlSelect.getFetch());
        SqlNode calciteSqlNode = parse(sql, new MySQLDatabaseType());
        assertNotNull(calciteSqlNode);
        assertThat(sqlNode.toString(), is(calciteSqlNode.toString()));
    }
    
    @Test
    public void assertConvertLimit() {
        String sql = "select order_id, user_id from t_order limit 1, 2";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        SqlNode sqlNode = SQLNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertThat(sqlSelect.getSelectList().size(), is(2));
        assertNull(sqlSelect.getWhere());
        assertNotNull(sqlSelect.getOffset());
        assertNotNull(sqlSelect.getFetch());
        SqlNode calciteSqlNode = parse(sql, new MySQLDatabaseType());
        assertNotNull(calciteSqlNode);
        assertThat(sqlNode.toString(), is(calciteSqlNode.toString()));
    }
    
    @Test
    public void assertConvertLimitWithRowCountOnly() {
        String sql = "select order_id, user_id from t_order limit 2";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        SqlNode sqlNode = SQLNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertThat(sqlSelect.getSelectList().size(), is(2));
        assertNull(sqlSelect.getWhere());
        assertNull(sqlSelect.getOffset());
        assertNotNull(sqlSelect.getFetch());
        SqlNode calciteSqlNode = parse(sql, new MySQLDatabaseType());
        assertNotNull(calciteSqlNode);
        assertThat(sqlNode.toString(), is(calciteSqlNode.toString()));
    }
    
    @Test
    public void assertConvertWhere() {
        String sql = "select order_id, user_id from t_order where order_id = 10";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        SqlNode sqlNode = SQLNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertThat(sqlSelect.getSelectList().size(), is(2));
        assertNotNull(sqlSelect.getWhere());
        SqlNode calciteSqlNode = parse(sql, new MySQLDatabaseType());
        assertNotNull(calciteSqlNode);
        assertThat(sqlNode.toString(), is(calciteSqlNode.toString()));
    }
    
    @Test
    public void assertConvertWhereAndGroupBy() {
        String sql = "select order_id, user_id from t_order where order_id = 10 group by order_id";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        SqlNode sqlNode = SQLNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertThat(sqlSelect.getSelectList().size(), is(2));
        assertNotNull(sqlSelect.getWhere());
        assertThat(sqlSelect.getGroup().size(), is(1));
        SqlNode calciteSqlNode = parse(sql, new MySQLDatabaseType());
        assertNotNull(calciteSqlNode);
        assertThat(sqlNode.toString(), is(calciteSqlNode.toString()));
    }
    
    @Test
    public void assertConvertWhereAndOrderBy() {
        String sql = "select order_id, user_id from t_order where user_id = 10 order by order_id desc";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        SqlNode sqlNode = SQLNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertThat(sqlSelect.getSelectList().size(), is(2));
        assertNotNull(sqlSelect.getWhere());
        assertThat(sqlSelect.getOrderList().size(), is(1));
        SqlNode calciteSqlNode = parse(sql, new MySQLDatabaseType());
        assertNotNull(calciteSqlNode);
        assertThat(sqlNode.toString(), is(calciteSqlNode.toString()));
    }
    
    @Test
    public void assertConvertInnerJoin() {
        String sql = "select 10 + 30, o1.order_id + 10, o1.order_id, o1.user_id, o2.status from t_order o1 join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 order by "
                + "o1.order_id desc";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        assertThat(((JoinTableSegment) ((MySQLSelectStatement) sqlStatement).getFrom()).getJoinType(), is("INNER"));
        SqlNode sqlNode = SQLNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        assertThat(sqlSelect.getFrom(), instanceOf(SqlJoin.class));
        assertThat(sqlSelect.getOrderList().size(), is(1));
    }
    
    @Test
    public void assertConvertLeftOuterJoin() {
        String sql = "select 10 + 30, o1.order_id + 10, o1.order_id, o1.user_id, o2.status from t_order o1 left outer join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 order by "
                + "o1.order_id desc";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        SqlNode sqlNode = SQLNodeConvertEngine.convert(sqlStatement);
        assertThat(sqlNode, instanceOf(SqlSelect.class));
        // TODO outer join is not supported by parser of ShardingSphere 
    }
    
    @SneakyThrows(SqlParseException.class)
    private SqlNode parse(final String sql, final DatabaseType databaseType) {
        return SqlParser.create(sql, Config.DEFAULT.withConformance(getSQLConformance(databaseType)).withLex(getLex(databaseType))).parseQuery();
    }
    
    private Lex getLex(final DatabaseType databaseType) {
        return databaseType instanceof MySQLDatabaseType ? Lex.MYSQL : Lex.ORACLE;
    }
    
    private SqlConformanceEnum getSQLConformance(final DatabaseType databaseType) {
        return databaseType instanceof MySQLDatabaseType ? SqlConformanceEnum.MYSQL_5 : SqlConformanceEnum.DEFAULT;
    }
}
