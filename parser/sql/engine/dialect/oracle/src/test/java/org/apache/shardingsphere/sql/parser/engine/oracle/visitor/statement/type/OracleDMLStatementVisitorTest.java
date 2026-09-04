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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ErrorLoggingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    
    @Test
    void assertVisitInsertErrorLogging() {
        InsertStatement actual = (InsertStatement) parse("INSERT INTO t_user VALUES (?) LOG ERRORS INTO ERR$_T_USER (?) REJECT LIMIT UNLIMITED");
        assertThat(actual.getParameterCount(), is(2));
        assertErrorLogging(actual.getErrorLogging().get(), "ERR$_T_USER", 1, "UNLIMITED");
    }
    
    @Test
    void assertVisitMultiTableInsertErrorLogging() {
        InsertStatement actual = (InsertStatement) parse("INSERT ALL INTO t_order VALUES (?) LOG ERRORS INTO ERR$_T_ORDER (?) REJECT LIMIT 1 "
                + "INTO t_order VALUES (2) LOG ERRORS REJECT LIMIT UNLIMITED SELECT 1 FROM dual");
        Iterator<InsertStatement> insertStatements = actual.getMultiTableInsertInto().get().getInsertStatements().iterator();
        assertErrorLogging(insertStatements.next().getErrorLogging().get(), "ERR$_T_ORDER", 1, "1");
        ErrorLoggingSegment actualErrorLogging = insertStatements.next().getErrorLogging().get();
        assertNull(actualErrorLogging.getTable());
        assertNull(actualErrorLogging.getTag());
        assertThat(actualErrorLogging.getRejectLimit(), is("UNLIMITED"));
        assertThat(actual.getParameterCount(), is(2));
    }
    
    @Test
    void assertVisitUpdateErrorLogging() {
        UpdateStatement actual = parseUpdate("UPDATE t_user SET email = ? WHERE user_id = ? LOG ERRORS INTO ERR$_T_USER (?) REJECT LIMIT 2");
        assertThat(actual.getParameterCount(), is(3));
        assertErrorLogging(actual.getErrorLogging().get(), "ERR$_T_USER", 2, "2");
    }
    
    @Test
    void assertVisitDeleteErrorLogging() {
        DeleteStatement actual = (DeleteStatement) parse("DELETE FROM t_user WHERE user_id = ? LOG ERRORS INTO ERR$_T_USER (?) REJECT LIMIT UNLIMITED");
        assertThat(actual.getParameterCount(), is(2));
        assertErrorLogging(actual.getErrorLogging().get(), "ERR$_T_USER", 1, "UNLIMITED");
    }
    
    @Test
    void assertVisitMergeErrorLogging() {
        MergeStatement actual = parseMerge("MERGE INTO t_user target USING t_user source ON (target.user_id = source.user_id) "
                + "WHEN MATCHED THEN UPDATE SET target.email = source.email LOG ERRORS INTO ERR$_T_USER (?) REJECT LIMIT 3");
        assertThat(actual.getParameterCount(), is(1));
        assertErrorLogging(actual.getErrorLogging().get(), "ERR$_T_USER", 0, "3");
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
    
    private void assertErrorLogging(final ErrorLoggingSegment actual, final String expectedTable, final int expectedParameterIndex, final String expectedRejectLimit) {
        assertThat(actual.getTable().getTableName().getIdentifier().getValue(), is(expectedTable));
        assertThat(actual.getTag(), isA(ParameterMarkerExpressionSegment.class));
        assertThat(((ParameterMarkerExpressionSegment) actual.getTag()).getParameterMarkerIndex(), is(expectedParameterIndex));
        assertThat(actual.getRejectLimit(), is(expectedRejectLimit));
    }
}
