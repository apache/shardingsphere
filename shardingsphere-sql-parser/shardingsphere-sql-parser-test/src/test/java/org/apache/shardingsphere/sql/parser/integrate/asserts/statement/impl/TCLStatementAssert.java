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

package org.apache.shardingsphere.sql.parser.integrate.asserts.statement.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.SQLParserTestCase;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.TCLStatement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * TCL statement assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TCLStatementAssert {
    
    /**
     * Assert TCL statement is correct with expected parser result.
     * 
     * @param actual actual TCL statement
     * @param expected expected parser result
     */
    public static void assertIs(final TCLStatement actual, final SQLParserTestCase expected) {
        assertThat(actual.getClass().getName(), is(expected.getTclActualStatementClassType()));
        if (actual instanceof SetAutoCommitStatement) {
            assertThat(((SetAutoCommitStatement) actual).isAutoCommit(), is(expected.isAutoCommit()));
        }
    }
}
