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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleDMLStatementVisitorTest {
    
    private static final CacheOption CACHE_OPTION = new CacheOption(128, 1024L);
    
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
        UpdateStatement actual = (UpdateStatement) parse("UPDATE t_user SET email = ? WHERE user_id = ? LOG ERRORS INTO ERR$_T_USER (?) REJECT LIMIT 2");
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
        MergeStatement actual = (MergeStatement) parse("MERGE INTO t_user target USING t_user source ON (target.user_id = source.user_id) "
                + "WHEN MATCHED THEN UPDATE SET target.email = source.email LOG ERRORS INTO ERR$_T_USER (?) REJECT LIMIT 3");
        assertThat(actual.getParameterCount(), is(1));
        assertErrorLogging(actual.getErrorLogging().get(), "ERR$_T_USER", 0, "3");
    }
    
    private static Object parse(final String sql) {
        return new SQLStatementVisitorEngine("Oracle").visit(new SQLParserEngine("Oracle", CACHE_OPTION).parse(sql, false));
    }
    
    private static void assertErrorLogging(final ErrorLoggingSegment actual, final String expectedTable, final int expectedParameterIndex, final String expectedRejectLimit) {
        assertThat(actual.getTable().getTableName().getIdentifier().getValue(), is(expectedTable));
        assertTrue(actual.getTag() instanceof ParameterMarkerExpressionSegment);
        assertThat(((ParameterMarkerExpressionSegment) actual.getTag()).getParameterMarkerIndex(), is(expectedParameterIndex));
        assertThat(actual.getRejectLimit(), is(expectedRejectLimit));
    }
}
