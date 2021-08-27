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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dcl.impl.mysql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.generic.GrantLevelSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.GrantStatementTestCase;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * MySQL Grant statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLGrantStatementAssert {
    
    /**
     * Assert MySQL grant statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual MySQL grant statement
     * @param expected expected grant statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLGrantStatement actual, final GrantStatementTestCase expected) {
        if (null != expected.getTables() && !expected.getTables().isEmpty()) {
            assertThat(expected.getTables().size(), is(1));
            GrantLevelSegmentAssert.assertIs(assertContext, actual.getLevel(), expected.getTables());
        } else {
            assertThat(assertContext.getText("Actual table should not exist."), actual.getTables(), is(Collections.emptyList()));
        }
    }
}
