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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLFlushStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.dal.FlushStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Flush statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlushStatementAssert {
    
    /**
     * Assert flush statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual flush statement
     * @param expected expected flush statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLFlushStatement actual, final FlushStatementTestCase expected) {
        assertThat(assertContext.getText("Flush statement is flush table assert error: "), actual.isFlushTable(), is(expected.isFlushTable()));
        assertTables(assertContext, actual, expected);
    }
    
    private static void assertTables(final SQLCaseAssertContext assertContext, final MySQLFlushStatement actual, final FlushStatementTestCase expected) {
        TableAssert.assertIs(assertContext, actual.getTables(), expected.getTables());
    }
}
