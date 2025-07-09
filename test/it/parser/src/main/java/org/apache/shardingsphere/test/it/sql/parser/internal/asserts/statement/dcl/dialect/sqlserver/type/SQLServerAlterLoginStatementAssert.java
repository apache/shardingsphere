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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.sqlserver.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.login.SQLServerAlterLoginStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerAlterLoginStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Alter login statement assert for SQLServer.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLServerAlterLoginStatementAssert {
    
    /**
     * Assert alter login statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter login statement
     * @param expected expected alter login statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SQLServerAlterLoginStatement actual, final SQLServerAlterLoginStatementTestCase expected) {
        if (null == expected.getLogin()) {
            assertNull(actual.getLoginSegment(), assertContext.getText("Actual login should not exist."));
        } else {
            assertNotNull(actual.getLoginSegment(), assertContext.getText("Actual login should exist."));
            assertThat(assertContext.getText("Login name assertion error: "), actual.getLoginSegment().getLoginName().getValueWithQuoteCharacters(), is(expected.getLogin().getName()));
            SQLSegmentAssert.assertIs(assertContext, actual.getLoginSegment(), expected.getLogin());
        }
    }
}
