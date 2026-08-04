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

package org.apache.shardingsphere.sql.parser.engine.mysql.visitor.statement;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLStatementVisitorTest {
    
    private static final CacheOption CACHE_OPTION = new CacheOption(128, 1024L);
    
    @Test
    void assertVisitCreateTemporaryTable() {
        CreateTableStatement statement = (CreateTableStatement) parse("CREATE TEMPORARY TABLE t_order (order_id INT)");
        assertTrue(statement.isTemporary());
    }
    
    @Test
    void assertVisitCreateTableWithoutTemporary() {
        CreateTableStatement statement = (CreateTableStatement) parse("CREATE TABLE t_order (order_id INT)");
        assertFalse(statement.isTemporary());
    }
    
    @Test
    void assertVisitCreateTableAsSelect() {
        CreateTableStatement statement = (CreateTableStatement) parse("CREATE TABLE t_projection AS SELECT id, name FROM source_table");
        assertTrue(statement.getSelectStatement().isPresent());
        SimpleTableSegment from = (SimpleTableSegment) statement.getSelectStatement().get().getFrom().get();
        assertThat(from.getTableName().getIdentifier().getValue(), is("source_table"));
    }
    
    @Test
    void assertVisitCreateTableAsSelectWithWindowFunction() {
        CreateTableStatement statement = (CreateTableStatement) parse(
                "CREATE TABLE t_window AS SELECT a.*, ROW_NUMBER() OVER(PARTITION BY a.user_id ORDER BY a.join_date DESC) rw FROM t_user a");
        ExpressionProjectionSegment projection = (ExpressionProjectionSegment) statement.getSelectStatement().get().getProjections().getProjections().get(1);
        FunctionSegment function = (FunctionSegment) projection.getExpr();
        assertTrue(function.getWindow().isPresent());
        assertThat(function.getWindow().get().getPartitionListSegments().size(), is(1));
        assertThat(function.getWindow().get().getOrderBySegment().getOrderByItems().size(), is(1));
    }
    
    @Test
    void assertVisitDropTemporaryTable() {
        DropTableStatement statement = (DropTableStatement) parse("DROP TEMPORARY TABLE t_order");
        assertTrue(statement.isTemporary());
    }
    
    @Test
    void assertVisitDropTableWithoutTemporary() {
        DropTableStatement statement = (DropTableStatement) parse("DROP TABLE t_order");
        assertFalse(statement.isTemporary());
    }
    
    private Object parse(final String sql) {
        ParseASTNode parseASTNode = new SQLParserEngine("MySQL", CACHE_OPTION).parse(sql, false);
        return new SQLStatementVisitorEngine("MySQL").visit(parseASTNode);
    }
}
