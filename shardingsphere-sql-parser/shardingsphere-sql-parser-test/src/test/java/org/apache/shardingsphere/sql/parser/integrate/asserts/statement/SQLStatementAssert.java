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

package org.apache.shardingsphere.sql.parser.integrate.asserts.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.parameter.ParameterMarkerAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.impl.AlterTableStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.impl.DeleteStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.impl.InsertStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.impl.SelectStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.impl.TCLStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.impl.UpdateStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.SQLParserTestCase;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.TCLStatement;

/**
 * SQL statement assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementAssert {
    
    /**
     * Assert SQL statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual SQL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SQLStatement actual, final SQLParserTestCase expected) {
        ParameterMarkerAssert.assertCount(assertContext, actual.getParametersCount(), expected.getParameters().size());
        if (actual instanceof SelectStatement) {
            SelectStatementAssert.assertIs(assertContext, (SelectStatement) actual, expected);
        } else if (actual instanceof UpdateStatement) {
            UpdateStatementAssert.assertIs(assertContext, (UpdateStatement) actual, expected);
        } else if (actual instanceof DeleteStatement) {
            DeleteStatementAssert.assertIs(assertContext, (DeleteStatement) actual, expected);
        } else if (actual instanceof InsertStatement) {
            InsertStatementAssert.assertIs(assertContext, (InsertStatement) actual, expected);
        } else if (actual instanceof AlterTableStatement) {
            AlterTableStatementAssert.assertIs(assertContext, (AlterTableStatement) actual, expected);
        } else if (actual instanceof TCLStatement) {
            TCLStatementAssert.assertIs((TCLStatement) actual, expected);
        }
    }
}
