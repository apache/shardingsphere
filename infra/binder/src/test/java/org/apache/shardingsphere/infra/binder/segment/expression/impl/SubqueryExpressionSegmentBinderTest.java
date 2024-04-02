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

package org.apache.shardingsphere.infra.binder.segment.expression.impl;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SubqueryExpressionSegmentBinderTest {
    
    @Test
    void assertBind() {
        SelectStatement selectStatement = new SQLServerSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SubquerySegment subquery = new SubquerySegment(0, 10, selectStatement, "subquery");
        SubqueryExpressionSegment segment = new SubqueryExpressionSegment(subquery);
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(null, null, null, null);
        Map<String, TableSegmentBinderContext> tableBinderContexts = new HashMap<>();
        SubqueryExpressionSegment actual = SubqueryExpressionSegmentBinder.bind(segment, statementBinderContext, tableBinderContexts);
        SubqueryExpressionSegment expected = new SubqueryExpressionSegment(SubquerySegmentBinder.bind(segment.getSubquery(), statementBinderContext, tableBinderContexts));
        assertThat(actual, isA(SubqueryExpressionSegment.class));
        assertThat(actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(actual.getStopIndex(), is(expected.getStopIndex()));
        assertThat(actual.getText(), is(expected.getText()));
        assertThat(actual.getSubquery().getStartIndex(), is(expected.getSubquery().getStartIndex()));
        assertThat(actual.getSubquery().getStopIndex(), is(expected.getSubquery().getStopIndex()));
        assertThat(actual.getSubquery().getSubqueryType(), is(expected.getSubquery().getSubqueryType()));
        assertThat(actual.getSubquery().getText(), is(expected.getSubquery().getText()));
        assertThat(actual.getSubquery().getSelect().getDatabaseType(), is(expected.getSubquery().getSelect().getDatabaseType()));
        assertThat(actual.getSubquery().getSelect().getProjections().getProjections(), is(expected.getSubquery().getSelect().getProjections().getProjections()));
    }
}
