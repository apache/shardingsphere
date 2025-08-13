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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.CreateSequenceStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.sequence.CreateSequenceStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Create sequence statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateSequenceStatementAssert {
    
    /**
     * Assert create sequence statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create sequence statement
     * @param expected expected create sequence statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateSequenceStatement actual, final CreateSequenceStatementTestCase expected) {
        assertSequenceName(assertContext, actual, expected);
    }
    
    private static void assertSequenceName(final SQLCaseAssertContext assertContext, final CreateSequenceStatement actual, final CreateSequenceStatementTestCase expected) {
        if (null == expected.getSequenceName()) {
            assertNull(actual.getSequenceName(), assertContext.getText("Actual create sequence segment should not exist."));
        } else {
            assertNotNull(actual.getSequenceName(), assertContext.getText("Actual create sequence segment should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s create sequence assertion error: ", actual.getClass().getSimpleName())),
                    actual.getSequenceName(), is(expected.getSequenceName().getName()));
        }
    }
}
