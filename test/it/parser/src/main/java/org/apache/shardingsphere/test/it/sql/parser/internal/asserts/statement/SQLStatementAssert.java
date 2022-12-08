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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rul.RULStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.parameter.ParameterMarkerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.comment.CommentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.DALStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.DCLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.DDLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.RALStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.RDLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rql.RQLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rul.RULStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.DMLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.tcl.TCLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;

/**
 * SQL statement assert.
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
        ParameterMarkerAssert.assertCount(assertContext, actual.getParameterCount(), expected.getParameters().size());
        CommentAssert.assertComment(assertContext, actual, expected);
        if (actual instanceof DMLStatement) {
            DMLStatementAssert.assertIs(assertContext, (DMLStatement) actual, expected);
        } else if (actual instanceof DDLStatement) {
            DDLStatementAssert.assertIs(assertContext, (DDLStatement) actual, expected);
        } else if (actual instanceof TCLStatement) {
            TCLStatementAssert.assertIs(assertContext, (TCLStatement) actual, expected);
        } else if (actual instanceof DCLStatement) {
            DCLStatementAssert.assertIs(assertContext, (DCLStatement) actual, expected);
        } else if (actual instanceof DALStatement) {
            DALStatementAssert.assertIs(assertContext, (DALStatement) actual, expected);
        } else if (actual instanceof RDLStatement) {
            RDLStatementAssert.assertIs(assertContext, (RDLStatement) actual, expected);
        } else if (actual instanceof RQLStatement) {
            RQLStatementAssert.assertIs(assertContext, (RQLStatement) actual, expected);
        } else if (actual instanceof RALStatement) {
            RALStatementAssert.assertIs(assertContext, (RALStatement) actual, expected);
        } else if (actual instanceof RULStatement) {
            RULStatementAssert.assertIs(assertContext, (RULStatement) actual, expected);
        }
    }
}
