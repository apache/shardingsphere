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
import org.apache.shardingsphere.distsql.statement.type.ral.RALStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.RQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.RULStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.lcl.LCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.function.OracleCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.procedure.OracleCreateProcedureStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.parameter.ParameterMarkerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.comment.CommentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.DALStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.DCLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.DDLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.DMLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.lcl.LCLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.plsql.PLSQLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.RALStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.RDLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rql.RQLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rul.RULStatementAssert;
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
        if (actual instanceof OracleCreateProcedureStatement || actual instanceof OracleCreateFunctionStatement) {
            PLSQLStatementAssert.assertIs(assertContext, actual, expected);
            return;
        }
        ParameterMarkerAssert.assertCount(assertContext, actual.getParameterCount(), expected.getParameters().size());
        CommentAssert.assertComment(assertContext, actual, expected);
        if (actual instanceof DMLStatement) {
            DMLStatementAssert.assertIs(assertContext, (DMLStatement) actual, expected);
        } else if (actual instanceof DDLStatement) {
            DDLStatementAssert.assertIs(assertContext, (DDLStatement) actual, expected);
        } else if (actual instanceof TCLStatement) {
            TCLStatementAssert.assertIs(assertContext, (TCLStatement) actual, expected);
        } else if (actual instanceof LCLStatement) {
            LCLStatementAssert.assertIs(assertContext, (LCLStatement) actual, expected);
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
