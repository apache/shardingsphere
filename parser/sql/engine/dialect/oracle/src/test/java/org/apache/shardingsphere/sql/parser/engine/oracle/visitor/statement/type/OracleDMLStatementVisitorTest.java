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

package org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

class OracleDMLStatementVisitorTest {
    
    private static final CacheOption CACHE_OPTION = new CacheOption(128, 1024L);
    
    @Test
    void assertVisitMergeUpdateWithScalarSubquery() {
        MergeStatement actual = parseMerge("MERGE INTO t_user target USING (SELECT ? user_id FROM DUAL) source ON (target.user_id = source.user_id) "
                + "WHEN MATCHED THEN UPDATE SET target.email = (SELECT MAX(u.email) FROM t_user u WHERE u.user_id < ?)");
        ExpressionSegment actualValue = actual.getUpdate().get().getAssignment().get().getAssignments().iterator().next().getValue();
        assertThat(actualValue, isA(SubqueryExpressionSegment.class));
        assertThat(actualValue.getText(), is("(SELECT MAX(u.email) FROM t_user u WHERE u.user_id < ?)"));
        assertThat(actual.getParameterCount(), is(2));
    }
    
    @Test
    void assertVisitMergeInsertWithScalarSubquery() {
        MergeStatement actual = parseMerge("MERGE INTO t_user target USING (SELECT ? user_id FROM DUAL) source ON (target.user_id = source.user_id) "
                + "WHEN NOT MATCHED THEN INSERT (user_id, email) VALUES (source.user_id, (SELECT MAX(u.email) FROM t_user u WHERE u.user_id < ?))");
        ExpressionSegment actualValue = actual.getInsert().get().getValues().iterator().next().getValues().get(1);
        assertThat(actualValue, isA(SubqueryExpressionSegment.class));
        assertThat(actualValue.getText(), is("(SELECT MAX(u.email) FROM t_user u WHERE u.user_id < ?)"));
        assertThat(actual.getParameterCount(), is(2));
    }
    
    @Test
    void assertVisitSystemDateTimeFunctions() {
        MergeStatement actual = parseMerge("MERGE INTO t_user target USING (SELECT ? user_id FROM DUAL) source ON (target.user_id = source.user_id) "
                + "WHEN MATCHED THEN UPDATE SET target.creation_date = SYSDATE "
                + "WHEN NOT MATCHED THEN INSERT (user_id, creation_date) VALUES (source.user_id, CAST(SYSTIMESTAMP AS TIMESTAMP))");
        ExpressionSegment actualSysdate = actual.getUpdate().get().getAssignment().get().getAssignments().iterator().next().getValue();
        FunctionSegment actualCast = (FunctionSegment) actual.getInsert().get().getValues().iterator().next().getValues().get(1);
        assertThat(actualSysdate, isA(FunctionSegment.class));
        assertThat(((FunctionSegment) actualSysdate).getFunctionName(), is("SYSDATE"));
        assertThat(actualCast.getParameters().iterator().next(), isA(FunctionSegment.class));
        assertThat(((FunctionSegment) actualCast.getParameters().iterator().next()).getFunctionName(), is("SYSTIMESTAMP"));
    }
    
    @Test
    void assertVisitQuotedSystemDateTimeColumn() {
        MergeStatement actual = parseMerge("MERGE INTO t_user target USING (SELECT ? user_id FROM DUAL) source ON (target.user_id = source.user_id) "
                + "WHEN MATCHED THEN UPDATE SET target.creation_date = \"SYSDATE\"");
        assertThat(actual.getUpdate().get().getAssignment().get().getAssignments().iterator().next().getValue(), isA(ColumnSegment.class));
    }
    
    @Test
    void assertVisitUpdateAssignmentWithScalarSubquery() {
        UpdateStatement actual = parseUpdate("UPDATE t_order SET status = (SELECT ? FROM DUAL) WHERE order_id = ?");
        assertThat(actual.getAssignment().get().getAssignments().iterator().next().getColumns().size(), is(1));
        assertThat(actual.getAssignment().get().getAssignments().iterator().next().getValue(), isA(SubqueryExpressionSegment.class));
    }
    
    @Test
    void assertVisitUpdateValueAssignment() {
        UpdateStatement actual = parseUpdate("UPDATE ot1 SET VALUE(ot1.x) = t1(20) WHERE VALUE(ot1.x) = t1(10)");
        assertThat(actual.getAssignment().get().getAssignments().iterator().next().getColumns().size(), is(1));
    }
    
    private MergeStatement parseMerge(final String sql) {
        return (MergeStatement) parse(sql);
    }
    
    private UpdateStatement parseUpdate(final String sql) {
        return (UpdateStatement) parse(sql);
    }
    
    private Object parse(final String sql) {
        return new SQLStatementVisitorEngine("Oracle").visit(new SQLParserEngine("Oracle", CACHE_OPTION).parse(sql, false));
    }
}
