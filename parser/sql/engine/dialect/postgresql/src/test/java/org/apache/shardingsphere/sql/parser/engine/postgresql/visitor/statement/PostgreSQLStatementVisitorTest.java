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

package org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLStatementVisitorTest {
    
    @Test
    void assertVisitUnaryNotWithScalarSubquery() {
        String sql = "select 1 from t17 as ref_0 where not ((ref_0.c59) < (select ref_1.c36 as c_0 from t23 as ref_1 join t22 as ref_2 "
                + "on(ref_1.colocated_key = ref_2.colocated_key)))";
        ParseASTNode parseASTNode = new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false);
        SelectStatement statement = (SelectStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(parseASTNode);
        assertTrue(statement.getWhere().isPresent());
        assertThat(statement.getWhere().get().getExpr(), isA(NotExpression.class));
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(statement);
        Collection<String> rewriteTableNames = tableExtractor.getRewriteTables().stream()
                .map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList());
        assertThat(rewriteTableNames, hasItems("t17", "t23", "t22"));
    }
    
    @Test
    void assertVisitSelectWithValuesDerivedTableColumnAliases() {
        SelectStatement statement = (SelectStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(
                new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse("SELECT * FROM (VALUES ('k1', 'v1', 1)) AS t(pk, col_a, col_a_type)", false));
        assertTrue(statement.getFrom().isPresent());
        assertThat(statement.getFrom().get(), isA(SubqueryTableSegment.class));
        SubqueryTableSegment subqueryTableSegment = (SubqueryTableSegment) statement.getFrom().get();
        assertTrue(subqueryTableSegment.getAliasSegment().isPresent());
        assertThat(subqueryTableSegment.getAliasSegment().get().getIdentifier().getValue(), is("t"));
        Collection<String> actualColumnAliases = subqueryTableSegment.getAliasSegment().get().getColumnAliases().stream().map(IdentifierValue::getValue).collect(Collectors.toList());
        assertThat(actualColumnAliases, contains("pk", "col_a", "col_a_type"));
        Collection<ProjectionSegment> actualProjections = subqueryTableSegment.getSubquery().getSelect().getProjections().getProjections();
        assertThat(actualProjections.size(), is(3));
        actualProjections.forEach(each -> assertThat(each, isA(ExpressionProjectionSegment.class)));
    }
    
    @Test
    void assertVisitUpdateWithValuesDerivedTableColumnAliases() {
        UpdateStatement statement = (UpdateStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L))
                .parse("UPDATE t_order SET status = t.status FROM (VALUES (1, 'PAID')) AS t(order_id, status) WHERE t_order.order_id = t.order_id", false));
        assertTrue(statement.getFrom().isPresent());
        assertThat(statement.getFrom().get(), isA(SubqueryTableSegment.class));
        SubqueryTableSegment subqueryTableSegment = (SubqueryTableSegment) statement.getFrom().get();
        assertTrue(subqueryTableSegment.getAliasSegment().isPresent());
        assertThat(subqueryTableSegment.getAliasSegment().get().getIdentifier().getValue(), is("t"));
        Collection<String> actualColumnAliases = subqueryTableSegment.getAliasSegment().get().getColumnAliases().stream().map(IdentifierValue::getValue).collect(Collectors.toList());
        assertThat(actualColumnAliases, contains("order_id", "status"));
        assertThat(subqueryTableSegment.getSubquery().getSelect().getProjections().getProjections().size(), is(2));
    }
}
