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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.sqlserver.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.DeclareVariableStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.definition.ColumnDefinitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.sqlserver.variable.SQLServerDeclareTableVariableStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Declare table variable statement assert for SQLServer.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLServerDeclareVariableStatementAssert {
    
    /**
     * Assert declare table variable statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual declare variable statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DeclareVariableStatement actual,
                                final SQLServerDeclareTableVariableStatementTestCase expected) {
        SQLSegmentAssert.assertIs(assertContext, actual.getVariableName(), expected.getVariableName());
        assertThat(assertContext.getText("Variable name assertion error: "), actual.getVariableName().getVariable(), is(expected.getVariableName().getVariable()));
        assertColumnDefinitions(assertContext, actual, expected);
    }
    
    private static void assertColumnDefinitions(final SQLCaseAssertContext assertContext, final DeclareVariableStatement actual,
                                                final SQLServerDeclareTableVariableStatementTestCase expected) {
        assertThat(assertContext.getText("Column definitions size assertion error: "), actual.getColumnDefinitions().size(), is(expected.getColumnDefinitions().size()));
        int count = 0;
        for (ColumnDefinitionSegment each : actual.getColumnDefinitions()) {
            ColumnDefinitionAssert.assertIs(assertContext, each, expected.getColumnDefinitions().get(count));
            count++;
        }
    }
}
