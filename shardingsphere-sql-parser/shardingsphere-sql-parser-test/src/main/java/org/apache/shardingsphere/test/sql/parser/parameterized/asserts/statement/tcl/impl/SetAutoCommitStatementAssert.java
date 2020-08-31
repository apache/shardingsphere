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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.tcl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.tcl.SetAutoCommitStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.SetAutoCommitStatement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Set auto commit statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SetAutoCommitStatementAssert {
    
    /**
     * Assert set auto commit statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual set auto commit statement
     * @param expected expected set auto commit statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SetAutoCommitStatement actual, final SetAutoCommitStatementTestCase expected) {
        assertThat(assertContext.getText("Set auto commit assertion error: "), actual.isAutoCommit(), is(expected.isAutoCommit()));
    }
}
