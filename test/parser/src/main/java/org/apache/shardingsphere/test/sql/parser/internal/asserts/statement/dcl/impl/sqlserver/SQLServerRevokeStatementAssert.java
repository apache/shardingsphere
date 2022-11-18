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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dcl.impl.sqlserver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerRevokeStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dcl.RevokeStatementTestCase;

/**
 * SQLServer Revoke statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLServerRevokeStatementAssert {
    
    /**
     * Assert SQLServer Revoke statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual SQLServer revoke statement
     * @param expected expected revoke statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SQLServerRevokeStatement actual, final RevokeStatementTestCase expected) {
        if (0 != expected.getTables().size()) {
            TableAssert.assertIs(assertContext, actual.getTables(), expected.getTables());
        }
        if (0 != expected.getColumns().size()) {
            ColumnAssert.assertIs(assertContext, actual.getColumns(), expected.getColumns());
        }
    }
}
