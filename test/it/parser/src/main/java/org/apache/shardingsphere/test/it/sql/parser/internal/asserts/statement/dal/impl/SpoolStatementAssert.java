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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dal.OracleSpoolStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.SpoolStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Oracle spool statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpoolStatementAssert {
    
    /**
     * Assert spool statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual spool statement
     * @param expected expected spool statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final OracleSpoolStatement actual, final SpoolStatementTestCase expected) {
        if (null != expected.getFilename()) {
            assertThat(assertContext.getText("Actual filename does not match: "), actual.getFileName(), is(expected.getFilename()));
        }
    }
}
